package cn.rongcloud.im.task;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.ResultCallback;
import cn.rongcloud.im.db.DbManager;
import cn.rongcloud.im.db.dao.FriendDao;
import cn.rongcloud.im.db.dao.GroupDao;
import cn.rongcloud.im.db.dao.UserDao;
import cn.rongcloud.im.db.model.BlackListEntity;
import cn.rongcloud.im.db.model.FriendBlackInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.file.FileManager;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.BlackListUser;
import cn.rongcloud.im.model.ContactGroupResult;
import cn.rongcloud.im.model.GetPokeResult;
import cn.rongcloud.im.model.LoginResult;
import cn.rongcloud.im.model.RegionResult;
import cn.rongcloud.im.model.RegisterResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.model.VerifyResult;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.service.UserService;
import cn.rongcloud.im.sp.CountryCache;
import cn.rongcloud.im.sp.UserCache;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.NetworkBoundResource;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import cn.rongcloud.im.utils.RongGenerate;
import cn.rongcloud.im.utils.SearchUtils;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imlib.model.Conversation;
import okhttp3.RequestBody;

/**
 * 用户相关业务处理
 */
public class UserTask {
    private FileManager fileManager;
    private UserService userService;
    private Context context;
    private DbManager dbManager;
    private IMManager imManager;
    //存储当前最新一次登录的用户信息
    private UserCache userCache;
    private CountryCache countryCache;


    public UserTask(Context context) {
        this.context = context.getApplicationContext();
        userService = HttpClientManager.getInstance(context).getClient().createService(UserService.class);
        dbManager = DbManager.getInstance(context.getApplicationContext());
        fileManager = new FileManager(context.getApplicationContext());
        userCache = new UserCache(context.getApplicationContext());
        countryCache = new CountryCache(context.getApplicationContext());
        imManager = IMManager.getInstance();
    }

    /**
     * 用户登录
     *
     * @param region   国家区号
     * @param phone    手机号码
     * @param password 密码
     */
    public LiveData<Resource<String>> login(String region, String phone, String password) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<LoginResult>> login = new NetworkOnlyResource<LoginResult, Result<LoginResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<LoginResult>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("region", region);
                paramsMap.put("phone", phone);
                paramsMap.put("password", password);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.loginLiveData(body);
            }
        }.asLiveData();
        result.addSource(login, loginResultResource -> {
            if (loginResultResource.status == Status.SUCCESS) {
                result.removeSource(login);
                LoginResult loginResult = loginResultResource.data;
                if (loginResult != null) {
                    imManager.connectIM(loginResult.token, false, new ResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            result.postValue(Resource.success(s));
                            // 存储当前登录成功的用户信息
                            UserCacheInfo info = new UserCacheInfo(s, loginResult.token, phone, password, region, countryCache.getCountryInfoByRegion(region));
                            userCache.saveUserCache(info);
                        }

                        @Override
                        public void onFail(int errorCode) {
                            result.postValue(Resource.error(errorCode, null));
                        }
                    });
                } else {
                    result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                }
            } else if (loginResultResource.status == Status.ERROR) {
                result.setValue(Resource.error(loginResultResource.code, null));
            } else {
                // do nothing
            }
        });
        return result;
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    public LiveData<Resource<UserInfo>> getUserInfo(final String userId) {
        return new NetworkBoundResource<UserInfo, Result<UserInfo>>() {
            @Override
            protected void saveCallResult(@NonNull Result<UserInfo> item) {
                if (item.getResult() == null) return;
                UserInfo userInfo = item.getResult();

                UserDao userDao = dbManager.getUserDao();
                if (userDao != null) {
                    String nameSpelling = SearchUtils.fullSearchableString(userInfo.getName());

                    userInfo.setNameSpelling(nameSpelling);
                    String portraitUri = userInfo.getPortraitUri();

                    // 当没有头像时生成默认头像
                    if (TextUtils.isEmpty(portraitUri)) {
                        portraitUri = RongGenerate.generateDefaultAvatar(context, userInfo.getId(), userInfo.getName());
                        userInfo.setPortraitUri(portraitUri);
                    }

                    String stAccount = userInfo.getStAccount();
                    if (!TextUtils.isEmpty(stAccount)) {
                        userDao.updateSAccount(userInfo.getId(), stAccount);
                    }
                    String gender = userInfo.getGender();
                    if (!TextUtils.isEmpty(gender)) {
                        userDao.updateGender(userInfo.getId(), gender);
                    }
                    // 更新现有用户信息若没有则创建新的用户信息，防止覆盖其他已有字段
                    int resultCount = userDao.updateNameAndPortrait(userInfo.getId(), userInfo.getName(), nameSpelling, portraitUri);
                    if (resultCount == 0) {
                        // 当前用户的话， 判断是否有电话号码， 没有则从缓存中取出
                        if (userInfo.getId().equals(imManager.getCurrentId())) {
                            UserCacheInfo cacheInfo = userCache.getUserCache();
                            if (cacheInfo != null && cacheInfo.getId().equals(userInfo.getId())) {
                                userInfo.setPhoneNumber(cacheInfo.getPhoneNumber());
                            }
                        }

                        userDao.insertUser(userInfo);
                    }
                }

                // 更新 IMKit 显示缓存
                String alias = "";
                if (userDao != null) {
                    alias = userDao.getUserByIdSync(userInfo.getId()).getAlias();
                }
                //有备注名的时，使用备注名
                String name = TextUtils.isEmpty(alias) ? userInfo.getName() : alias;
                IMManager.getInstance().updateUserInfoCache(userInfo.getId(), name, Uri.parse(userInfo.getPortraitUri()));
            }

            @NonNull
            @Override
            protected LiveData<UserInfo> loadFromDb() {

                UserDao userDao = dbManager.getUserDao();
                if (userDao != null) {
                    return userDao.getUserById(userId);
                } else {
                    return new MediatorLiveData<>();
                }
            }

            @NonNull
            @Override
            protected LiveData<Result<UserInfo>> createCall() {
                return userService.getUserInfo(userId);
            }
        }.asLiveData();
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    public UserInfo getUserInfoSync(final String userId) {
        return dbManager.getUserDao().getUserByIdSync(userId);
    }


    /**
     * 发送验证码
     *
     * @param region
     * @param phoneNumber
     * @return
     */
    public LiveData<Resource<String>> sendCode(String region, String phoneNumber) {
        return new NetworkOnlyResource<String, Result<String>>() {

            @NonNull
            @Override
            protected LiveData<Result<String>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("region", region);
                paramsMap.put("phone", phoneNumber);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.sendCode(body);
            }
        }.asLiveData();
    }

    /**
     * 注册请求
     *
     * @param phoneCode
     * @param phoneNumber
     * @param shortMsgCode
     * @param nickName
     * @param password
     * @return
     */
    public LiveData<Resource<RegisterResult>> register(String phoneCode, String phoneNumber, String shortMsgCode, String nickName, String password) {
        MediatorLiveData<Resource<RegisterResult>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<VerifyResult>> verify = new NetworkOnlyResource<VerifyResult, Result<VerifyResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<VerifyResult>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("region", phoneCode);
                paramsMap.put("phone", phoneNumber);
                paramsMap.put("code", shortMsgCode);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.verifyCode(body);
            }
        }.asLiveData();
        result.addSource(verify, verifyResult -> {
            if (verifyResult.status == Status.SUCCESS) {
                String verifyToken = verifyResult.data.verification_token;
                LiveData<Resource<RegisterResult>> register = new NetworkOnlyResource<RegisterResult, Result<RegisterResult>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<RegisterResult>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("nickname", nickName);
                        paramsMap.put("password", password);
                        paramsMap.put("verification_token", verifyToken);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return userService.register(body);
                    }
                }.asLiveData();

                result.addSource(register, registerResult -> {
                    if (registerResult.status == Status.SUCCESS) {
                        if (registerResult != null) {
                            result.postValue(registerResult);
                        } else {
                            result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                        }
                    } else if (registerResult.status == Status.ERROR) {
                        result.setValue(Resource.error(registerResult.code, null));
                    }
                });

            } else if (verifyResult.status == Status.ERROR) {
                result.setValue(Resource.error(verifyResult.code, null));
            } else {
                result.setValue(Resource.loading(null));
            }
        });

        return result;
    }

    /**
     * 获取地区的消息列表
     *
     * @return
     */
    public LiveData<Resource<List<RegionResult>>> getRegionList() {
        return new NetworkOnlyResource<List<RegionResult>, Result<List<RegionResult>>>() {

            @NonNull
            @Override
            protected LiveData<Result<List<RegionResult>>> createCall() {
                return userService.getRegionList();
            }
        }.asLiveData();
    }

    /**
     * 检测手机号是否注册了
     *
     * @param phoneCode
     * @param phoneNumber
     * @return
     */
    public LiveData<Resource<Boolean>> checkPhoneAvailable(String phoneCode, String phoneNumber) {
        return new NetworkOnlyResource<Boolean, Result<Boolean>>() {

            @NonNull
            @Override
            protected LiveData<Result<Boolean>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("region", phoneCode);
                paramsMap.put("phone", phoneNumber);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.checkPhoneAvailable(body);
            }
        }.asLiveData();
    }

    /**
     * 重置密码
     *
     * @param countryCode
     * @param phoneNumber
     * @param shortMsgCode
     * @param password
     * @return
     */
    public LiveData<Resource<String>> resetPassword(String countryCode, String phoneNumber, String shortMsgCode, String password) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<VerifyResult>> verify = new NetworkOnlyResource<VerifyResult, Result<VerifyResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<VerifyResult>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("region", countryCode);
                paramsMap.put("phone", phoneNumber);
                paramsMap.put("code", shortMsgCode);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.verifyCode(body);
            }
        }.asLiveData();

        result.addSource(verify, verifyResult -> {
            if (verifyResult != null) {
                if (verifyResult.status == Status.SUCCESS) {
                    String verifyToken = verifyResult.data.verification_token;
                    LiveData<Resource<String>> resetPassword = new NetworkOnlyResource<String, Result<String>>() {

                        @NonNull
                        @Override
                        protected LiveData<Result<String>> createCall() {
                            HashMap<String, Object> paramsMap = new HashMap<>();
                            paramsMap.put("password", password);
                            paramsMap.put("verification_token", verifyToken);
                            RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                            return userService.resetPassword(body);
                        }
                    }.asLiveData();

                    result.addSource(resetPassword, resetPasswordResult -> {
                        if (resetPasswordResult.status == Status.SUCCESS) {
                            if (resetPasswordResult != null) {
                                result.postValue(resetPasswordResult);
                            } else {
                                result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                            }
                        } else if (resetPasswordResult.status == Status.ERROR) {
                            result.setValue(Resource.error(resetPasswordResult.code, null));
                        }
                    });

                } else if (verifyResult.status == Status.ERROR) {
                    result.setValue(Resource.error(verifyResult.code, null));
                } else {
                    result.setValue(Resource.loading(null));
                }
            } else if (verifyResult.status == Status.ERROR) {
                result.setValue(Resource.error(verifyResult.code, null));
            }

        });
        return result;
    }

    /**
     * 设置自己的昵称
     *
     * @param nickName
     * @return
     */
    public LiveData<Resource<Result>> setMyNickName(String nickName) {
        return new NetworkOnlyResource<Result, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("nickname", nickName);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.setMyNickName(body);
            }

            @Override
            protected Result transformRequestType(Result response) {
                return response;
            }

            @Override
            protected void saveCallResult(@NonNull Result item) {
                //更新 nickName
                saveAndSyncNickname(nickName);
            }
        }.asLiveData();
    }

    /**
     * 设置 SealTalk 账号
     *
     * @param stAccount
     * @return
     */
    public LiveData<Resource<Result>> setStAccount(String stAccount) {
        return new NetworkOnlyResource<Result, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("stAccount", stAccount);
                RequestBody body = RetrofitUtil.createJsonRequest(paramMap);
                return userService.setStAccount(body);
            }

            @Override
            protected Result transformRequestType(Result response) {
                return response;
            }

            @Override
            protected void saveCallResult(@NonNull Result item) {
                updateStAccount(IMManager.getInstance().getCurrentId(), stAccount);
            }
        }.asLiveData();

    }

    public LiveData<Resource<Result>> setGender(String gender) {
        return new NetworkOnlyResource<Result, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("gender", gender);
                RequestBody body = RetrofitUtil.createJsonRequest(paramMap);
                return userService.setGender(body);
            }

            @Override
            protected Result transformRequestType(Result response) {
                return response;
            }

            @Override
            protected void saveCallResult(@NonNull Result item) {
                updateGender(IMManager.getInstance().getCurrentId(), gender);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Result>> setPortrait(Uri imageUri) {
        MediatorLiveData<Resource<Result>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<String>> uploadResource = fileManager.uploadImage(imageUri);
        result.addSource(uploadResource, new Observer<Resource<String>>() {
            @Override
            public void onChanged(Resource<String> resource) {
                if (resource.status != Status.LOADING) {
                    result.removeSource(uploadResource);
                }

                if (resource.status == Status.ERROR) {
                    result.setValue(Resource.error(resource.code, null));
                    return;
                }

                if (resource.status == Status.SUCCESS) {
                    LiveData<Resource<Result>> setPortrait = setPortrait(resource.data);
                    result.addSource(setPortrait, new Observer<Resource<Result>>() {
                        @Override
                        public void onChanged(Resource<Result> resultResource) {

                            if (resultResource.status != Status.LOADING) {
                                result.removeSource(setPortrait);
                            }

                            if (resultResource.status == Status.ERROR) {
                                result.setValue(Resource.error(resultResource.code, null));
                                return;
                            }
                            if (resultResource.status == Status.SUCCESS) {
                                result.setValue(resultResource);
                            }
                        }
                    });
                }
            }
        });

        return result;
    }

    /**
     * 设置头像信息
     *
     * @param portraitUrl
     * @return
     */
    private LiveData<Resource<Result>> setPortrait(String portraitUrl) {
        return new NetworkOnlyResource<Result, Result>() {

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("portraitUri", portraitUrl);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.setPortrait(body);
            }

            @Override
            protected Result transformRequestType(Result response) {
                return response;
            }

            @Override
            protected void saveCallResult(@NonNull Result item) {
                //更新 头像
                saveAndSyncPortrait(IMManager.getInstance().getCurrentId(), portraitUrl);
            }

        }.asLiveData();
    }


    /**
     * 保存并同步用户的头像
     *
     * @param userId
     * @param portraitUrl
     */
    public void saveAndSyncPortrait(String userId, String portraitUrl) {
        saveAndSyncUserInfo(userId, null, portraitUrl);
//
//        UserDao userDao = dbManager.getUserDao();
//        if (userDao != null) {
//            int i = userDao.updatePortrait(userId, portraitUrl);
//            SLog.d("ss_update", "i=" + i);
//        }
    }

    /**
     * 保存并同步用户的昵称
     *
     * @param nickName
     */
    public void saveAndSyncNickname(String nickName) {
        saveAndSyncUserInfo(IMManager.getInstance().getCurrentId(), nickName, null);
    }

    /**
     * 保存并同步用户的昵称和头像
     *
     * @param userId
     * @param nickName
     * @param portraitUrl
     */
    public void saveAndSyncUserInfo(String userId, String nickName, String portraitUrl) {
        UserDao userDao = dbManager.getUserDao();
        if (userDao != null) {
            UserInfo userInfo = userDao.getUserByIdSync(userId);
            if (nickName == null) {
                nickName = userInfo == null ? "" : userInfo.getName();
            }

            if (portraitUrl == null) {
                portraitUrl = userInfo == null ? "" : userInfo.getPortraitUri();
            }
            int i = userDao.updateNameAndPortrait(userId, nickName, CharacterParser.getInstance().getSpelling(nickName), portraitUrl);
            SLog.d("ss_update", "i=" + i);

            IMManager.getInstance().updateUserInfoCache(userId, nickName, Uri.parse(portraitUrl));
        }
    }

    /**
     * 更新数据库 SealTalk 号信息
     *
     * @param userId
     * @param stAccount
     */
    public void updateStAccount(String userId, String stAccount) {
        UserDao userDao = dbManager.getUserDao();
        if (userDao != null) {
            int i = userDao.updateSAccount(userId, stAccount);
            SLog.d("st_update", "i=" + i);
        }
    }

    public void updateGender(String userId, String gender) {
        UserDao userDao = dbManager.getUserDao();
        if (userDao != null) {
            int i = userDao.updateGender(userId, gender);
            SLog.d("gender_update", "i=" + i);
        }
    }


    /**
     * 修改用户密码
     *
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public LiveData<Resource<Result>> changePassword(String oldPassword, String newPassword) {
        return new NetworkOnlyResource<Result, Result>() {

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("oldPassword", oldPassword);
                paramsMap.put("newPassword", newPassword);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return userService.changePassword(body);
            }

            @Override
            protected Result transformRequestType(Result response) {
                return response;
            }
        }.asLiveData();
    }

    /**
     * 获取最新一次登录用户的缓存信息
     *
     * @return
     */
    public UserCacheInfo getUserCache() {
        return userCache.getUserCache();
    }


    /**
     * 获取黑名单用户
     *
     * @return
     */
    public LiveData<Resource<List<UserSimpleInfo>>> getBlackList() {
        return new NetworkBoundResource<List<UserSimpleInfo>, Result<List<FriendBlackInfo>>>() {
            @Override
            protected void saveCallResult(@NonNull Result<List<FriendBlackInfo>> item) {
                List<FriendBlackInfo> result = item.getResult();
                if (result == null) return;

                List<BlackListEntity> blackList = new ArrayList<>();
                BlackListEntity addBlack;
                UserInfo user;

                UserDao userDao = dbManager.getUserDao();

                for (FriendBlackInfo blackInfo : result) {
                    BlackListUser blackUser = blackInfo.getUser();
                    if (blackUser == null) continue;

                    // 将黑名单中的用户信息更新用户表
                    user = new UserInfo();
                    user.setId(blackUser.getId());
                    String nickname = blackUser.getNickname();
                    String nameSpelling = SearchUtils.fullSearchableString(nickname);
                    user.setNameSpelling(nameSpelling);
                    user.setName(nickname);
                    String portraitUri = blackUser.getPortraitUri();

                    // 当没有头像时生成默认头像
                    if (TextUtils.isEmpty(portraitUri)) {
                        portraitUri = RongGenerate.generateDefaultAvatar(context, blackUser.getId(), nickname);
                        user.setPortraitUri(portraitUri);
                    } else {
                        user.setPortraitUri(portraitUri);
                    }

                    // 更新现有用户信息若没有则创建新的用户信息，防止覆盖其他已有字段
                    int resultCount = userDao.updateNameAndPortrait(user.getId(), user.getName(), nameSpelling, portraitUri);
                    if (resultCount == 0) {
                        userDao.insertUser(user);
                    }

                    // 添加到黑名单
                    addBlack = new BlackListEntity();
                    addBlack.setId(blackUser.getId());
                    blackList.add(addBlack);
                }

                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    // 每次清除之前有的黑名单
                    friendDao.deleteAllBlackList();
                    friendDao.updateBlackList(blackList);
                }
            }

            @NonNull
            @Override
            protected LiveData<List<UserSimpleInfo>> loadFromDb() {
                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    return friendDao.getBlackListUser();
                } else {
                    return new MutableLiveData<>(null);
                }
            }

            @NonNull
            @Override
            protected LiveData<Result<List<FriendBlackInfo>>> createCall() {
                return userService.getFriendBlackList();
            }
        }.asLiveData();
    }


    /**
     * 添加到黑名单
     *
     * @return
     */
    public LiveData<Resource<Void>> addToBlackList(String userId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    BlackListEntity blackListEntity = new BlackListEntity();
                    blackListEntity.setId(userId);
                    friendDao.addToBlackList(blackListEntity);
                }

                IMManager.getInstance().clearConversationAndMessage(userId, Conversation.ConversationType.PRIVATE);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("friendId", userId);
                return userService.addToBlackList(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 移除黑名单
     *
     * @return
     */
    public LiveData<Resource<Void>> removeFromBlackList(String userId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    friendDao.removeFromBlackList(userId);
                }
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("friendId", userId);
                return userService.removeFromBlackList(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 获取通讯录群组列表
     *
     * @return
     */
    public LiveData<Resource<List<GroupEntity>>> getContactGroupList() {
        return new NetworkBoundResource<List<GroupEntity>, Result<ContactGroupResult>>() {
            @Override
            protected void saveCallResult(@NonNull Result<ContactGroupResult> item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao == null) return;
                // 先清除所有群组在通讯录状态
                groupDao.clearAllGroupContact();

                ContactGroupResult result = item.getResult();
                if (result == null) return;
                List<GroupEntity> list = result.getList();
                if (list != null && list.size() > 0) {
                    // 设置默认头像和名称拼音
                    for (GroupEntity groupEntity : list) {
                        String portraitUri = groupEntity.getPortraitUri();
                        if (TextUtils.isEmpty(portraitUri)) {
                            portraitUri = RongGenerate.generateDefaultAvatar(context, groupEntity.getId(), groupEntity.getName());
                            groupEntity.setPortraitUri(portraitUri);
                        }
                        groupEntity.setNameSpelling(SearchUtils.fullSearchableString(groupEntity.getName()));
                        groupEntity.setNameSpellingInitial(SearchUtils.initialSearchableString(groupEntity.getName()));
                        groupEntity.setOrderSpelling(CharacterParser.getInstance().getSpelling(groupEntity.getName()));
                        // 设置在该群是在通讯录中
                        groupEntity.setIsInContact(1);
                    }

                    groupDao.insertGroup(list);
                }
            }

            @NonNull
            @Override
            protected LiveData<List<GroupEntity>> loadFromDb() {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    return groupDao.getContactGroupInfoList();
                }
                return new MutableLiveData<>(null);
            }

            @NonNull
            @Override
            protected LiveData<Result<ContactGroupResult>> createCall() {
                return userService.getGroupListInContact();
            }
        }.asLiveData();
    }


    /**
     * 从数据中获取用户是否在黑名单
     *
     * @param friendId
     * @return
     */
    public LiveData<Resource<UserSimpleInfo>> getInBlackListUser(String friendId) {
        /*
         * 由于目前没有查询单一用户是否在黑名单的 API ，所以先从获取所有的黑名单列表
         * 然后再从数据库中返回单一黑名单用户的信息
         */
        MediatorLiveData<Resource<UserSimpleInfo>> result = new MediatorLiveData<>();

        // API 请求后的数据库数据源
        LiveData<Resource<List<UserSimpleInfo>>> blackListResource = getBlackList();

        // 单一黑名单用户的数据源
        LiveData<UserSimpleInfo> dbSource;
        FriendDao friendDao = dbManager.getFriendDao();
        if (friendDao != null) {
            dbSource = friendDao.getUserInBlackList(friendId);
        } else {
            dbSource = new MutableLiveData<>(null);
        }

        result.addSource(blackListResource, resource -> {
            if (resource.status != Status.LOADING) {
                result.removeSource(blackListResource);
            }

            if (resource.status == Status.SUCCESS) {
                result.addSource(dbSource, newData -> {
                    result.setValue(Resource.success(newData));
                });
            } else if (resource.status == Status.ERROR) {
                result.addSource(dbSource, newData -> {
                    result.setValue(Resource.error(resource.code, newData));
                });
            }
        });

        return result;
    }

    /**
     * 退出登录
     */
    public void logout() {
        userCache.logoutClear();
        dbManager.closeDb();
    }

    /**
     * 设置是否接收戳一下消息
     *
     * @param isReceive
     * @return
     */
    public LiveData<Resource<Void>> setReceivePokeMessageState(boolean isReceive) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                IMManager.getInstance().updateReceivePokeMessageStatus(isReceive);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("pokeStatus", isReceive ? 1 : 0); // 0 不允许; 1 允许
                return userService.setReceivePokeMessageStatus(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 获取是否接收戳一下消息状态
     *
     * @return
     */
    public LiveData<Resource<GetPokeResult>> getReceivePokeMessageState() {
        return new NetworkOnlyResource<GetPokeResult, Result<GetPokeResult>>() {
            @Override
            protected void saveCallResult(@NonNull GetPokeResult item) {
                IMManager.getInstance().updateReceivePokeMessageStatus(item.isReceivePokeMessage());
            }

            @NonNull
            @Override
            protected LiveData<Result<GetPokeResult>> createCall() {
                return userService.getReceivePokeMessageStatus();
            }
        }.asLiveData();
    }
}
