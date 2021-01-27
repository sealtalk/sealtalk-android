package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.io.IOException;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.VibratorUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UserDetailViewModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;

/**
 * 戳一下消息邀请聊天界面
 */
public class PokeInviteChatActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "PokeInviteChatActivity";
    /**
     * 来戳一下消息时震动规则
     */
    private final long[] VIBRATOR_PATTERN = new long[]{1000, 1000};

    /**
     * 默认关闭戳一下邀请界面的时间
     */
    private final long TIME_TO_FINISH_POKE_INVITE = 60 * 1000;
    /**
     * 邀请人 id
     */
    private String fromId;
    /**
     * 邀请的会话类型
     */
    private Conversation.ConversationType conversationType;
    /**
     * 邀请到目标的 targetId, 群组时为群组 id，个人时即为发送人 id
     */
    private String targetId;
    /**
     * 戳一下消息内容
     */
    private String pokeMessage;

    private TextView fromUserNameTv;
    private String targetUserName;
    private String groupName;
    private SelectableRoundedImageView fromUserAvatarIv;

    private MediaPlayer mediaPlayer;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.poke_activity_show_poke_message);

        Intent intent = getIntent();
        fromId = intent.getStringExtra(IntentExtra.START_FROM_ID);
        targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        pokeMessage = intent.getStringExtra(IntentExtra.STR_POKE_MESSAGE);
        conversationType = (Conversation.ConversationType) intent.getSerializableExtra(IntentExtra.SERIA_CONVERSATION_TYPE);
        groupName = intent.getStringExtra(IntentExtra.STR_GROUP_NAME);

        handler  = new Handler();
        // 当一定时间后关闭戳一下界面
        handler.postDelayed(timeToDismissRunnable, TIME_TO_FINISH_POKE_INVITE);

        initView();
        initViewModel();
        startNoticeAndVibrator();
    }

    private void initView() {
        fromUserNameTv = findViewById(R.id.poke_tv_invite_name);
        TextView pokeMessageTv = findViewById(R.id.poke_tv_poke_message);
        fromUserAvatarIv = findViewById(R.id.poke_siv_user_avatar);
        TextView groupNameTv = findViewById(R.id.poke_tv_invite_group_name);
        // 设置戳一下消息内容
        pokeMessageTv.setText(pokeMessage);

        // 设置群组名称
        if (!TextUtils.isEmpty(groupName)) {
            groupNameTv.setText(groupName);
            groupNameTv.setVisibility(View.VISIBLE);
        }

        // 设置忽略按钮监听
        findViewById(R.id.poke_ll_poke_ignore_container).setOnClickListener(this);
        // 设置进入聊天按钮监听
        findViewById(R.id.poke_ll_poke_start_chat_container).setOnClickListener(this);

        // 设置宽度为屏宽, 靠近屏幕底部。
        Window win = getWindow();

        WindowManager.LayoutParams params = win.getAttributes();
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        win.setAttributes(params);
    }

    private void initViewModel() {
        UserDetailViewModel userDetailViewModel =
                ViewModelProviders.of(this, new UserDetailViewModel.Factory(getApplication(), fromId)).get(UserDetailViewModel.class);

        // 获取邀请人信息
        userDetailViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> userInfoResource) {
                UserInfo data = userInfoResource.data;
                if (data != null) {
                    updateUserInfo(data);
                }
            }
        });
    }

    /**
     * 开启提示音和震动
     */
    private void startNoticeAndVibrator() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor noticeMusic = getResources().openRawResourceFd(R.raw.music_poke_msg_incoming);
            try {
                mediaPlayer.setDataSource(noticeMusic.getFileDescriptor(), noticeMusic.getStartOffset(), noticeMusic.getLength());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (mp != null) {
                            mp.setLooping(true);
                            mp.start();
                        }
                    }
                });
            } catch (IOException e) {
                SLog.e(TAG, "startNoticeAndVibrator", e);
            }
        } else {
            mediaPlayer.reset();
            mediaPlayer.prepareAsync();
        }

        // 启用震动
        VibratorUtils.startVibrator(this, VIBRATOR_PATTERN, 0);
    }

    /**
     * 停止提示音乐和震动
     */
    private void stopNoticeAndVibrator() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            }catch (IllegalStateException e){
                SLog.e(TAG, "stopNoticeAndVibrator", e);
            }
        }

        VibratorUtils.cancelVibrator(this);
    }

    /**
     * 倒计时退出戳一下显示
     */
    private Runnable timeToDismissRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    /**
     * 更新邀请人用户信息
     *
     * @param userInfo
     */
    private void updateUserInfo(UserInfo userInfo) {
        String displayName;
        String userAlias = userInfo.getAlias();
        if (!TextUtils.isEmpty(userAlias)) {
            displayName = userInfo.getAlias();
        } else {
            displayName = userInfo.getName();
        }
        // 保存用于聊天时的标题
        targetUserName = displayName;

        fromUserNameTv.setText(displayName);
        ImageLoaderUtils.displayUserPortraitImage(userInfo.getPortraitUri(), fromUserAvatarIv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.poke_ll_poke_ignore_container:
                finish();
                break;
            case R.id.poke_ll_poke_start_chat_container:
                String title = "";
                if (conversationType == Conversation.ConversationType.GROUP) {
                    Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(targetId);
                    title = groupInfo.getName();
                } else if (conversationType == Conversation.ConversationType.PRIVATE) {
                    if (TextUtils.isEmpty(targetUserName)) {
                        io.rong.imlib.model.UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
                        title = userInfo.getName();
                    } else {
                        title = targetUserName;
                    }
                }
                RongIM.getInstance().startConversation(PokeInviteChatActivity.this, conversationType, targetId, title);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void finish() {
        super.finish();

        handler.removeCallbacks(timeToDismissRunnable);
        stopNoticeAndVibrator();
    }
}
