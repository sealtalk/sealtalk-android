package cn.rongcloud.contactcard.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.util.List;

import cn.rongcloud.contactcard.R;
import cn.rongcloud.contactcard.message.ContactMessage;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

/**
 * Created by Beyond on 2016/11/24.
 */

public class ContactDetailActivity extends RongBaseNoActionbarActivity {

    private AsyncImageView mTargetPortrait;
    private TextView mTargetName;
    private TextView mContactName;
    private EditText mMessage;
    private TextView mSend;
    private TextView mCancel;
    private ImageView mArrow;
    private ViewAnimator mViewAnimator;
    private GridView mGridView;

    private Conversation.ConversationType mConversationType;
    private String mTargetId;
    private UserInfo mContactFriend;
    private Group group;
    private List<UserInfo> mGroupMember;
    private boolean mGroupMemberShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rc_ac_contact_detail);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView() {
        mTargetPortrait = (AsyncImageView) findViewById(R.id.target_portrait);
        mTargetName = (TextView) findViewById(R.id.target_name);
        mArrow = (ImageView) findViewById(R.id.target_group_arrow);
        mContactName = (TextView) findViewById(R.id.contact_name);
        mMessage = (EditText) findViewById(R.id.message);
        mSend = (TextView) findViewById(R.id.send);
        mCancel = (TextView) findViewById(R.id.cancel);
        mViewAnimator = (ViewAnimator) findViewById(R.id.va_detail);
        mGridView = (GridView) findViewById(R.id.gridview);

        mCancel.requestFocus();
        this.setFinishOnTouchOutside(false);
    }

    private void initData() {
        mTargetId = getIntent().getStringExtra("targetId");
        mConversationType = (Conversation.ConversationType) getIntent().getSerializableExtra("conversationType");
        mContactFriend = getIntent().getParcelableExtra("contact");

        switch (mConversationType) {
            case PRIVATE:
                UserInfo mine = RongUserInfoManager.getInstance().getUserInfo(mTargetId);
                onEventMainThread(mine);
                break;
            case GROUP:
                group = RongUserInfoManager.getInstance().getGroupInfo(mTargetId);
                onEventMainThread(group);

                RongIM.IGroupMembersProvider groupMembersProvider = RongMentionManager.getInstance().getGroupMembersProvider();
                if (groupMembersProvider != null) {
                    groupMembersProvider.getGroupMembers(mTargetId, new RongIM.IGroupMemberCallback() {
                        @Override
                        public void onGetGroupMembersResult(final List<UserInfo> members) {
                            mGroupMember = members;
                            if (mGroupMember != null) {
                                if (group != null) {
                                    mTargetName.setText(String.format(getResources().getString(R.string.rc_contact_group_member_count),
                                            group.getName(), mGroupMember.size()));
                                }
                                mGridView.setAdapter(new GridAdapter(ContactDetailActivity.this, mGroupMember));
                            }
                        }
                    });
                    mArrow.setVisibility(View.VISIBLE);
                }

                mArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mGroupMemberShown) {
                            hideInputKeyBoard();
                            mViewAnimator.setDisplayedChild(1);
                            if (mGroupMember != null && mGroupMember.size() > 4)
                                mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RongUtils.dip2px(160)));
                            else
                                mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RongUtils.dip2px(90)));

                            ObjectAnimator animator = ObjectAnimator.ofFloat(mArrow, "rotation", 0f, 180f);
                            animator.setDuration(500);
                            animator.start();
                            mGroupMemberShown = true;
                        } else {
                            mViewAnimator.setDisplayedChild(0);
                            mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                            ObjectAnimator animator = ObjectAnimator.ofFloat(mArrow, "rotation", 180f, 0f);
                            animator.setDuration(500);
                            animator.start();
                            mGroupMemberShown = false;
                        }
                    }
                });
                break;
            default:
                break;
        }

        if (mContactFriend != null)
            mContactName.setText(getString(R.string.rc_plugins_contact) + ": " + mContactFriend.getName());

        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    int start = mMessage.getSelectionStart();
                    int end = mMessage.getSelectionEnd();
                    mMessage.removeTextChangedListener(this);
                    mMessage.setText(AndroidEmoji.ensure(s.toString()));
                    mMessage.addTextChangedListener(this);
                    mMessage.setSelection(start, end);
                }
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo sendUserInfo = RongUserInfoManager.getInstance().
                        getUserInfo(RongIMClient.getInstance().getCurrentUserId());
                String sendUserName = sendUserInfo == null ? "" : sendUserInfo.getName();
                ContactMessage contactMessage = ContactMessage.obtain(mContactFriend.getUserId(),
                        mContactFriend.getName(), mContactFriend.getPortraitUri().toString(),
                        RongIMClient.getInstance().getCurrentUserId(), sendUserName, "");
                String pushContent = String.format(RongContext.getInstance().getResources().getString(R.string.rc_recommend_clause_to_me), sendUserName, contactMessage.getName());
                RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, contactMessage),
                        pushContent, null, new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(Message message) {

                            }

                            @Override
                            public void onSuccess(Message message) {

                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                            }
                        });

                String message = mMessage.getText().toString().trim();
                if (!("".equals(message))) {
                    TextMessage mTextMessage = TextMessage.obtain(message);
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, mTextMessage), null, null,
                            new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }
                            });
                } else {
                    hideInputKeyBoard();
                }
                finish();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputKeyBoard();
                finish();
            }
        });
    }

    public void onEventMainThread(UserInfo mine) {
        if (mine != null) {
            if (mine.getPortraitUri() != null)
                mTargetPortrait.setAvatar(mine.getPortraitUri());
            if (mine.getName() != null)
                mTargetName.setText(mine.getName());
        }
    }

    public void onEventMainThread(Group group) {
        if (group != null) {
            this.group = group;
            if (group.getPortraitUri() != null)
                mTargetPortrait.setAvatar(group.getPortraitUri());
            if (group.getName() != null) {
                if (mGroupMember != null && mGroupMember.size() > 0) {
                    mTargetName.setText(String.format(getResources().getString(R.string.rc_contact_group_member_count),
                            group.getName(), mGroupMember.size()));
                } else {
                    mTargetName.setText(group.getName());
                }
            }
        }
    }

    private static class GridAdapter extends BaseAdapter {

        private List<UserInfo> list;
        Context context;

        GridAdapter(Context context, List<UserInfo> list) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.rc_gridview_item_contact_group_members, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.portrait = (AsyncImageView) convertView.findViewById(R.id.iv_avatar);
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_username);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            UserInfo member = list.get(position);
            if (member != null) {
                viewHolder.portrait.setAvatar(member.getPortraitUri());
                viewHolder.name.setText(member.getName());
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class ViewHolder {
        AsyncImageView portrait;
        TextView name;
    }

    private void hideInputKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mMessage.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {

    }
}
