package cn.rongcloud.im.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class ForwardDialog extends CommonDialog {

    private ArrayList<GroupEntity> groupList;
    private ArrayList<FriendShipInfo> friendList;
    private ArrayList<Message> messageList;

    @Override
    protected View onCreateContentView() {
        View view = View.inflate(getContext(), R.layout.dialog_forward, null);
        View multiLayout = view.findViewById(R.id.hsv_container);
        LinearLayout multiContainer = view.findViewById(R.id.ll_selected_contact_container);
        UserInfoItemView selectSingleUiv = view.findViewById(R.id.uiv_selected_info);
        TextView messageTv = view.findViewById(R.id.tv_message);

        Bundle expandParams = getExpandParams();
        if (expandParams != null) {
            messageList = expandParams.getParcelableArrayList(IntentExtra.FORWARD_MESSAGE_LIST);
            groupList = expandParams.getParcelableArrayList(IntentExtra.GROUP_LIST);
            friendList = expandParams.getParcelableArrayList(IntentExtra.FRIEND_LIST);
        }

        int groupSize = groupList == null ? 0 : groupList.size();
        int friendSize = friendList == null ? 0 : friendList.size();

        if (groupSize + friendSize == 1) {
            multiLayout.setVisibility(View.GONE);
            selectSingleUiv.setVisibility(View.VISIBLE);
            if (groupList != null && groupList.size() > 0) {
                final GroupEntity groupEntity = groupList.get(0);
                selectSingleUiv.setName(groupEntity.getName() + " (" + groupEntity.getMemberCount() + ") ");
                ImageLoaderUtils.displayGroupPortraitImage(groupEntity.getPortraitUri(), selectSingleUiv.getHeaderImageView());
            }

            if (friendList != null && friendList.size() > 0) {
                final FriendShipInfo friendShipInfo = friendList.get(0);
                selectSingleUiv.setName(TextUtils.isEmpty(friendShipInfo.getDisplayName()) ?  friendShipInfo.getUser().getNickname() : friendShipInfo.getDisplayName());
                ImageLoaderUtils.displayGroupPortraitImage(friendShipInfo.getUser().getPortraitUri(), selectSingleUiv.getHeaderImageView());
            }

        } else {
            multiLayout.setVisibility(View.VISIBLE);
            selectSingleUiv.setVisibility(View.GONE);

            int width = (int) getContext().getResources().getDimension(R.dimen.seal_dialog_forward_item_portrait_width);
            int leftMargin = (int) getContext().getResources().getDimension(R.dimen.seal_dialog_forward_item_portrait_margin_left);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
            params.leftMargin = leftMargin;
            if (groupList != null) {
                for (GroupEntity groupEntity : groupList) {
                    final AsyncImageView asyncImageView = new AsyncImageView(getContext());
                    asyncImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    multiContainer.addView(asyncImageView, params);
                    ImageLoaderUtils.displayGroupPortraitImage(groupEntity.getPortraitUri(), asyncImageView);
                }
            }
            if (friendList != null) {
                for (FriendShipInfo info : friendList) {
                    final AsyncImageView asyncImageView = new AsyncImageView(getContext());
                    asyncImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    multiContainer.addView(asyncImageView, params);
                    ImageLoaderUtils.displayGroupPortraitImage(info.getUser().getPortraitUri(), asyncImageView);
                }
            }
        }

        if (messageList != null) {
            String content = "...";
            final Message message = messageList.get(0);
            final String objectName = message.getObjectName();
            if (objectName.equals("RC:TxtMsg")) {
                content = ((TextMessage) message.getContent()).getContent();
            } else if (objectName.equals("RC:VcMsg") || objectName.equals("RC:HQVCMsg")) {
                content = getString(R.string.rc_message_content_voice);
            } else if (objectName.equals("RC:FileMsg")) {
                content = getString(R.string.rc_message_content_file);

            } else if (objectName.equals("RC:ImgMsg")) {
                content = getString(R.string.rc_message_content_image);

            } else if (objectName.equals("RC:LBSMsg")) {
                content = getString(R.string.rc_message_content_location);

            } else if (objectName.equals("RC:SightMsg")) {
                content = getString(R.string.rc_message_content_sight);

            }

            if (messageList.size() > 1) {
                content = content + "...";
            }
            messageTv.setText(content);
        }

        return view;
    }


    @Override
    protected Bundle getPositiveDatas() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IntentExtra.FORWARD_MESSAGE_LIST, messageList);
        bundle.putParcelableArrayList(IntentExtra.GROUP_LIST, groupList);
        bundle.putParcelableArrayList(IntentExtra.FRIEND_LIST, friendList);
        return bundle;
    }

    public static class Builder extends CommonDialog.Builder {
        @Override
        protected CommonDialog getCurrentDialog() {
            return new ForwardDialog();
        }
    }
}
