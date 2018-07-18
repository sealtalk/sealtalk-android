package cn.rongcloud.im;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import io.rong.common.RLog;

public class GroupMemberEngine implements OnDataListener {
    private static final String TAG = "GroupMemberEngine";

    private Context context;

    private IGroupMembersCallback groupMembersCallback;

    private static final int REQUEST_GROUP_MEMBER = 4235;

    public GroupMemberEngine(Context context) {
        this.context = context;
    }

    public void startEngine(String groupId, IGroupMembersCallback callback) {
        this.groupMembersCallback = callback;
        AsyncTaskManager.getInstance(context).request(groupId, REQUEST_GROUP_MEMBER, this);
    }

    @Override
    public Object doInBackground(int requestCode, String groupId) throws HttpException {
        return new SealAction(context).getGroupMember(groupId);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            GetGroupMemberResponse response = (GetGroupMemberResponse) result;
            ArrayList<String> memberList = new ArrayList<>();
            if (response.getCode() == 200) {
                List<GetGroupMemberResponse.ResultEntity> resultEntityList = response.getResult();
                for (GetGroupMemberResponse.ResultEntity r : resultEntityList) {
                    memberList.add(r.getUser().getId());
                }
            }
            if (groupMembersCallback != null) {
                groupMembersCallback.onResult(memberList);
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        RLog.d(TAG, "onFailure state = " + state);
    }

    public interface IGroupMembersCallback {
        void onResult(ArrayList<String> members);
    }
}
