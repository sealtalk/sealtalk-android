package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.utils.log.SLog;

public class SelectMultiViewModel extends SelectBaseViewModel {
    private static final String TAG = "SelectMultiViewModel";
    private MutableLiveData<Integer> selectedCount = new MutableLiveData<>();

    public SelectMultiViewModel(@NonNull Application application) {
        super(application);
        selectedCount.setValue(0);
    }

    @Override
    public void onItemClicked(CheckableContactModel checkableContactModel) {
        SLog.i(TAG, "onItemClicked()");
        switch (checkableContactModel.getCheckType()) {
            case DISABLE:
                //不可选 do nothing
                break;
            case CHECKED:
                checkableContactModel.setCheckType(CheckType.NONE);
                break;
            case NONE:
                checkableContactModel.setCheckType(CheckType.CHECKED);
                break;
            default:
                break;
        }
        // 记录选中数
        int size = getCheckedList().size();
        selectedCount.setValue(size);
    }

    /**
     * 获取选择用户的数量
     *
     * @return
     */
    public LiveData<Integer> getSelectedCount(){
        return selectedCount;
    }
}
