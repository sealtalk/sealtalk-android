package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.MorePopWindow;
import cn.rongcloud.im.ui.fragment.MainContactsListFragment;
import cn.rongcloud.im.ui.fragment.MainConversationListFragment;
import cn.rongcloud.im.ui.fragment.MainDiscoveryFragment;
import cn.rongcloud.im.ui.fragment.MainMeFragment;
import cn.rongcloud.im.ui.view.MainBottomTabGroupView;
import cn.rongcloud.im.ui.view.MainBottomTabItem;
import cn.rongcloud.im.ui.widget.DragPointView;
import cn.rongcloud.im.ui.widget.TabGroupView;
import cn.rongcloud.im.ui.widget.TabItem;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.MainViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.RongIM;

public class MainActivity extends BaseActivity implements MorePopWindow.OnPopWindowItemClickListener {
    public static final String PARAMS_TAB_INDEX = "tab_index";
    private static final int REQUEST_START_CHAT = 0;
    private static final int REQUEST_START_GROUP = 1;
    private static final String TAG = "MainActivity";

    private ViewPager vpFragmentContainer;
    private MainBottomTabGroupView tabGroupView;
    private ImageView ivSearch;
    private ImageView ivMore;
    private AppViewModel appViewModel;
    public MainViewModel mainViewModel;

    /**
     * tab 项枚举
     */
    public enum Tab {
        /**
         * 聊天
         */
        CHAT(0),
        /**
         * 联系人
         */
        CONTACTS(1),
        /**
         * 发现
         */
        FIND(2),
        /**
         * 我的
         */
        ME(3);

        private int value;

        Tab(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Tab getType(int value) {
            for (Tab type : Tab.values()) {
                if (value == type.getValue()) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * tabs 的图片资源
     */
    private int[] tabImageRes = new int[]{
            R.drawable.seal_tab_chat_selector,
            R.drawable.seal_tab_contact_list_selector,
            R.drawable.seal_tab_find_selector,
            R.drawable.seal_tab_me_selector
    };

    /**
     * 各个 Fragment 界面
     */
    private List<Fragment> fragments = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_main);
        initView();
        initViewModel();
        clearBadgeStatu();
    }

    //清除华为的角标
    private void clearBadgeStatu() {
        if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            try {
                String packageName = getPackageName();
                String launchClassName = getPackageManager()
                        .getLaunchIntentForPackage(packageName)
                        .getComponent().getClassName();
                Bundle bundle = new Bundle();//需要存储的数据
                bundle.putString("package", packageName);//包名
                bundle.putString("class", launchClassName);//启动的Activity完整名称
                bundle.putInt("badgenumber", 0);//未读信息条数清空
                getContentResolver().call(
                        Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                        "change_badge", null, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 初始化布局
     */
    private void initView() {
        int tabIndex = getIntent().getIntExtra(PARAMS_TAB_INDEX, Tab.CHAT.getValue());

        // title
        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SealSearchActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MorePopWindow morePopWindow = new MorePopWindow(MainActivity.this, MainActivity.this);
                morePopWindow.showPopupWindow(v);
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

    /**
     * 初始化 Tabs
     */
    private void initTabs() {
        // 初始化 tab
        List<TabItem> items = new ArrayList<>();
        String[] stringArray = getResources().getStringArray(R.array.tab_names);

        for (Tab tab : Tab.values()) {
            TabItem tabItem = new TabItem();
            tabItem.id = tab.getValue();
            tabItem.text = stringArray[tab.getValue()];
            tabItem.drawable = tabImageRes[tab.getValue()];
            items.add(tabItem);
        }

        tabGroupView.initView(items, new TabGroupView.OnTabSelectedListener() {
            @Override
            public void onSelected(View view, TabItem item) {
                // 当点击 tab 的后， 也要切换到正确的 fragment 页面
                int currentItem = vpFragmentContainer.getCurrentItem();
                if (currentItem != item.id) {
                    // 切换布局
                    vpFragmentContainer.setCurrentItem(item.id);
                    // 如果是我的页面， 则隐藏红点
                    if (item.id == Tab.ME.getValue()) {
                        ((MainBottomTabItem) tabGroupView.getView(Tab.ME.getValue())).setRedVisibility(View.GONE);
                    }
                }
            }
        });

        tabGroupView.setOnTabDoubleClickListener(new MainBottomTabGroupView.OnTabDoubleClickListener() {
            @Override
            public void onDoubleClick(TabItem item, View view) {
                // 双击定位到某一个未读消息位置
                if (item.id == Tab.CHAT.getValue()) {
                    MainConversationListFragment fragment = (MainConversationListFragment) fragments.get(Tab.CHAT.getValue());
                    fragment.focusUnreadItem();
                }
            }
        });

        // 未读数拖拽
        ((MainBottomTabItem) tabGroupView.getView(Tab.CHAT.getValue())).setTabUnReadNumDragListener(new DragPointView.OnDragListencer() {

            @Override
            public void onDragOut() {
                ((MainBottomTabItem) tabGroupView.getView(Tab.CHAT.getValue())).setNumVisibility(View.GONE);
                showToast(getString(R.string.seal_main_toast_unread_clear_success));
                clearUnreadStatus();
            }
        });
        ((MainBottomTabItem) tabGroupView.getView(Tab.CHAT.getValue())).setNumVisibility(View.VISIBLE);
    }


    /**
     * 初始化 initFragmentViewPager
     */
    private void initFragmentViewPager() {
        fragments.add(new MainConversationListFragment());
        fragments.add(new MainContactsListFragment());
        fragments.add(new MainDiscoveryFragment());
        fragments.add(new MainMeFragment());

        // ViewPager 的 Adpater
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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
        vpFragmentContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // 当页面切换完成之后， 同时也要把 tab 设置到正确的位置
                tabGroupView.setSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        appViewModel.getHasNewVersion().observe(this, new Observer<Resource<VersionInfo.AndroidVersion>>() {
            @Override
            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                if (resource.status == Status.SUCCESS && resource.data != null) {
                    if (tabGroupView.getSelectedItemId() != Tab.ME.getValue()) {
                        ((MainBottomTabItem) tabGroupView.getView(Tab.ME.getValue())).setRedVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // 未读消息
        mainViewModel.getUnReadNum().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                MainBottomTabItem chatTab = (MainBottomTabItem) tabGroupView.getView(Tab.CHAT.getValue());
                if (count == 0) {
                    chatTab.setNumVisibility(View.GONE);
                } else if (count > 0 && count < 100) {
                    chatTab.setNumVisibility(View.VISIBLE);
                    chatTab.setNum(String.valueOf(count));
                } else {
                    chatTab.setVisibility(View.VISIBLE);
                    chatTab.setNum(getString(R.string.seal_main_chat_tab_more_read_message));
                }
            }
        });

        // 新朋友数量
        mainViewModel.getNewFriendNum().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                MainBottomTabItem chatTab = tabGroupView.getView(Tab.CONTACTS.getValue());
                if (count > 0) {
                    chatTab.setRedVisibility(View.VISIBLE);
                } else {
                    chatTab.setRedVisibility(View.GONE);
                }
            }
        });

        mainViewModel.getPrivateChatLiveData().observe(this, new Observer<FriendShipInfo>() {
            @Override
            public void onChanged(FriendShipInfo friendShipInfo) {
                RongIM.getInstance().startPrivateChat(MainActivity.this,
                        friendShipInfo.getUser().getId(),
                        TextUtils.isEmpty(friendShipInfo.getDisplayName()) ?
                                friendShipInfo.getUser().getNickname() : friendShipInfo.getDisplayName());
            }
        });

    }


    /**
     * 清理未读消息状态
     */
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
                    ArrayList<String> memberList = data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
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


    /**
     * 发起单聊
     */
    @Override
    public void onStartChartClick() {
        Intent intent = new Intent(this, SelectSingleFriendActivity.class);
        startActivityForResult(intent, REQUEST_START_CHAT);
    }

    /**
     * 创建群组
     */
    @Override
    public void onCreateGroupClick() {
        Intent intent = new Intent(this, SelectCreateGroupActivity.class);
        startActivityForResult(intent, REQUEST_START_GROUP);
    }

    /**
     * 添加好友
     */
    @Override
    public void onAddFriendClick() {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);
    }

    /**
     * 扫一扫
     */
    @Override
    public void onScanClick() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

}
