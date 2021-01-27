package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.GetPokeResult;
import cn.rongcloud.im.model.QuietHours;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;

public class NewMessageViewModel extends AndroidViewModel {

    private IMManager imManager;
    private SingleSourceLiveData<Resource<Boolean>> removeNotifiQuietHoursResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<QuietHours>> setNotifiQuietHoursResult = new SingleSourceLiveData<>();
    private SingleSourceMapLiveData<Boolean, Boolean> remindStatus;
    private MediatorLiveData<QuietHours> donotDistrabStatus = new MediatorLiveData<>();
    private SingleSourceLiveData<Resource<Void>> setReceivePokeMsgStatusResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<GetPokeResult>> getReceivePokeMsgStatusResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Boolean>> getPushNotifyDetailResult = new SingleSourceLiveData<>();

    private UserTask userTask;

    public NewMessageViewModel(@NonNull Application application) {
        super(application);
        imManager = IMManager.getInstance();
        userTask = new UserTask(application);
        remindStatus = new SingleSourceMapLiveData<>(new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean input) {
                setRemindStatus(input);
                return input;
            }
        });

        remindStatus.setValue(imManager.getRemindStatus());
        donotDistrabStatus.setValue(imManager.getNotifiQuietHours());
        getPushNotifyDetailResult.setSource(imManager.getPushDetailContentStatus());
    }

    /**
     * 移除通知免打扰结果
     */
    public LiveData<Resource<Boolean>> getRemoveNotifiQuietHoursResult() {
        return removeNotifiQuietHoursResult;
    }

    /**
     * 设置通知免打扰时间结果
     */
    public LiveData<Resource<QuietHours>> getSetNotifiQuietHoursResult() {
        return setNotifiQuietHoursResult;
    }

    /**
     * 获取消息接受提示设置
     *
     * @return
     */
    public LiveData<Boolean> getRemindStatus() {
        return remindStatus;
    }

    /**
     * 免打扰状态
     *
     * @return
     */
    public LiveData<QuietHours> getDonotDistrabStatus() {
        return donotDistrabStatus;
    }


    /**
     * 设置新消息提醒状态
     *
     * @param status
     */
    public void setRemindStatus(boolean status) {
        imManager.setRemindStatus(status);
    }

    /**
     * 移除消息免打扰
     */
    public void removeNotificationQuietHours() {
        removeNotifiQuietHoursResult.setSource(imManager.removeNotificationQuietHours());
    }

    /**
     * 设置通知免打扰时间。
     *
     * @param startTime   起始时间 格式 HH:MM:SS。
     * @param spanMinutes 间隔分钟数大于 0 小于 1440。
     */
    public void setNotificationQuietHours(String startTime, int spanMinutes) {
        donotDistrabStatus.addSource(setNotifiQuietHoursResult, new Observer<Resource<QuietHours>>() {
            @Override
            public void onChanged(Resource<QuietHours> resource) {
                donotDistrabStatus.removeSource(setNotifiQuietHoursResult);
                if (resource.status == Status.SUCCESS) {
                    donotDistrabStatus.setValue(resource.data);
                }
            }
        });
        setNotifiQuietHoursResult.setSource(imManager.setNotificationQuietHours(startTime, spanMinutes, true));
    }

    /**
     * 设置接受戳一下消息
     */
    public void setReceivePokeMessageStatus(boolean isReceive) {
        setReceivePokeMsgStatusResult.setSource(userTask.setReceivePokeMessageState(isReceive));
    }

    /**
     * 获取设置接受戳一下消息结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getSetReceivePokeMessageStatusResult() {
        return setReceivePokeMsgStatusResult;
    }

    /**
     * 请求获取接受戳一下消息状态
     */
    public void requestReceivePokeMessageStatus() {
        getReceivePokeMsgStatusResult.setSource(userTask.getReceivePokeMessageState());
    }

    /**
     * 获取接受戳一下消息状态结果
     *
     * @return
     */
    public SingleSourceLiveData<Resource<GetPokeResult>> getReceivePokeMsgStatusResult() {
        return getReceivePokeMsgStatusResult;
    }

    /**
     * 设置推送消息通知是否显示详细内容
     *
     * @param isDetail 是否显示详细的通知消息内容。
     */
    public void setPushMsgDetailStatus(boolean isDetail) {
        getPushNotifyDetailResult.setSource(imManager.setPushDetailContentStatus(isDetail));
    }

    /**
     * 获取推送消息通知详细详细状态
     *
     * @return 当前是否显示消息通知详情状态。
     */
    public LiveData<Resource<Boolean>> getPushMsgDetailStatus() {
        return getPushNotifyDetailResult;
    }

}