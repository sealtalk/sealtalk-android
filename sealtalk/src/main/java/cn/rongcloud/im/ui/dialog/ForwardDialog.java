package cn.rongcloud.im.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
import cn.rongcloud.im.utils.AsyncImageView;;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public class ForwardDialog extends CommonDialog {

    private ArrayList<GroupEntity> groupList;
    private ArrayList<FriendShipInfo> friendList;
    private ArrayList<Message> messageList;
    private ArrayList<Integer> messageIdList;

    @Override
    protected View onCreateContentView(ViewGroup container) {
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
            messageIdList = expandParams.getIntegerArrayList(IntentExtra.FORWARD_MESSAGE_ID_LIST);
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
                selectSingleUiv.setName(TextUtils.isEmpty(friendShipInfo.getDisplayName()) ? friendShipInfo.getUser().getNickname() : friendShipInfo.getDisplayName());
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

        if (messageList != null && messageList.size() > 0) {
            String content = "...";
            Message message = messageList.get(0);
            content = getMessageContent(message);
            if (messageList.size() > 1) {
//                content = content + "...";
                content = getString(R.string.seal_selected_contact_content,content.length());
            }
            messageTv.setText(content);
        } else if (messageIdList != null && messageIdList.size() > 0) {
            RongIMClient.getInstance().getMessage(messageIdList.get(0), new RongIMClient.ResultCallback<Message>() {
                @Override
                public void onSuccess(Message message) {
                    String content = getMessageContent(message);
                    if (messageIdList.size() > 1) {
//                        content = content + "...";
                        content = getString(R.string.seal_selected_contact_content,messageIdList.size());
                    }
                    messageTv.setText(content);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                }
            });
        }
        return view;
    }

    private String getMessageContent(Message message) {
        String content = "";
        if (message != null) {
            final MessageContent messageContent = message.getContent();
            //todo
//            if (messageContent instanceof TextMessage) {
//                content = ((TextMessage) message.getContent()).getContent();
//            } else if (messageContent instanceof VoiceMessage || messageContent instanceof HQVoiceMessage) {
//                content = getString(R.string.rc_message_content_voice);
//            } else if (messageContent instanceof FileMessage) {
//                content = getString(R.string.rc_message_content_file);
//            } else if (messageContent instanceof ImageMessage) {
//                content = getString(R.string.rc_message_content_image);
//            } else if (messageContent instanceof LocationMessage) {
//                content = getString(R.string.rc_message_content_location);
//            } else if (messageContent instanceof SightMessage) {
//                content = getString(R.string.rc_message_content_sight);
//            }
        }
        return content;
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
