package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.ui.adapter.models.PublicServiceModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.PublicServiceProfileList;

public class PublicServiceViewModel extends AndroidViewModel {

    private MutableLiveData<List<ContactModel>> publicService;

    public PublicServiceViewModel(@NonNull Application application) {
        super(application);
        publicService = new MutableLiveData();
    }

    public void loadPublicServices() {
        RongIM.getInstance().getPublicServiceList(new RongIMClient.ResultCallback<PublicServiceProfileList>() {
            @Override
            public void onSuccess(PublicServiceProfileList infoList) {
                for (PublicServiceProfile info : infoList.getPublicServiceData()) {
                    RongUserInfoManager.getInstance().setPublicServiceProfile(info);
                }
                List<PublicServiceProfile> publicServiceProfiles = infoList.getPublicServiceData();
                publicService.postValue(convert(publicServiceProfiles));
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });
    }

    public void searchPublicServices(String match) {
        RongIM.getInstance().searchPublicService(RongIMClient.SearchType.FUZZY, match, new RongIMClient.ResultCallback<PublicServiceProfileList>() {

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }

            @Override
            public void onSuccess(PublicServiceProfileList list) {
                publicService.postValue(convert(list.getPublicServiceData()));
            }
        });
    }

    private List<ContactModel> convert(List<PublicServiceProfile> input) {
        List<ContactModel> out = new ArrayList<>();
        for (PublicServiceProfile profile : input) {
            out.add(new PublicServiceModel(profile, R.layout.seal_public_service_item));
        }
        return out;
    }

    public MutableLiveData<List<ContactModel>> getPublicService() {
        return publicService;
    }
}
