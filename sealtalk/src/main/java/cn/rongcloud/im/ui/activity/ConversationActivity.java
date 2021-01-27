package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.event.DeleteFriendEvent;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.ScreenCaptureData;
import cn.rongcloud.im.model.ScreenCaptureResult;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.TypingInfo;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.ui.dialog.RencentPicturePopWindow;
import cn.rongcloud.im.ui.view.AnnouceView;
import cn.rongcloud.im.utils.CheckPermissionUtils;
import cn.rongcloud.im.utils.NavigationBarUtil;
import cn.rongcloud.im.utils.ScreenCaptureUtil;
import cn.rongcloud.im.utils.StatusBarUtil;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.ConversationViewModel;
import cn.rongcloud.im.viewmodel.GroupManagementViewModel;
import cn.rongcloud.im.viewmodel.PrivateChatSettingViewModel;
import io.rong.imkit.activity.RongBaseActivity;
import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.conversation.extension.RongExtensionViewModel;
import io.rong.imkit.conversation.messgelist.viewmodel.MessageViewModel;
import io.rong.imkit.manager.UnReadMessageManager;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.widget.TitleBar;
import io.rong.imlib.model.Conversation;

/**
 * 会话页面
 */
public class ConversationActivity extends RongBaseActivity implements UnReadMessageManager.IUnReadMessageObserver {

    private String TAG = ConversationActivity.class.getSimpleName();
    private ConversationFragment fragment;
    private String mTargetId;
    private Conversation.ConversationType mConversationType;
    private AnnouceView annouceView;
    private ConversationViewModel conversationViewModel;
    private RongExtensionViewModel extensionViewModel;
    private GroupManagementViewModel groupManagementViewModel;
    private PrivateChatSettingViewModel privateChatSettingViewModel;
    private String title;
    private boolean isExtensionHeightInit = false;
    private boolean isSoftKeyOpened = false;
    private boolean isClickToggle = false;
    private boolean isExtensionExpanded;
    private int extensionExpandedHeight;
    private int extensionCollapsedHeight;
    private ScreenCaptureData rencentScreenCaptureData;
    private static List<String> rencentShowIdList = new ArrayList<>();
    private RencentPicturePopWindow rencentPicturePopWindow;
    private UserConfigCache userConfigCache;
    private final int REQUEST_CODE_PERMISSION = 118;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

    private ScreenCaptureUtil screenCaptureUtil;

    /**
     * 在会话类型为群组时：是否为群主
     */
    private boolean isGroupOwner;
    /**
     * 在会话类型为群组时：是否为群管理员
     */
    private boolean isGroupManager;

    private DelayDismissHandler mHandler;
    private final int RECENTLY_POPU_DISMISS = 0x2870;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_conversation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, getResources().getColor(R.color.rc_background_main_color)); //Color.parseColor("#F5F6F9")
        }
        // 没有intent 的则直接返回
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        mTargetId = getIntent().getStringExtra(RouteUtils.TARGET_ID);
        mConversationType = Conversation.ConversationType.valueOf(getIntent().getStringExtra(RouteUtils.CONVERSATION_TYPE).toUpperCase(Locale.US));
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            title = bundle.getString(RouteUtils.TITLE);
        }
        userConfigCache = new UserConfigCache(this);
        mHandler = new DelayDismissHandler(this);
        setListenerToRootView();
        initView();
        initViewModel();
//        initScreenShotListener();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        MessageViewModel messageViewModel = ViewModelProviders.of(fragment).get(MessageViewModel.class);
        messageViewModel.IsEditStatusLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    mTitleBar.setRightVisible(false);
                } else {
                    if (mConversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)
                            || mConversationType.equals(Conversation.ConversationType.CHATROOM)) {
                        mTitleBar.setRightVisible(false);
                    } else {
                        mTitleBar.setRightVisible(true);
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (fragment != null && !fragment.onBackPressed()) {
                finish();
            }
        }
        return false;
    }

    private boolean isFirstResume = true;

    @Override
    protected void onResume() {
        super.onResume();
        getTitleStr(mTargetId, mConversationType, title);
        refreshScreenCaptureStatus();
        //设置聊天背景
        if (isFirstResume) {
            UserConfigCache configCache = new UserConfigCache(this);
            if (!TextUtils.isEmpty(configCache.getChatbgUri())) {
                try {
                    fragment.getView().findViewById(R.id.rc_refresh).setBackground(Drawable.createFromStream(getContentResolver().openInputStream(Uri.parse(configCache.getChatbgUri())), null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isFirstResume = false;
        }
        // 记录当前的会话类型和 id
        IMManager.getInstance().setLastConversationRecord(mTargetId, mConversationType);
        setOnLayoutListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (screenCaptureUtil != null) {
            screenCaptureUtil.unRegister();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (screenCaptureUtil != null) {
            screenCaptureUtil.unRegister();
        }
        // 清除会话记录
        IMManager.getInstance().clearConversationRecord(mTargetId);
        mHandler.removeCallbacksAndMessages(null);
        UnReadMessageManager.getInstance().removeObserver(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RencentPicturePopWindow.REQUEST_PICTURE && resultCode == RESULT_OK) {
            SLog.i("onActivityResult", "send" + "***" + data.getStringExtra(IntentExtra.URL) + "***" + data.getBooleanExtra(IntentExtra.ORGIN, false));
            IMManager.getInstance().sendImageMessage(mConversationType, mTargetId,
                    Collections.singletonList(Uri.parse(data.getStringExtra(IntentExtra.URL))),
                    data.getBooleanExtra(IntentExtra.ORGIN, false));
        }
    }

    private void refreshScreenCaptureStatus() {
        //私聊或者群聊才开启功能
        if (mConversationType.equals(Conversation.ConversationType.PRIVATE) || mConversationType.equals(Conversation.ConversationType.GROUP)) {
            int cacheType = userConfigCache.getScreenCaptureStatus();
            //1为开启，0为关闭
            if (cacheType == 1) {
                if (screenCaptureUtil == null) {
                    initScreenShotListener();
                } else {
                    screenCaptureUtil.register();
                }
            } else if (cacheType == 0) {
                if (screenCaptureUtil != null) {
                    screenCaptureUtil.unRegister();
                }
            }
        }
    }

    private void initScreenShotListener() {
        screenCaptureUtil = new ScreenCaptureUtil(this);
        screenCaptureUtil.setScreenShotListener(new ScreenCaptureUtil.ScreenShotListener() {
            @Override
            public void onScreenShotComplete(String data, long dateTaken) {
                SLog.d(TAG, "onScreenShotComplete===" + data);
//                rencentScreenCaptureData = null;
//                rencentScreenCaptureData = new ScreenCaptureData(data, System.currentTimeMillis());
//                if (!isExtensionExpanded) {
//                    showRencentPicturePop(extensionCollapsedHeight);
//                }
                if (mConversationType.equals(Conversation.ConversationType.PRIVATE) || mConversationType.equals(Conversation.ConversationType.GROUP)) {
                    int cacheType = userConfigCache.getScreenCaptureStatus();
                    //1为开启，0为关闭
                    if (cacheType == 0) {
                        return;
                    }
                }
                ThreadManager.getInstance().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //在主线程注册 observeForever 因为截屏时候可能使得 activity 处于 pause 状态，无法发送消息
                        LiveData<Resource<Void>> result = conversationViewModel.sendScreenShotMsg(mConversationType.getValue(), mTargetId);
                        result.observeForever(new Observer<Resource<Void>>() {
                            @Override
                            public void onChanged(Resource<Void> voidResource) {
                                if (voidResource.status == Status.SUCCESS) {
                                    result.removeObserver(this);
                                    SLog.d(TAG, "sendScreenShotMsg===Success");
                                } else if (voidResource.status == Status.ERROR) {
                                    result.removeObserver(this);
                                    SLog.d(TAG, "sendScreenShotMsg===Error");
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onFaild(Exception e) {
                // 没有权限异常，申请权限
                if (e instanceof SecurityException) {
                    CheckPermissionUtils.requestPermissions(ConversationActivity.this, permissions, REQUEST_CODE_PERMISSION);
                }
            }
        });
        screenCaptureUtil.register();
    }

    private void initViewModel() {
        conversationViewModel = ViewModelProviders.of(this, new ConversationViewModel.Factory(mTargetId, mConversationType, title, this.getApplication())).get(ConversationViewModel.class);

        conversationViewModel.getTitleStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String title) {
                if (TextUtils.isEmpty(title)) {
                    if (mConversationType == null) {
                        return;
                    }
                    int titleResId;
                    if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {
                        titleResId = R.string.seal_conversation_title_discussion_group;
                    } else if (mConversationType.equals(Conversation.ConversationType.SYSTEM)) {
                        titleResId = R.string.seal_conversation_title_system;
                    } else if (mConversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
                        titleResId = R.string.seal_conversation_title_feedback;
                    } else {
                        titleResId = R.string.seal_conversation_title_defult;
                    }
                    mTitleBar.setTitle(titleResId);

                } else {
                    mTitleBar.setTitle(title);
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
                if (typingInfo.conversationType.equals(mConversationType) && typingInfo.targetId.equals(mTargetId)) {
                    if (typingInfo.typingList == null) {
                        mTitleBar.getMiddleView().setVisibility(View.VISIBLE);
                        mTitleBar.getTypingView().setVisibility(View.GONE);
                    } else {
                        mTitleBar.getMiddleView().setVisibility(View.GONE);
                        mTitleBar.getTypingView().setVisibility(View.VISIBLE);
                        TypingInfo.Typing typing = typingInfo.typingList.get(typingInfo.typingList.size() - 1);
                        if (typing.type == TypingInfo.Typing.Type.text) {
                            mTitleBar.setTyping(R.string.seal_conversation_remote_side_is_typing);
                        } else if (typing.type == TypingInfo.Typing.Type.voice) {
                            mTitleBar.setTyping(R.string.seal_conversation_remote_side_speaking);
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
                Intent intent = new Intent(getApplicationContext(), MemberMentionedExActivity.class);
                intent.putExtra(RouteUtils.CONVERSATION_TYPE, mConversationType.getValue());
                intent.putExtra(RouteUtils.TARGET_ID, mTargetId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        conversationViewModel.getScreenCaptureStatus(mConversationType.getValue(), mTargetId).observe(this, new Observer<Resource<ScreenCaptureResult>>() {
            @Override
            public void onChanged(Resource<ScreenCaptureResult> screenCaptureResultResource) {
                if (screenCaptureResultResource.status == Status.SUCCESS) {
                    // 0 关闭 1 打开
                    //refreshScreenCaptureStatus();
                }
            }
        });

        // 判读是否为群组聊天
        if (mConversationType == Conversation.ConversationType.GROUP) {
            groupManagementViewModel = ViewModelProviders.of(this, new GroupManagementViewModel.Factory(mTargetId, getApplication())).get(GroupManagementViewModel.class);
            // 群主
            groupManagementViewModel.getGroupOwner().observe(this, new Observer<GroupMember>() {
                @Override
                public void onChanged(GroupMember groupMember) {
                    if (groupMember != null && groupMember.getUserId().equals(IMManager.getInstance().getCurrentId())) {
                        isGroupOwner = true;
                    } else {
                        isGroupOwner = false;
                    }
                }
            });

            // 群管理
            groupManagementViewModel.getGroupManagements().observe(this, new Observer<Resource<List<GroupMember>>>() {
                @Override
                public void onChanged(Resource<List<GroupMember>> resource) {
                    if (resource.data != null) {
                        boolean isManager = false;
                        for (GroupMember groupMember : resource.data) {
                            if (groupMember.getUserId().equals(IMManager.getInstance().getCurrentId())) {
                                isManager = true;
                                break;
                            }
                        }
                        isGroupManager = isManager;
                    }
                }
            });
        }

        //判断截屏通知状态
        if (mConversationType == Conversation.ConversationType.GROUP || mConversationType == Conversation.ConversationType.PRIVATE) {
            privateChatSettingViewModel = ViewModelProviders.of(this, new PrivateChatSettingViewModel.Factory(getApplication(), mTargetId, mConversationType)).get(PrivateChatSettingViewModel.class);
            privateChatSettingViewModel.getScreenCaptureStatusResult().observe(this, new Observer<Resource<ScreenCaptureResult>>() {
                @Override
                public void onChanged(Resource<ScreenCaptureResult> screenCaptureResultResource) {
                    if (screenCaptureResultResource.status == Status.SUCCESS) {
                        //0 关闭 1 打开
                        if (screenCaptureResultResource.data != null && screenCaptureResultResource.data.status == 1) {
                            //判断是否有读取文件的权限
                            if (!CheckPermissionUtils.requestPermissions(ConversationActivity.this, permissions, REQUEST_CODE_PERMISSION)) {
                                return;
                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * 获取 title
     *
     * @param mTargetId
     * @param conversationType
     * @param title
     */
    private void getTitleStr(String mTargetId, Conversation.ConversationType conversationType, String title) {
        if (conversationViewModel != null) {
            conversationViewModel.getTitleByConversation(mTargetId, conversationType, title);
        }
    }


    private void initView() {
        initTitleBar(mConversationType, mTargetId);
        initAnnouceView();
        initConversationFragment();
    }

    boolean collapsed = true;
    int originalTop = 0;
    int originalBottom = 0;
    int extensionHeight = 0;

    private void initConversationFragment() {
        /**
         * 加载会话界面 。 ConversationFragmentEx 继承自 ConversationFragment
         */
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentByTag(ConversationFragment.class.getCanonicalName());
        if (existFragment != null) {
            fragment = (ConversationFragment) existFragment;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        } else {
            fragment = new ConversationFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.rong_content, fragment, ConversationFragment.class.getCanonicalName());
            transaction.commitAllowingStateLoss();
        }
    }

    private void setOnLayoutListener() {
        if (fragment != null && fragment.getRongExtension() != null) {
            fragment.getRongExtension().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (originalTop != 0) {
                        if (originalTop > top) {
                            if (originalBottom > bottom && collapsed) {
                                collapsed = false;
                                extensionHeight = originalBottom - top;
                            } else if (collapsed) {
                                collapsed = false;
                                extensionHeight = bottom - top;
                            }
                        } else {
                            if (!collapsed) {
                                collapsed = true;
                                extensionHeight = 0;
                            }
                        }
                    }
                    if (originalTop == 0) {
                        originalTop = top;
                        originalBottom = bottom;
                    }
                    if (extensionHeight > 0) {
                        isExtensionExpanded = true;
                        //点击+号如果开启高度不等于0直接显示快捷图片框
                        if (extensionExpandedHeight == 0) {
                            extensionExpandedHeight = extensionHeight;
                            showRencentPicturePop(extensionExpandedHeight);
                        }
                    } else {
                        if (!isClickToggle) {
                            isExtensionExpanded = false;
                        }
                        isClickToggle = false;
                    }
                }
            });
        }
    }

    /**
     * 展示最近截图展示框
     *
     * @param h
     */
    private void showRencentPicturePop(int h) {
        //仅显示截图时间在1~30秒内的最新图片
        SLog.i("showRencentPicturePop", h + "***");
        if (screenCaptureUtil == null) {
            return;
        }
        //判断是否有读取文件的权限
        if (!CheckPermissionUtils.requestPermissions(ConversationActivity.this, permissions, REQUEST_CODE_PERMISSION)) {
            return;
        }
        ScreenCaptureUtil.MediaItem mediaItem = screenCaptureUtil.getLastPictureItems(this);
        if (mediaItem == null) {
            return;
        }
        SLog.i("ConverSationActivity", mediaItem.toString());
        //已经展示过的图片不再展示
        if (rencentShowIdList.contains(mediaItem.id)) {
            return;
        }
        rencentShowIdList.add(mediaItem.id);
        //创建时间超过30秒不展示
        if (System.currentTimeMillis() - mediaItem.addTime * 1000 > 30000) {
            return;
        }
        if (rencentPicturePopWindow == null) {
            rencentPicturePopWindow = new RencentPicturePopWindow(this);
        }
        rencentPicturePopWindow.setIvPicture(mediaItem.uri);
        if (!rencentPicturePopWindow.isShowing()) {
            //需加上底部虚拟导航栏的高度
            rencentPicturePopWindow.showPopupWindow(h + NavigationBarUtil.getNavigationBarHeightIfRoom(this));
            //30秒后自动隐藏
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (rencentPicturePopWindow != null && rencentPicturePopWindow.isShowing()) {
                        rencentPicturePopWindow.dismiss();
                    }
                }
            }, 30000);
        }
    }

    @Override
    public void onCountChanged(int count) {
        if (count > 0) {
            if (count < 100) {
                mTitleBar.setLeftText("(" + count + ")");
            } else {
                mTitleBar.setLeftText("(" + 99 + "+)");
            }
        } else {
            mTitleBar.setLeftText("");
        }
    }

    private static class DelayDismissHandler extends Handler {
        //持有弱引用MainActivity,GC回收时会被回收掉.
        private WeakReference<ConversationActivity> mActivity;

        public DelayDismissHandler(ConversationActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                    //todo
//                    Intent intent = new Intent(RongKitIntent.RONG_INTENT_ACTION_WEBVIEW);
//                    intent.setPackage(v.getContext().getPackageName());
//                    intent.putExtra("url", str);
//                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    private void initTitleBar(Conversation.ConversationType conversationType, String mTargetId) {
        // title 布局设置
        if (!conversationType.equals(Conversation.ConversationType.CHATROOM)) {
            UnReadMessageManager.getInstance().addObserver(null, this);
        }


        mTitleBar.setBackgroundResource(R.color.rc_background_main_color);

        if (mConversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)
                || mConversationType.equals(Conversation.ConversationType.CHATROOM)) {
            mTitleBar.setRightVisible(false);
        }
        mTitleBar.setOnRightIconClickListener(new TitleBar.OnRightIconClickListener() {
            @Override
            public void onRightIconClick(View v) {
                toDetailActivity(conversationType, mTargetId);
            }
        });
        mTitleBar.setOnBackClickListener(new TitleBar.OnBackClickListener() {
            @Override
            public void onBackClick() {
                if (fragment != null && !fragment.onBackPressed()) {
                    finish();
                }
            }
        });
    }

    /**
     * 根据 mTargetid 和 ConversationType 进入到设置页面
     */
    private void toDetailActivity(Conversation.ConversationType conversationType, String mTargetId) {

        if (conversationType == Conversation.ConversationType.PUBLIC_SERVICE
                || conversationType == Conversation.ConversationType.APP_PUBLIC_SERVICE) {
            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                    .appendPath("publicServiceProfile")
                    .appendPath(conversationType.getName().toLowerCase())
                    .appendQueryParameter("targetId", mTargetId).build();
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (conversationType == Conversation.ConversationType.PRIVATE) {
            Intent intent = new Intent(this, PrivateChatSettingActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, mTargetId);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, Conversation.ConversationType.PRIVATE);
            startActivity(intent);
        } else if (conversationType == Conversation.ConversationType.GROUP) {
            Intent intent = new Intent(this, GroupDetailActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, mTargetId);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, Conversation.ConversationType.GROUP);
            startActivity(intent);
        } else if (conversationType == Conversation.ConversationType.DISCUSSION) {

        }
    }

    /**
     * 软键盘打开和关闭的监听
     */
    public void setListenerToRootView() {
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                    //软键盘开启
                    isSoftKeyOpened = true;
                } else if (isSoftKeyOpened) {
                    //软键盘关闭
                    if (!isExtensionExpanded && rencentPicturePopWindow != null && rencentPicturePopWindow.isShowing()) {
                        rencentPicturePopWindow.dismiss();
                    }
                    isSoftKeyOpened = false;
                }
            }
        });
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && !CheckPermissionUtils.allPermissionGranted(grantResults)) {
            List<String> permissionsNotGranted = new ArrayList<>();
            for (String permission : permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    permissionsNotGranted.add(permission);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, requestCode);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                            default:
                                break;
                        }
                    }
                };
                CheckPermissionUtils.showPermissionAlert(this, getResources().getString(R.string.seal_grant_permissions) + CheckPermissionUtils.getNotGrantedPermissionMsg(this, permissionsNotGranted), listener);
            }
        }
    }

    /**
     * 显示软键盘
     */
    public void showSoftInput() {
        if (fragment != null && fragment.getRongExtension() != null) {
//            fragment.getRongExtension().showSoftInput();
        }
    }

    /**
     * 在当前会话时是否为群主
     *
     * @return
     */
    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    /**
     * 在当前会话时是否为群管理员
     *
     * @return
     */
    public boolean isGroupManager() {
        return isGroupManager;
    }

    /**
     * 删除联系人成功的事件
     *
     * @param event 删除结果事件
     */
    public void onEventMainThread(DeleteFriendEvent event) {
        if (event.result && event.userId.equals(mTargetId)) {
            SLog.i(TAG, "DeleteFriend Success");
            finish();
        }
    }
}
