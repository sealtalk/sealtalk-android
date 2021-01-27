package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;

public class SelectSingleViewModel extends SelectBaseViewModel {
    private static final String TAG = "SelectSingleViewModel";

    public SelectSingleViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void onItemClicked(CheckableContactModel checkableContactModel) {
        Log.i(TAG, "onItemClicked");
        switch (checkableContactModel.getCheckType()) {
            case DISABLE:
                //不可选 do nothind
                break;
            case CHECKED:
                checkableContactModel.setCheckType(CheckType.NONE);
                removeFromCheckedList(checkableContactModel);
                break;
            case NONE:
                cancelAllCheck();
                checkableContactModel.setCheckType(CheckType.CHECKED);
                addToCheckedList(checkableContactModel);
                break;
            default:
                break;

        }
    }
}
