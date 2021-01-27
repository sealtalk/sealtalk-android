package cn.rongcloud.im.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.SelectGroupMemberAllowEmptyActivity;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;

/**
 * 发送戳一下消息对话框
 */
public class SendPokeDialog extends CommonDialog {
    /**
     * 发送名称最长显示长度
     */
    private static final int SEND_TO_NAME_MAX_DISPLAY_LENGTH = 10;
    /**
     * 对话框宽度
     */
    private static final int POKE_DIALOG_WIDTH_DP = 320;
    /**
     * Request Code:选择发送人
     */
    private static final int REQUEST_SELECT_SEND_TO = 1000;

    private TextView sendToTitleTv;
    private EditText sendMessageEt;
    private TextView sendToNameTv;
    private boolean isMultiSelect;
    private OnSendPokeClickedListener sendPokeListener;
    private ArrayList<String> targetUserList;
    private String targetName;
    private String targetId;

    @Override
    protected View onCreateContentView(ViewGroup container) {
        Context context = getContext();
        View contentView = LayoutInflater.from(context).inflate(R.layout.im_plugin_dialog_send_poke_msg, container, false);
        sendMessageEt = contentView.findViewById(R.id.send_poke_et_send_msg);
        sendToTitleTv = contentView.findViewById(R.id.send_poke_tv_send_to);
        LinearLayout sentToContainerLl = contentView.findViewById(R.id.poke_ll_poke_select_send_to);
        TextView sendToLabelTv = contentView.findViewById(R.id.poke_tv_select_send_to_label);
        sendToNameTv = contentView.findViewById(R.id.poke_tv_select_send_to_name);

        // 设置戳一下最长文本数
        sendMessageEt.addTextChangedListener(new TextWatcher() {
            private String lastTxt = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentTxt = sendMessageEt.getText().toString();
                if(currentTxt.length() > Constant.POKE_MESSAGE_MEX_LENGTH){
                    int selectionStart = sendMessageEt.getSelectionStart() - 1; // 向后退 1 位，减 1
                    sendMessageEt.setText(lastTxt);
                    if(selectionStart <= lastTxt.length()) {
                        sendMessageEt.setSelection(selectionStart);
                    } else {
                        sendMessageEt.setSelection(lastTxt.length());
                    }
                    ToastUtils.showToast(R.string.poke_message_max_allow_length);
                } else {
                    lastTxt = currentTxt;
                }
            }
        });

        // 设置发送给目标用户的文本
        if (!TextUtils.isEmpty(targetName)) {
            String targetContent = context.getString(R.string.imt_plugin_poke_send_to_format, targetName);
            sendToTitleTv.setText(targetContent);
        }

        if (isMultiSelect) {
            updateSendToName(getString(R.string.im_plugin_poke_multi_all_in_group));
            sentToContainerLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SelectGroupMemberAllowEmptyActivity.class);
                    intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
                    intent.putExtra(IntentExtra.LIST_EXCLUDE_ID_LIST, new ArrayList<String>() {{
                        add(IMManager.getInstance().getCurrentId());
                    }});
                    if (targetUserList != null && targetUserList.size() > 0) {
                        intent.putStringArrayListExtra(IntentExtra.LIST_ALREADY_CHECKED_USER_ID_LIST, targetUserList);
                    }
                    startActivityForResult(intent, REQUEST_SELECT_SEND_TO);
                }
            });
        } else {
            sentToContainerLl.setVisibility(View.INVISIBLE);
            sendToLabelTv.setVisibility(View.GONE);
            sendToNameTv.setVisibility(View.GONE);
        }
        return contentView;
    }

    @Override
    protected boolean onPositiveClick() {
        if (sendPokeListener != null) {
            String pokeMsg = sendMessageEt.getText().toString();
            if (TextUtils.isEmpty(pokeMsg)) {
                pokeMsg = getString(R.string.im_plugin_poke_message_default);
            }
            sendPokeListener.onSendPokeClicked(isMultiSelect,
                    targetUserList != null && targetUserList.size() > 0 ? targetUserList.toArray(new String[0]) : null,
                    pokeMsg);
        }
        return true;
    }


    /**
     * 设置发送目标人名称
     *
     * @param targetName
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * 设置选择发送人时的群组 id
     *
     * @param targetId
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * 设置发送戳一下消息点击事件
     *
     * @param listener
     */
    public void setOnSendPokeClickedListener(OnSendPokeClickedListener listener) {
        sendPokeListener = listener;
    }

    /**
     * 是否多选发送人
     *
     * @param isMultiSelect
     */
    public void setIsMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }


    /**
     * 更新发送人显示名称
     *
     * @param sendToName
     */
    private void updateSendToName(String sendToName) {
        Context context = getContext();
        if (context != null) {
            String targetContent = context.getString(R.string.imt_plugin_poke_send_to_format, sendToName);
            sendToTitleTv.setText(targetContent);

            String sendToDisplay;
            if (sendToName.length() > SEND_TO_NAME_MAX_DISPLAY_LENGTH) {
                sendToDisplay = sendToName.substring(0, SEND_TO_NAME_MAX_DISPLAY_LENGTH) + "...";
            } else {
                sendToDisplay = sendToName;
            }
            sendToNameTv.setText(sendToDisplay);
        }
    }

    /**
     * 设置戳一下对话框宽度
     *
     * @return
     */
    protected int getDialogWidth() {
        Context context = getContext();
        if (context != null) {
            return (int) (context.getResources().getDisplayMetrics().density * POKE_DIALOG_WIDTH_DP);
        }
        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_SEND_TO && resultCode == Activity.RESULT_OK && data != null) {
            targetUserList = data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
            if (targetUserList != null && targetUserList.size() > 0) {
                // 拼接要发送人的名称
                StringBuilder memberNameUnion = new StringBuilder();
                for (String memberId : targetUserList) {
                    String memberName = "";
                    GroupUserInfo groupUserInfo = RongUserInfoManager.getInstance().getGroupUserInfo(targetId, memberId);
                    if (groupUserInfo != null) {
                        memberName = groupUserInfo.getNickname();
                    } else {
                        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(memberId);
                        if(userInfo != null){
                            memberName = userInfo.getName();
                        }
                    }

                    if(TextUtils.isEmpty(memberName)){
                        continue;
                    }

                    memberNameUnion.append(memberName).append(",");
                }
                if (memberNameUnion.length() > 0) {
                    // 去除末尾分割符号，并刷新发送给目标的名称
                    updateSendToName(memberNameUnion.substring(0, memberNameUnion.length() - 1));
                }
            } else {
                updateSendToName(getString(R.string.im_plugin_poke_multi_all_in_group));
            }
        }
    }

    public interface OnSendPokeClickedListener {
        /**
         * 点击确认发送戳一下消息时回调
         *
         * @param isMultiSelect 是否为发送给多人
         * @param userIds       当 isMultiSelect 为 true 时，为多选的发送目标人，当为 null 为发给所有人；
         *                      当 isMultiSelect 为 false 时,一直为 null。
         * @param pokeMessage   填写的戳一下消息内容
         */
        void onSendPokeClicked(boolean isMultiSelect, String[] userIds, String pokeMessage);
    }
}
