package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.SecurityStatusResult;
import cn.rongcloud.im.model.SecurityVerifyResult;
import cn.rongcloud.im.task.SecurityTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

public class SecurityViewModel extends AndroidViewModel {

    SecurityTask securityTask;

    private SingleSourceLiveData<Resource<SecurityStatusResult>> securityStatus =
            new SingleSourceLiveData<>();

    private SingleSourceLiveData<Resource<SecurityVerifyResult>> securityVerify =
            new SingleSourceLiveData<>();

    public SecurityViewModel(@NonNull Application application) {
        super(application);
        securityTask = new SecurityTask(application);

        securityStatus.setSource(securityTask.querySecurityStatus());
    }

    public LiveData<Resource<SecurityStatusResult>> getSecurityStatus() {
        return securityStatus;
    }

    public LiveData<Resource<SecurityVerifyResult>> getSecurityVerify() {
        return securityVerify;
    }

    public void doSecurityVerify(String deviceId) {
        new Handler(Looper.getMainLooper())
                .post(() -> securityVerify.setSource(securityTask.doSecurityVerify(deviceId)));
    }
}
