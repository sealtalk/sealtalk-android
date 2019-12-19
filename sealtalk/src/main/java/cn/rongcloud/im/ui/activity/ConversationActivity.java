package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import cn.rongcloud.im.ui.fragment.ConversationFragmentEx;
import cn.rongcloud.im.ui.view.AnnouceView;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.CheckPermissionUtils;
import cn.rongcloud.im.utils.NavigationBarUtil;
import cn.rongcloud.im.utils.ScreenCaptureUtil;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.ConversationViewModel;
import cn.rongcloud.im.viewmodel.GroupManagementViewModel;
import cn.rongcloud.im.viewmodel.PrivateChatSettingViewModel;
import io.rong.eventbus.EventBus;
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
    /**
     * 对方id
     */
    private String targetId;
    /**
     * 会话类型
     */
    private Conversation.ConversationType conversationType;
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
        userConfigCache = new UserConfigCache(this);
        mHandler = new DelayDismissHandler(this);
        setListenerToRootView();
        initView();
        initViewModel();
        EventBus.getDefault().register(this);
//        initScreenShotListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTitleStr(targetId, conversationType, title);
        refreshScreenCaptureStatus();

        // 记录当前的会话类型和 id
        IMManager.getInstance().setLastConversationRecord(targetId, conversationType);
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
        IMManager.getInstance().clearConversationRecord(targetId);
        mHandler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RencentPicturePopWindow.REQUEST_PICTURE && resultCode == RESULT_OK) {
            SLog.i("onActivityResult", "send" + "***" + data.getStringExtra(IntentExtra.URL) + "***" + data.getBooleanExtra(IntentExtra.ORGIN, false));
            IMManager.getInstance().sendImageMessage(conversationType, targetId,
                    Collections.singletonList(Uri.parse(data.getStringExtra(IntentExtra.URL))),
                    data.getBooleanExtra(IntentExtra.ORGIN, false));
        }
    }

    private void refreshScreenCaptureStatus() {
        //私聊或者群聊才开启功能
        if (conversationType.equals(Conversation.ConversationType.PRIVATE) || conversationType.equals(Conversation.ConversationType.GROUP)) {
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
                if (conversationType.equals(Conversation.ConversationType.PRIVATE) || conversationType.equals(Conversation.ConversationType.GROUP)) {
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
                        LiveData<Resource<Void>> result = conversationViewModel.sendScreenShotMsg(conversationType.getValue(), targetId);
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

        conversationViewModel.getScreenCaptureStatus(conversationType.getValue(), targetId).observe(this, new Observer<Resource<ScreenCaptureResult>>() {
            @Override
            public void onChanged(Resource<ScreenCaptureResult> screenCaptureResultResource) {
                if (screenCaptureResultResource.status == Status.SUCCESS) {
                    // 0 关闭 1 打开
                    //refreshScreenCaptureStatus();
                }
            }
        });

        // 判读是否为群组聊天
        if (conversationType == Conversation.ConversationType.GROUP) {
            groupManagementViewModel = ViewModelProviders.of(this, new GroupManagementViewModel.Factory(targetId, getApplication())).get(GroupManagementViewModel.class);
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
        if (conversationType == Conversation.ConversationType.GROUP || conversationType == Conversation.ConversationType.PRIVATE) {
            privateChatSettingViewModel = ViewModelProviders.of(this, new PrivateChatSettingViewModel.Factory(getApplication(), targetId, conversationType)).get(PrivateChatSettingViewModel.class);
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
        if (existFragment != null) {
            fragment = (ConversationFragmentEx) existFragment;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        } else {
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

            if (conversationType.equals(Conversation.ConversationType.PRIVATE)
                    || conversationType.equals(Conversation.ConversationType.GROUP)) {
                fragment.setOnExtensionChangeListener(new ConversationFragmentEx.OnExtensionChangeListener() {
                    @Override
                    public void onExtensionHeightChange(int h) {
                        if (!isExtensionHeightInit) {
                            isExtensionHeightInit = true;
                            extensionCollapsedHeight = h;
//                            if (screenCaptureUtil!=null){
//                                rencentScreenCaptureData = null;
//                                rencentScreenCaptureData = new ScreenCaptureData(screenCaptureUtil.getLastPictureItems(ConversationActivity.this).uri
//                                        , screenCaptureUtil.getLastPictureItems(ConversationActivity.this).addTime*1000);
//                            }
//                            showRencentPicturePop(extensionCollapsedHeight);
                        }
                    }

                    @Override
                    public void onExtensionExpanded(int h) {
                        isExtensionExpanded = true;
                        //点击+号如果开启高度不等于0直接显示快捷图片框
                        if (extensionExpandedHeight == 0) {
                            extensionExpandedHeight = h;
                            showRencentPicturePop(extensionExpandedHeight);
                        }
                    }

                    @Override
                    public void onExtensionCollapsed() {
                        //如果是点击 + 号进来的逻辑不设置为false ，因点击 + 号会调用一次此回调，可是点击+号一定最终一定是打开状态
                        if (!isClickToggle) {
                            isExtensionExpanded = false;
                        }
                        isClickToggle = false;
                    }

                    @Override
                    public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
                        //展开高度未获得的时候让 onExtensionExpanded 回调中展示
                        isClickToggle = true;
                        if (extensionExpandedHeight != 0) {
                            showRencentPicturePop(extensionExpandedHeight);
                        }
                    }
                });
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.rong_content, fragment, ConversationFragmentEx.class.getCanonicalName());
            transaction.commitAllowingStateLoss();
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
            fragment.getRongExtension().showSoftInput();
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
        if (event.result && event.userId.equals(targetId)) {
            SLog.i(TAG, "DeleteFriend Success");
            finish();
        }
    }
}
