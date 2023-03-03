package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.security.SMSDKUtils;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.FraudTipsDialog;
import cn.rongcloud.im.ui.dialog.MorePopWindow;
import cn.rongcloud.im.ui.fragment.MainContactsListFragment;
import cn.rongcloud.im.ui.fragment.MainDiscoveryFragment;
import cn.rongcloud.im.ui.fragment.MainMeFragment;
import cn.rongcloud.im.ui.fragment.UltraConversationListFragment;
import cn.rongcloud.im.ui.view.MainBottomTabGroupView;
import cn.rongcloud.im.ui.view.MainBottomTabItem;
import cn.rongcloud.im.ui.widget.DragPointView;
import cn.rongcloud.im.ui.widget.TabGroupView;
import cn.rongcloud.im.ui.widget.TabItem;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.MainViewModel;
import cn.rongcloud.im.viewmodel.SecurityViewModel;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;
import com.umeng.commonsdk.UMConfigure;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.picture.tools.ScreenUtils;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity
        implements MorePopWindow.OnPopWindowItemClickListener {
    public static final String PARAMS_TAB_INDEX = "tab_index";
    private static final int REQUEST_START_CHAT = 0;
    private static final int REQUEST_START_GROUP = 1;
    private static final String TAG = "MainActivity";
    public static final String CHAT = "chat";
    public static final String ULTRA = "ultra";
    public static final String CONTACTS = "contacts";
    public static final String FIND = "find";
    public static final String ME = "me";

    private ViewPager vpFragmentContainer;
    private MainBottomTabGroupView tabGroupView;
    private ImageView ivSearch;
    private ImageView ivMore;
    private AppViewModel appViewModel;
    public MainViewModel mainViewModel;
    private SecurityViewModel securityViewModel;
    private UltraGroupViewModel mConversationListViewModel;
    private TextView tvTitle;
    private RelativeLayout btnSearch;
    private ImageButton btnMore;
    private boolean isDebugUltraGroup = false; // 是否处于debug模式
    private LinkedHashMap<String, Integer> tabsMap = new LinkedHashMap<>();
    private String[] tabNameList; // tab 显示名称数组

    /** 各个 Fragment 界面 */
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_main);
        ApplicationInfo info = this.getApplicationInfo();
        isDebugUltraGroup =
                getApplication()
                        .getSharedPreferences("config", MODE_PRIVATE)
                        .getBoolean("isDebug", false);
        initTabData();
        initView();
        initViewModel();
        clearBadgeStatu();
        showFraudTipsDialog();
        initAMapPrivacy();
        if (isDebugUltraGroup) {
            initOtherPrivacy();
        }
        if (Build.VERSION.SDK_INT >= 33) {
            askNotificationPermission();
        }
    }

    private void initOtherPrivacy() {
        UMConfigure.init(
                this, BuildConfig.SEALTALK_UMENG_APPKEY, null, UMConfigure.DEVICE_TYPE_PHONE, null);
    }

    private void initAMapPrivacy() {
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);
    }

    private void showFraudTipsDialog() {
        if (!BuildConfig.DEBUG) {
            new FraudTipsDialog(this).show();
        }
    }

    // 设置Activity对应的顶部状态栏的颜色
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 清除华为的角标
    private void clearBadgeStatu() {
        if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            try {
                String packageName = getPackageName();
                String launchClassName =
                        getPackageManager()
                                .getLaunchIntentForPackage(packageName)
                                .getComponent()
                                .getClassName();
                Bundle bundle = new Bundle(); // 需要存储的数据
                bundle.putString("package", packageName); // 包名
                bundle.putString("class", launchClassName); // 启动的Activity完整名称
                bundle.putInt("badgenumber", 0); // 未读信息条数清空
                getContentResolver()
                        .call(
                                Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                                "change_badge",
                                null,
                                bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 初始化布局 */
    private void initView() {
        tvTitle = findViewById(R.id.tv_title);
        btnSearch = findViewById(R.id.btn_search);
        btnMore = findViewById(R.id.btn_more);

        int tabIndex = getIntent().getIntExtra(PARAMS_TAB_INDEX, tabsMap.get(CHAT));

        // title
        btnSearch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SealSearchActivity.class);
                        startActivity(intent);
                    }
                });

        btnMore.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentItem = vpFragmentContainer.getCurrentItem();
                        if (currentItem == tabsMap.get(CHAT)) {
                            MorePopWindow morePopWindow =
                                    new MorePopWindow(MainActivity.this, MainActivity.this);
                            morePopWindow.showPopupWindow(btnMore, 0.8f, -getXOffset(), 0);
                        } else if (currentItem == tabsMap.get(CONTACTS)) {
                            onAddFriendClick();
                        }
                    }
                });

        // 底部按钮
        tabGroupView = findViewById(R.id.tg_bottom_tabs);
        vpFragmentContainer = findViewById(R.id.vp_main_container);

        // 初始化底部 tabs
        initTabs();
        // 初始化 fragment 的 viewpager
        initFragmentViewPager();

        // 设置当前的选项为聊天界面
        tabGroupView.setSelected(tabIndex);
    }

    /** 初始化 Tabs */
    private void initTabs() {
        // 初始化 tab
        List<TabItem> items = new ArrayList<>();
        if (isDebugUltraGroup) {
            tabNameList = getResources().getStringArray(R.array.tab_names_ultra);
        } else {
            tabNameList = getResources().getStringArray(R.array.tab_names_nomal);
        }
        List<TabItem.AnimationDrawableBean> animationDrawableList = new ArrayList<>();
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        R.drawable.tab_chat_0, R.drawable.tab_chat_animation_list));
        if (isDebugUltraGroup) {
            animationDrawableList.add(
                    new TabItem.AnimationDrawableBean(
                            R.drawable.rc_ultra_0, R.drawable.tab_ultra_animation_list));
        }
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        R.drawable.tab_contacts_0, R.drawable.tab_contacts_animation_list));
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        R.drawable.tab_chatroom_0, R.drawable.tab_chatroom_animation_list));
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        R.drawable.tab_me_0, R.drawable.tab_me_animation_list));
        for (Map.Entry<String, Integer> entry : tabsMap.entrySet()) {
            TabItem tabItem = new TabItem();
            tabItem.id = entry.getValue();
            tabItem.text = tabNameList[entry.getValue()];
            tabItem.animationDrawable = animationDrawableList.get(entry.getValue());
            items.add(tabItem);
        }

        tabGroupView.initView(
                items,
                new TabGroupView.OnTabSelectedListener() {
                    @Override
                    public void onSelected(View view, TabItem item) {
                        // 当点击 tab 的后， 也要切换到正确的 fragment 页面
                        int currentItem = vpFragmentContainer.getCurrentItem();
                        if (currentItem != item.id) {
                            // 切换布局
                            vpFragmentContainer.setCurrentItem(item.id);
                            if (item.id == tabsMap.get(ME)) {
                                // 如果是我的页面， 则隐藏红点
                                ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(ME)))
                                        .setRedVisibility(View.GONE);
                                if (isDebugUltraGroup) {
                                    tvTitle.setText(tabNameList[4]);
                                } else {
                                    tvTitle.setText(tabNameList[3]);
                                }
                                btnMore.setVisibility(View.GONE);
                                btnSearch.setVisibility(View.GONE);
                            }
                        } else if (item.id == tabsMap.get(CHAT)) {
                            btnMore.setVisibility(View.VISIBLE);
                            btnSearch.setVisibility(View.VISIBLE);
                            btnMore.setImageDrawable(
                                    getResources().getDrawable(R.drawable.seal_ic_main_more));
                            tvTitle.setText(tabNameList[0]);
                        } else if (isDebugUltraGroup && item.id == tabsMap.get(ULTRA)) {
                            btnMore.setVisibility(View.VISIBLE);
                            btnSearch.setVisibility(View.GONE);
                            mConversationListViewModel.getUltraGroupMemberList();
                            btnMore.setVisibility(View.GONE);
                            tvTitle.setText(tabNameList[1]);
                        } else if (item.id == tabsMap.get(CONTACTS)) {
                            btnMore.setVisibility(View.VISIBLE);
                            btnSearch.setVisibility(View.VISIBLE);
                            btnMore.setImageDrawable(
                                    getResources().getDrawable(R.drawable.seal_ic_main_add_friend));
                            if (isDebugUltraGroup) {
                                tvTitle.setText(tabNameList[2]);
                            } else {
                                tvTitle.setText(tabNameList[1]);
                            }
                        } else if (item.id == tabsMap.get(FIND)) {
                            if (isDebugUltraGroup) {
                                tvTitle.setText(tabNameList[3]);
                            } else {
                                tvTitle.setText(tabNameList[2]);
                            }
                            btnMore.setVisibility(View.GONE);
                            btnSearch.setVisibility(View.GONE);
                        }
                    }
                });

        tabGroupView.setOnTabDoubleClickListener(
                new MainBottomTabGroupView.OnTabDoubleClickListener() {
                    @Override
                    public void onDoubleClick(TabItem item, View view) {
                        // 双击定位到某一个未读消息位置
                        if (item.id == tabsMap.get(CHAT)) {
                            // todo
                            //                    MainConversationListFragment fragment =
                            // (MainConversationListFragment) fragments.get(Tab.CHAT.getValue());
                            //                    fragment.focusUnreadItem();
                        }
                    }
                });

        // 未读数拖拽
        ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT)))
                .setTabUnReadNumDragListener(
                        new DragPointView.OnDragListencer() {

                            @Override
                            public void onDragOut() {
                                ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT)))
                                        .setNumVisibility(View.GONE);
                                showToast(getString(R.string.seal_main_toast_unread_clear_success));
                                clearUnreadStatus();
                            }
                        });
        ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT)))
                .setNumVisibility(View.VISIBLE);
    }

    private void initTabData() {
        if (isDebugUltraGroup) {
            tabsMap.put(CHAT, 0);
            tabsMap.put(ULTRA, 1);
            tabsMap.put(CONTACTS, 2);
            tabsMap.put(FIND, 3);
            tabsMap.put(ME, 4);
        } else {
            tabsMap.put(CHAT, 0);
            tabsMap.put(CONTACTS, 1);
            tabsMap.put(FIND, 2);
            tabsMap.put(ME, 3);
        }
    }

    /** 初始化 initFragmentViewPager */
    private void initFragmentViewPager() {
        fragments.add(new ConversationListFragment());
        if (isDebugUltraGroup) {
            fragments.add(new UltraConversationListFragment());
        }
        fragments.add(new MainContactsListFragment());
        fragments.add(new MainDiscoveryFragment());
        fragments.add(new MainMeFragment());

        //        FragmentTransaction fragmentTransaction =
        // getSupportFragmentManager().beginTransaction();
        //        for (Fragment item : fragments) {
        //            fragmentTransaction.add(R.id.vp_main_container, item).hide(item);
        //        }
        //        fragmentTransaction.show(fragments.get(0)).commit();

        // ViewPager 的 Adpater
        FragmentPagerAdapter fragmentPagerAdapter =
                new FragmentPagerAdapter(
                        getSupportFragmentManager(),
                        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                    @Override
                    public Fragment getItem(int position) {
                        return fragments.get(position);
                    }

                    @Override
                    public int getCount() {
                        return fragments.size();
                    }
                };

        vpFragmentContainer.setAdapter(fragmentPagerAdapter);
        vpFragmentContainer.setOffscreenPageLimit(fragments.size());
        // 设置页面切换监听
        vpFragmentContainer.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(
                            int position, float positionOffset, int positionOffsetPixels) {}

                    @Override
                    public void onPageSelected(int position) {
                        // 当页面切换完成之后， 同时也要把 tab 设置到正确的位置
                        tabGroupView.setSelected(position);
                        if (isDebugUltraGroup) {
                            if (position == 0) {
                                RouteUtils.registerActivity(
                                        RouteUtils.RongActivityType.ConversationActivity,
                                        ConversationActivity.class);
                            } else if (position == 1) {
                                RouteUtils.registerActivity(
                                        RouteUtils.RongActivityType.ConversationActivity,
                                        UltraConversationActivity.class);
                            }
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {}
                });
    }

    /** 初始化ViewModel */
    private void initViewModel() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        securityViewModel = ViewModelProviders.of(this).get(SecurityViewModel.class);
        if (isDebugUltraGroup) {
            mConversationListViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);
        }
        appViewModel
                .getHasNewVersion()
                .observe(
                        this,
                        new Observer<Resource<VersionInfo.AndroidVersion>>() {
                            @Override
                            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                                if (resource.status == Status.SUCCESS && resource.data != null) {
                                    if (tabGroupView.getSelectedItemId() != tabsMap.get(ME)) {
                                        ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(ME)))
                                                .setRedVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });

        // 未读消息
        mainViewModel
                .getUnReadNum()
                .observe(
                        this,
                        new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer count) {
                                MainBottomTabItem chatTab =
                                        (MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT));
                                if (count == 0) {
                                    chatTab.setNumVisibility(View.GONE);
                                } else if (count > 0 && count < 100) {
                                    chatTab.setNumVisibility(View.VISIBLE);
                                    chatTab.setNum(String.valueOf(count));
                                } else {
                                    chatTab.setVisibility(View.VISIBLE);
                                    chatTab.setNum(
                                            getString(
                                                    R.string.seal_main_chat_tab_more_read_message));
                                }
                            }
                        });

        // 新朋友数量
        mainViewModel
                .getNewFriendNum()
                .observe(
                        this,
                        new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer count) {
                                MainBottomTabItem chatTab =
                                        tabGroupView.getView(tabsMap.get(CONTACTS));
                                if (count > 0) {
                                    chatTab.setRedVisibility(View.VISIBLE);
                                } else {
                                    chatTab.setRedVisibility(View.GONE);
                                }
                            }
                        });

        mainViewModel
                .getPrivateChatLiveData()
                .observe(
                        this,
                        new Observer<FriendShipInfo>() {
                            @Override
                            public void onChanged(FriendShipInfo friendShipInfo) {
                                Bundle bundle = new Bundle();
                                bundle.putString(
                                        "title",
                                        TextUtils.isEmpty(friendShipInfo.getDisplayName())
                                                ? friendShipInfo.getUser().getNickname()
                                                : friendShipInfo.getDisplayName());
                                RouteUtils.routeToConversationActivity(
                                        MainActivity.this,
                                        ConversationIdentifier.obtainPrivate(
                                                friendShipInfo.getUser().getId()),
                                        bundle);
                            }
                        });
        securityViewModel
                .getSecurityVerify()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS && resource.data != null) {
                                if (resource.data.isKickOut()) {
                                    logoutBySecurity();
                                }
                            }
                        });
        securityViewModel
                .getSecurityStatus()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS && resource.data != null) {
                                boolean openEnable = resource.data.openEnable;
                                if (!BuildConfig.DEBUG && openEnable) {
                                    SMSDKUtils.init(
                                            MainActivity.this.getApplicationContext(),
                                            new SMSDKUtils.Callback() {
                                                @Override
                                                public void onSuccess(String id) {
                                                    securityViewModel.doSecurityVerify(id);
                                                }
                                            });
                                }
                            }
                        });
    }

    /** 清理未读消息状态 */
    private void clearUnreadStatus() {
        if (mainViewModel != null) {
            mainViewModel.clearMessageUnreadStatus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_START_CHAT:
                    mainViewModel.startPrivateChat(data.getStringExtra(IntentExtra.STR_TARGET_ID));
                    break;
                case REQUEST_START_GROUP:
                    ArrayList<String> memberList =
                            data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
                    SLog.i(TAG, "memberList.size = " + memberList.size());
                    Intent intent = new Intent(this, CreateGroupActivity.class);
                    intent.putExtra(IntentExtra.LIST_STR_ID_LIST, memberList);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    /** 发起单聊 */
    @Override
    public void onStartChartClick() {
        Intent intent = new Intent(this, SelectSingleFriendActivity.class);
        startActivityForResult(intent, REQUEST_START_CHAT);
    }

    /** 创建群组 */
    @Override
    public void onCreateGroupClick() {
        Intent intent = new Intent(this, SelectCreateGroupActivity.class);
        startActivityForResult(intent, REQUEST_START_GROUP);
    }

    /** 添加好友 */
    @Override
    public void onAddFriendClick() {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);
    }

    /** 扫一扫 */
    @Override
    public void onScanClick() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private int getXOffset() {
        int marginEnd = ScreenUtils.dip2px(MainActivity.this, 12);
        float popSelfXOffset =
                getResources().getDimension(R.dimen.seal_main_title_popup_width)
                        - btnMore.getWidth();
        return (int) (popSelfXOffset);
    }

    @RequiresApi(api = 33)
    private void askNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.GET_PERMISSIONS) {

        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            // FCM SDK (and your app) can post notifications.
                        } else {

                        }
                    });
}
