package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.utils.log.SLog;

public class SelectMultiViewModel extends SelectBaseViewModel {
    private static final String TAG = "SelectMultiViewModel";

    public SelectMultiViewModel(@NonNull Application application) {
        super(application);
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
                removeFromCheckedList(checkableContactModel);
                break;
            case NONE:
                checkableContactModel.setCheckType(CheckType.CHECKED);
                addToCheckedList(checkableContactModel);
                break;
            default:
                break;
        }
    }
}
