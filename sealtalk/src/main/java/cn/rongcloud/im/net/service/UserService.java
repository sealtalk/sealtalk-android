package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;

import java.util.List;

import cn.rongcloud.im.db.model.FriendBlackInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.ContactGroupResult;
import cn.rongcloud.im.model.GetPokeResult;
import cn.rongcloud.im.model.LoginResult;
import cn.rongcloud.im.model.RegionResult;
import cn.rongcloud.im.model.RegisterResult;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.UploadTokenResult;
import cn.rongcloud.im.model.VerifyResult;
import cn.rongcloud.im.net.SealTalkUrl;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {
    @POST(SealTalkUrl.LOGIN)
    LiveData<Result<LoginResult>> loginLiveData(@Body RequestBody body);

    @GET(SealTalkUrl.GET_TOKEN)
    Call<Result<LoginResult>> getToken();

    @GET(SealTalkUrl.GET_USER_INFO)
    LiveData<Result<UserInfo>> getUserInfo(@Path("user_id") String userId);

    @POST(SealTalkUrl.SEND_CODE)
    LiveData<Result<String>> sendCode(@Body RequestBody body);

    @POST(SealTalkUrl.VERIFY_CODE)
    LiveData<Result<VerifyResult>> verifyCode(@Body RequestBody body);

    @POST(SealTalkUrl.REGISTER)
    LiveData<Result<RegisterResult>> register(@Body RequestBody body);

    @GET(SealTalkUrl.REGION_LIST)
    LiveData<Result<List<RegionResult>>> getRegionList();

    @POST(SealTalkUrl.CHECK_PHONE_AVAILABLE)
    LiveData<Result<Boolean>> checkPhoneAvailable(@Body RequestBody body);

    @POST(SealTalkUrl.RESET_PASSWORD)
    LiveData<Result<String>> resetPassword(@Body RequestBody body);

    @POST(SealTalkUrl.SET_NICK_NAME)
    LiveData<Result> setMyNickName(@Body RequestBody requestBody);

    @POST(SealTalkUrl.SET_ST_ACCOUNT)
    LiveData<Result> setStAccount(@Body RequestBody requestBody);

    @POST(SealTalkUrl.SET_GENDER)
    LiveData<Result> setGender(@Body RequestBody requestBody);

    @GET(SealTalkUrl.GET_IMAGE_UPLOAD_TOKEN)
    LiveData<Result<UploadTokenResult>> getImageUploadToken();

    @POST(SealTalkUrl.SET_PORTRAIT)
    LiveData<Result> setPortrait(@Body RequestBody body);

    @POST(SealTalkUrl.CHANGE_PASSWORD)
    LiveData<Result> changePassword(@Body RequestBody body);


    /**
     * 获取黑名单信息
     *
     * @return
     */
    @GET(SealTalkUrl.GET_BLACK_LIST)
    LiveData<Result<List<FriendBlackInfo>>> getFriendBlackList();

    /**
     * 添加到黑名单
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.ADD_BLACK_LIST)
    LiveData<Result> addToBlackList(@Body RequestBody body);

    /**
     * 移除黑名单
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.REMOVE_BLACK_LIST)
    LiveData<Result> removeFromBlackList(@Body RequestBody body);

    /**
     * 获取通讯录中的群组列表
     *
     * @return
     */
    @GET(SealTalkUrl.GROUP_GET_ALL_IN_CONTACT)
    LiveData<Result<ContactGroupResult>> getGroupListInContact();

    /**
     * 设置接收戳一下消息状态
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.SET_RECEIVE_POKE_MESSAGE_STATUS)
    LiveData<Result> setReceivePokeMessageStatus(@Body RequestBody body);

    /**
     * 获取接收戳一下消息状态
     *
     * @return
     */
    @GET(SealTalkUrl.GET_RECEIVE_POKE_MESSAGE_STATUS)
    LiveData<Result<GetPokeResult>> getReceivePokeMessageStatus();

    @POST(SealTalkUrl.REGISTER_AND_LOGIN)
    LiveData<Result<LoginResult>> registerAndLogin(@Body RequestBody body);
}
