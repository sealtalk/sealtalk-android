package cn.rongcloud.im.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.ui.adapter.models.UserGroupModel;
import cn.rongcloud.im.ui.adapter.viewholders.UserGroupHolder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserGroupListAdapter extends AbsSelectedAdapter<UserGroupHolder, UserGroupModel> {

    @NonNull
    @Override
    public UserGroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.select_user_group_list_item, null, false);
        return new UserGroupHolder(itemView, null);
    }

    public void setUserGroupInfoList(List<UserGroupInfo> list, int viewType) {
        setUserGroupInfoList(list, null, viewType);
    }

    public void setUserGroupInfoList(
            List<UserGroupInfo> list, HashSet<String> checkedSet, int viewType) {
        List<CheckModel> newData = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (UserGroupInfo info : list) {
                UserGroupModel model = new UserGroupModel(info, viewType);
                if (checkedSet != null && checkedSet.contains(info.userGroupId)) {
                    model.setCheckType(CheckType.CHECKED);
                }
                newData.add(model);
            }
            Log.e("sss", "onActivityResult2 :" + list.size() + " , " + list);
        }
        Log.e("sss", "onActivityResult3 :" + newData.size() + " , " + newData);
        setData(newData);
    }

    public ArrayList<UserGroupInfo> getCheckedUserGroupList() {
        ArrayList<UserGroupInfo> list = new ArrayList<>();
        for (CheckModel model : originModelList) {
            if (model.getCheckType() == CheckType.CHECKED) {
                list.add((UserGroupInfo) model.getBean());
            }
        }
        return list;
    }

    public ArrayList<UserGroupInfo> getList() {
        ArrayList<UserGroupInfo> list = new ArrayList<>();
        ArrayList<CheckModel> contactModels = originModelList;
        for (ContactModel model : contactModels) {
            list.add((UserGroupInfo) model.getBean());
        }
        return list;
    }
}
