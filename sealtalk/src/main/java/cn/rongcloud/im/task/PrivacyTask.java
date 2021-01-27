package cn.rongcloud.im.task;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.HashMap;

import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.model.PrivacyResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.ScreenCaptureResult;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.service.PrivacyService;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.utils.NetworkOnlyResource;

public class PrivacyTask {

    private DBManager dbManager;
    private PrivacyService privacyService;
    private Context context;
    private UserConfigCache userConfigCache;

    public PrivacyTask(Context context) {
        this.context = context.getApplicationContext();
        userConfigCache = new UserConfigCache(context);
        dbManager = DBManager.getInstance(context);
        privacyService = HttpClientManager.getInstance(context).getClient().createService(PrivacyService.class);
    }

    /**
     * 用户隐私设置（可同时设置多项，传-1为不设置，0允许，1不允许）
     *
     * @param phoneVerify    是否可以通过电话号码查找
     * @param stSearchVerify 是否可以通过 SealTalk 号查找
     * @param friVerify      加好友验证
     * @param groupVerify    允许直接添加至群聊
     * @return
     */
    public LiveData<Resource<Void>> setPrivacy(int phoneVerify, int stSearchVerify,
                                               int friVerify, int groupVerify) {
        return new NetworkOnlyResource<Void, Result>() {

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                if (phoneVerify != -1) {
                    paramMap.put("phoneVerify", phoneVerify);
                }
                if (stSearchVerify != -1) {
                    paramMap.put("stSearchVerify", stSearchVerify);
                }
                if (friVerify != -1) {
                    paramMap.put("friVerify", friVerify);
                }
                if (groupVerify != -1) {
                    paramMap.put("groupVerify", groupVerify);
                }
                return privacyService.setPrivacy(RetrofitUtil.createJsonRequest(paramMap));
            }
        }.asLiveData();
    }

    /**
     * 获取个人隐私设置
     *
     * @return
     */
    public LiveData<Resource<PrivacyResult>> getPrivacyState() {
        return new NetworkOnlyResource<PrivacyResult, Result<PrivacyResult>>() {

            @NonNull
            @Override
            protected LiveData<Result<PrivacyResult>> createCall() {
                return privacyService.getPrivacy();
            }
        }.asLiveData();
    }

    /**
     * 获取是否开启截屏通知状态
     *
     * @param conversationType
     * @param targetId
     * @return
     */
    public LiveData<Resource<ScreenCaptureResult>> getScreenCapture(int conversationType, String targetId) {
        return new NetworkOnlyResource<ScreenCaptureResult, Result<ScreenCaptureResult>>() {

            @NonNull
            @Override
            protected LiveData<Result<ScreenCaptureResult>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("conversationType", conversationType);
                paramMap.put("targetId", targetId);
                return privacyService.getScreenCapture(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull ScreenCaptureResult item) {
                super.saveCallResult(item);
                userConfigCache.setScreenCaptureStatus(item.status);
            }
        }.asLiveData();
    }

    /**
     * 设置是否开启截屏通知
     *
     * @param conversationType
     * @param targetId
     * @param noticeStatus     0 关闭 1 打开
     * @return
     */
    public LiveData<Resource<Void>> setScreenCapture(int conversationType, String targetId, int noticeStatus) {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("conversationType", conversationType);
                paramMap.put("targetId", targetId);
                paramMap.put("noticeStatus", noticeStatus);
                return privacyService.setScreenCapture(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                super.saveCallResult(item);
                userConfigCache.setScreenCaptureStatus(noticeStatus);
            }
        }.asLiveData();
    }

    /**
     * 发送使用了截屏通知的消息
     *
     * @param conversationType
     * @param targetId
     * @return
     */
    public LiveData<Resource<Void>> sendScreenShotMessage(int conversationType, String targetId) {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("conversationType", conversationType);
                paramMap.put("targetId", targetId);
                return privacyService.sendScreenShotMsg(RetrofitUtil.createJsonRequest(paramMap));
            }
        }.asLiveData();
    }
}
