package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.QuietHours;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;

public class NewMessageViewModel extends AndroidViewModel {

    private IMManager imManager;
    private SingleSourceLiveData<Resource<Boolean>> removeNotifiQuietHoursResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<QuietHours>> setNotifiQuietHoursResult = new SingleSourceLiveData<>();
    private SingleSourceMapLiveData<Boolean, Boolean> remindStatus ;
    private MediatorLiveData<QuietHours> donotDistrabStatus = new MediatorLiveData<>();

    public NewMessageViewModel(@NonNull Application application) {
        super(application);
        imManager = IMManager.getInstance();
        remindStatus = new SingleSourceMapLiveData<>(new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean input) {
                setRemindStatus(input);
                return input;
            }
        });

        remindStatus.setValue(imManager.getRemindStatus());
        donotDistrabStatus.setValue(imManager.getNotifiQuietHours());
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

}
