package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Locale;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.TypingInfo;
import cn.rongcloud.im.ui.fragment.ConversationFragmentEx;
import cn.rongcloud.im.ui.view.AnnouceView;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.viewmodel.ConversationViewModel;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongKitIntent;
import io.rong.imlib.model.Conversation;

/**
 * 会话页面
 */
public class ConversationActivity extends TitleBaseActivity {

    private String TAG = ConversationActivity.class.getSimpleName();
    private ConversationFragmentEx fragment;
    private AnnouceView annouceView;
    private ConversationViewModel conversationViewModel;
    private String title;
    /**
     * 对方id
     */
    private String targetId;
    /**
     * 会话类型
     */
    private Conversation.ConversationType conversationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_conversation);

        // 没有intent 的则直接返回
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            finish();
            return;
        }

        targetId = intent.getData().getQueryParameter("targetId");
        conversationType = Conversation.ConversationType.valueOf(intent.getData()
                .getLastPathSegment().toUpperCase(Locale.US));
        title = intent.getData().getQueryParameter("title");

        initView();
        initViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTitleStr(targetId, conversationType, title);
    }

    private void initViewModel() {
        conversationViewModel = ViewModelProviders.of(this, new ConversationViewModel.Factory(targetId, conversationType, title, this.getApplication())).get(ConversationViewModel.class);

        conversationViewModel.getTitleStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String title) {
                if (TextUtils.isEmpty(title)) {
                    if (conversationType == null) {
                        return;
                    }
                    int titleResId;
                    if (conversationType.equals(Conversation.ConversationType.DISCUSSION)) {
                        titleResId = R.string.seal_conversation_title_discussion_group;
                    } else if (conversationType.equals(Conversation.ConversationType.SYSTEM)) {
                        titleResId = R.string.seal_conversation_title_system;
                    } else if (conversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
                        titleResId = R.string.seal_conversation_title_feedback;
                    } else {
                        titleResId = R.string.seal_conversation_title_defult;
                    }
                    getTitleBar().setTitle(titleResId);

                } else {
                    getTitleBar().setTitle(title);
                }
            }
        });

        // 正在输入状态
        conversationViewModel.getTypingStatusInfo().observe(this, new Observer<TypingInfo>() {
            @Override
            public void onChanged(TypingInfo typingInfo) {
                if (typingInfo == null) {
                    return;
                }

                if (typingInfo.conversationType == conversationType && typingInfo.targetId.equals(targetId)) {
                    if (typingInfo.typingList == null) {
                        getTitleBar().setType(SealTitleBar.Type.NORMAL);
                    } else {
                        TypingInfo.Typing typing = typingInfo.typingList.get(typingInfo.typingList.size() - 1);
                        getTitleBar().setType(SealTitleBar.Type.TYPING);
                        if (typing.type == TypingInfo.Typing.Type.text) {
                            getTitleBar().setTyping(R.string.seal_conversation_remote_side_is_typing);
                        } else if (typing.type == TypingInfo.Typing.Type.voice) {
                            getTitleBar().setTyping(R.string.seal_conversation_remote_side_speaking);
                        }
                    }
                }
            }
        });

        // 群 @ 跳转
        conversationViewModel.getGroupAt().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                // 跳转选择界面
                Intent intent = new Intent(RongContext.getInstance(), MemberMentionedExActivity.class);
                intent.putExtra("conversationType", conversationType.getValue());
                intent.putExtra("targetId", targetId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    /**
     * 获取 title
     * @param targetId
     * @param conversationType
     * @param title
     */
    private void getTitleStr(String targetId, Conversation.ConversationType conversationType, String title) {
        if (conversationViewModel != null) {
            conversationViewModel.getTitleByConversation(targetId, conversationType, title);
        }
    }


    private void initView() {
        initTitleBar(conversationType, targetId);
        initAnnouceView();
        initConversationFragment();
    }

    private void initConversationFragment() {
        /**
         * 加载会话界面 。 ConversationFragmentEx 继承自 ConversationFragment
         */
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentByTag(ConversationFragmentEx.class.getCanonicalName());
        if(existFragment != null){
            fragment = (ConversationFragmentEx)existFragment;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }else {
            fragment = new ConversationFragmentEx();
            // 自定义的服务才会有通知监听
            if (conversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
                // 设置通知监听
                fragment.setOnShowAnnounceBarListener(new ConversationFragmentEx.OnShowAnnounceListener() {
                    @Override
                    public void onShowAnnounceView(String announceMsg, final String announceUrl) {
                        annouceView.setVisibility(View.VISIBLE);
                        annouceView.setAnnounce(announceMsg, announceUrl);
                    }
                });
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.rong_content, fragment, ConversationFragmentEx.class.getCanonicalName());
            transaction.commitAllowingStateLoss();
        }

    }

    /**
     * 通知布局
     */
    private void initAnnouceView() {
        // 初始化通知布局
        annouceView = findViewById(R.id.view_annouce);
        annouceView.setVisibility(View.GONE);
        annouceView.setOnAnnounceClickListener(new AnnouceView.OnAnnounceClickListener() {
            @Override
            public void onClick(View v, String url) {
                String str = url.toLowerCase();
                if (!TextUtils.isEmpty(str)) {
                    if (!str.startsWith("http") && !str.startsWith("https")) {
                        str = "http://" + str;
                    }
                    Intent intent = new Intent(RongKitIntent.RONG_INTENT_ACTION_WEBVIEW);
                    intent.setPackage(v.getContext().getPackageName());
                    intent.putExtra("url", str);
                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    /**
     * 初始化 title
     *
     * @param conversationType
     * @param targetId
     */
    private void initTitleBar(Conversation.ConversationType conversationType, String targetId) {
        // title 布局设置
        // 左边返回按钮
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment != null && !fragment.onBackPressed()) {
                    if (fragment.isLocationSharing()) {
                        fragment.showQuitLocationSharingDialog(ConversationActivity.this);
                        return;
                    }
                    hintKbTwo();
                }
                finish();
            }
        });


        getTitleBar().getBtnRight().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDetailActivity(conversationType, targetId);
            }
        });

        if (conversationType.equals(Conversation.ConversationType.GROUP)) {
            getTitleBar().getBtnRight().setImageDrawable(getResources().getDrawable(R.drawable.seal_detail_group));
        } else if (conversationType.equals(Conversation.ConversationType.PRIVATE)
                || conversationType.equals(Conversation.ConversationType.PUBLIC_SERVICE)
                || conversationType.equals(Conversation.ConversationType.APP_PUBLIC_SERVICE)
                || conversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            getTitleBar().getBtnRight().setImageDrawable(getResources().getDrawable(R.drawable.seal_detail_single));
        } else {
            getTitleBar().getBtnRight().setVisibility(View.GONE);
        }
    }

    /**
     * 根据 targetid 和 ConversationType 进入到设置页面
     */
    private void toDetailActivity(Conversation.ConversationType conversationType, String targetId) {

        if (conversationType == Conversation.ConversationType.PUBLIC_SERVICE
                || conversationType == Conversation.ConversationType.APP_PUBLIC_SERVICE) {

            RongIM.getInstance().startPublicServiceProfile(this, conversationType, targetId);
        } else if (conversationType == Conversation.ConversationType.PRIVATE) {
            Intent intent = new Intent(this, PrivateChatSettingActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, Conversation.ConversationType.PRIVATE);
            startActivity(intent);
        } else if (conversationType == Conversation.ConversationType.GROUP) {
            Intent intent = new Intent(this, GroupDetailActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, Conversation.ConversationType.GROUP);
            startActivity(intent);
        } else if (conversationType == Conversation.ConversationType.DISCUSSION) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (fragment != null && !fragment.onBackPressed()) {
                if (fragment.isLocationSharing()) {
                    fragment.showQuitLocationSharingDialog(this);
                    return true;
                }
                finish();
            }
        }
        return false;
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void clearAllFragmentExistBeforeCreate() {
    }
}
