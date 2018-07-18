package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.FriendDao;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.ReadReceiptViewPager;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.utils.RongDateUtils;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Yuejunhong on 16/10/10.
 * 回执详情
 * Company RongCloud
 */
public class ReadReceiptDetailActivity extends BaseActivity {

    private static final String TAG = "ReadReceiptDetailActivity";

    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final int TEXT_MAX_LINE = 4;

    private Message mMessage;
    private TextMessage mTextMessage;
    private TextView tv_name;
    private TextView tv_time;
    private TextView tv_message;
    private TextView tv_underline_page1;
    private TextView tv_underline_page2;
    private TextView tv_title_unread;
    private TextView tv_title_read;
    private TextView mPromtReadText;
    private TextView mPromtUnreadText;
    private GridView mGridRead;
    private GridView mGridUnread;
    private GridAdapter mReadGridAdapter;
    private GridAdapter mUnreadGridAdapter;
    private View readView;
    private View unreadView;
    private ImageView mImageExpand;
    private boolean isExpand = false;
    private ScrollView mScrollView;
    private int mSolidHeight = 0;

    ReadReceiptViewPager pager = null;
    ArrayList<View> viewContainter = new ArrayList<View>();
    ArrayList<String> titleContainer = new ArrayList<String>();
    private Groups mGroup;
    private List<GroupMember> mGroupMember;
    private List<GroupMember> mReadMember = new ArrayList<GroupMember>();
    private List<GroupMember> mUnreadMember = new ArrayList<GroupMember>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_receipt_detail);
        Intent intent = getIntent();
        mMessage = intent.getParcelableExtra("message");
        if (mMessage == null) {
            return;
        }
        if (mMessage != null) {
            mTextMessage = (TextMessage) mMessage.getContent();
        }
        initViews();
        LoadDialog.show(mContext);
        if (mMessage.getConversationType() == Conversation.ConversationType.GROUP) {
            getGroupInfoFromDB();
        }
        setTitle(R.string.read_receipt_detail);
    }


    private void getGroupInfoFromDB() {
        if (SealUserInfoManager.getInstance() != null && mMessage != null) {
            SealUserInfoManager.getInstance().getGroupsByID(mMessage.getTargetId(), new SealUserInfoManager.ResultCallback<Groups>() {
                @Override
                public void onSuccess(Groups groups) {
                    mGroup = groups;
                    SealUserInfoManager.getInstance().getGroupMembers(mMessage.getTargetId(), new SealUserInfoManager.ResultCallback<List<GroupMember>>() {
                        @Override
                        public void onSuccess(List<GroupMember> groupMembers) {
                            mGroupMember = groupMembers;
                            setReadReceiptMember(mGroupMember);
                            LoadDialog.dismiss(mContext);
                            initDataView();
                        }

                        @Override
                        public void onError(String errString) {
                            LoadDialog.dismiss(mContext);
                        }
                    });
                }

                @Override
                public void onError(String errString) {
                    LoadDialog.dismiss(mContext);
                }
            });
        }
    }

    private void setReadReceiptMember(List<GroupMember> groupMember) {
        if (mUnreadMember == null && mReadMember == null) {
            return;
        }
        if (mUnreadMember != null) {
            mUnreadMember.clear();
        }
        if (mReadMember != null) {
            mReadMember.clear();
        }
        if (groupMember != null && groupMember.size() > 0) {
            for (GroupMember member : groupMember) {
                if (member.getUserId().equals(RongIM.getInstance().getCurrentUserId())) {
                    continue;
                }

                if (isReadMember(member)) {
                    continue;
                } else {
                    mUnreadMember.add(member);
                }
            }

            //添加已读，按照阅读时间排序
            try {
                Map map = mMessage.getReadReceiptInfo().getRespondUserIdList();
                List mHashMapEntryList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
                Collections.sort(mHashMapEntryList, new Comparator<Map.Entry<String, Long>>() {

                    @Override
                    public int compare(Map.Entry<String, Long> firstMapEntry,
                                       Map.Entry<String, Long> secondMapEntry) {
                        return firstMapEntry.getValue().compareTo(secondMapEntry.getValue());
                    }
                });
                if (mHashMapEntryList != null) {
                    Iterator iter = mHashMapEntryList.iterator();
                    GroupMember readMember;
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        Object key = entry.getKey();
                        if (getMemberIndex(key.toString(), groupMember) == -1) { //不在群组中
                            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(key.toString());
                            if (userInfo != null) {
                                readMember = new GroupMember(key.toString(), userInfo.getName(), userInfo.getPortraitUri());
                                readMember.setDisplayName(userInfo.getName());
                            } else {
                                readMember = new GroupMember(key.toString(), key.toString(), null);
                                readMember.setDisplayName(key.toString());
                            }
                            if (mReadMember != null) {
                                mReadMember.add(readMember);
                            }
                        } else {
                            if (mReadMember != null) {
                                mReadMember.add(groupMember.get(getMemberIndex(key.toString(), groupMember)));
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {

            }
        }


    }

    private boolean isReadMember(GroupMember member) {
        if (mMessage != null && mMessage.getReadReceiptInfo() != null
                && mMessage.getReadReceiptInfo().getRespondUserIdList() != null
                && mMessage.getReadReceiptInfo().getRespondUserIdList().size() > 0) {
            if (mMessage.getReadReceiptInfo().getRespondUserIdList().containsKey(member.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得在 groupMember 中的位置,如果不在则返回 -1
     *
     * @param userId
     * @param groupMember
     * @return
     */
    private int getMemberIndex(String userId, List<GroupMember> groupMember) {
        if (groupMember != null && groupMember.size() > 0) {
            for (int i = 0; i < groupMember.size(); i++) {
                if (userId.equals(groupMember.get(i).getUserId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void setSolidHeight(int solidHeight) {
        mSolidHeight = solidHeight;
    }

    private int getSolidHeight() {
        return mSolidHeight;
    }

    public void setGridViewHeight(GridView gridView) {
        if (gridView == null) {
            return;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();

        //获取ActionBar高度
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
        }

        /**
         * 获取状态栏高度
         * */
        int statusBarHeight = 0;
        if (getResources() != null) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }
        params.height = getScreenHeight() - getSolidHeight() - actionBarHeight - statusBarHeight;
        // 设置参数
        gridView.setLayoutParams(params);
    }

    public int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private void initViews() {
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_message = (TextView) findViewById(R.id.tv_message);
        if (mMessage != null) {
            Friend friendInfo = getUserInfoById(mMessage.getSenderUserId());
            if (friendInfo != null) {
                String displayName = friendInfo.getDisplayName();
                if (!TextUtils.isEmpty(displayName)) {
                    tv_name.setText(displayName);
                } else {
                    tv_name.setText(friendInfo.getName());
                }
            } else {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(mMessage.getSenderUserId());
                if (userInfo != null && userInfo.getName() != null) {
                    tv_name.setText(userInfo.getName());
                } else {

                }
            }
            tv_time.setText(RongDateUtils.getConversationFormatDate(mMessage.getSentTime(), this));
        }
        if (mTextMessage != null) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(mTextMessage.getContent());
            AndroidEmoji.ensure(spannable);
            tv_message.setText(spannable);
        }

        mScrollView = (ScrollView) this.findViewById(R.id.rc_read_receipt_scroll_view);

        pager = (ReadReceiptViewPager) this.findViewById(R.id.viewpager);
        readView = LayoutInflater.from(this).inflate(R.layout.tab_read_receipt_layout, null);
        unreadView = LayoutInflater.from(this).inflate(R.layout.tab_read_receipt_layout, null);

        //viewpager开始添加view
        viewContainter.add(readView);
        viewContainter.add(unreadView);

        pager.setAdapter(mPagerAdapter);
        pager.setOnPageChangeListener(mPageChangerLister);
        pager.setCurrentItem(0, false);

        String formatString;
        String content;
        tv_underline_page1 = (TextView) findViewById(R.id.underline_page1);
        tv_underline_page2 = (TextView) findViewById(R.id.underline_page2);
        tv_title_read = (TextView) findViewById(R.id.title_read);

        formatString = getResources().getString(R.string.read_receipt_read_persons);
        content = String.format(formatString, 0);
        tv_title_read.setText(content);
        setTabSelectedBG(tv_underline_page1, tv_title_read);
        tv_title_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(0, false);
            }
        });

        tv_title_unread = (TextView) findViewById(R.id.title_unread);
        formatString = getResources().getString(R.string.read_receipt_unread_persons);
        content = String.format(formatString, 0);
        tv_title_unread.setText(content);
        setTabUnSelectedBG(tv_underline_page2, tv_title_unread);
        tv_title_unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(1, false);
            }
        });

        mPromtReadText = (TextView) readView.findViewById(R.id.promt_text);
        mPromtUnreadText = (TextView) unreadView.findViewById(R.id.promt_text);
        mImageExpand = (ImageView) findViewById(R.id.ic_iv_expand);
        mImageExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpand == false) { //未展开
                    isExpand = true;
                    mImageExpand.setImageDrawable(getResources().getDrawable(R.drawable.rc_read_receipt_up_arrow));
                    tv_message.setMaxLines(10000);
                } else { //展开
                    isExpand = false;
                    mImageExpand.setImageDrawable(getResources().getDrawable(R.drawable.rc_read_receipt_down_arrow));
                    tv_message.setMaxLines(TEXT_MAX_LINE);
                }
            }
        });
        tv_message.post(new Runnable() {
            @Override
            public void run() {
                mImageExpand.setVisibility(tv_message.getLineCount() > TEXT_MAX_LINE ? View.VISIBLE : View.GONE);
                tv_message.setMaxLines(TEXT_MAX_LINE);
            }
        });
    }

    private void initDataView() { //初始化需要群组信息返回之后的数据
        String formatString;
        String content;
        int groupCount = 0;
        int unreadCount = 0;
        int readCount = 0;

        /*设置标签*/
        if (mGroupMember != null) {
            groupCount = mGroupMember.size();
        }
        if (mMessage != null && mMessage.getReadReceiptInfo() != null
                && mMessage.getReadReceiptInfo().getRespondUserIdList() != null) {
            readCount = mMessage.getReadReceiptInfo().getRespondUserIdList().size();
        }

        formatString = getResources().getString(R.string.read_receipt_read_persons);
        content = String.format(formatString, readCount);
        tv_title_read.setText(content);

        if (mUnreadMember != null) {
            unreadCount = mUnreadMember.size();
        } else {
            unreadCount = 0;
        }

        formatString = getResources().getString(R.string.read_receipt_unread_persons);
        content = String.format(formatString, unreadCount);

        /*加载群已读/未读成员信息*/
        if (readView == null || unreadView == null) {
            return;
        }
        mGridRead = (GridView) readView.findViewById(R.id.grid_view);
        mGridUnread = (GridView) unreadView.findViewById(R.id.grid_view);

        mGridUnread.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        mGridRead.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        LinearLayout layout_solid = (LinearLayout) findViewById(R.id.rc_read_receipt_solid_area);
        if (layout_solid != null) {
            setSolidHeight(layout_solid.getHeight());
        }
        if (mReadMember != null && mReadMember.size() > 0) {
            mReadGridAdapter = new GridAdapter(mContext, mReadMember);
            mGridRead.setAdapter(mReadGridAdapter);
            setGridViewHeight(mGridRead);
        } else {

            mPromtReadText.setVisibility(View.VISIBLE);
            mPromtReadText.setText(getResources().getString(R.string.read_receipt_no_person_read));
        }

        if (mUnreadMember != null && mUnreadMember.size() > 0) {
            mUnreadGridAdapter = new GridAdapter(mContext, mUnreadMember);
            mGridUnread.setAdapter(mUnreadGridAdapter);
        } else {
            mPromtUnreadText.setVisibility(View.VISIBLE);
            mPromtUnreadText.setText(getResources().getString(R.string.read_receipt_no_person_unread));
        }

        tv_title_unread.setText(content);
        tv_name.setFocusable(true);
        tv_name.setFocusableInTouchMode(true);
        tv_name.requestFocus();
    }

    private class GridAdapter extends BaseAdapter {

        private List<GroupMember> list;
        Context context;

        public GridAdapter(Context context, List<GroupMember> list) {
            this.list = list;
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.read_receipt_gridview_item, parent, false);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);

            final GroupMember bean = list.get(position);
            Friend friend = getUserInfoById(bean.getUserId());
            if (friend != null && !TextUtils.isEmpty(friend.getDisplayName())) {
                tv_username.setText(friend.getDisplayName());
            } else {
                tv_username.setText(bean.getName());
            }

            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(bean);
            ImageLoader.getInstance().displayImage(portraitUri, iv_avatar, App.getOptions());
            iv_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserInfo userInfo = new UserInfo(bean.getUserId(), bean.getName(), TextUtils.isEmpty(bean.getPortraitUri().toString()) ? Uri.parse(RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId())) : bean.getPortraitUri());
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
                    intent.putExtra("friend", friend);
                    intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                    intent.putExtra("conversationType", mMessage.getConversationType().getValue());
                    if (mGroup != null) {
                        intent.putExtra("groupName", mGroup.getName());
                    }
                    context.startActivity(intent);
                }

            });
            iv_avatar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            if (position == list.size() - 1) {
                tv_username.setPadding(0, 0, 0, RongUtils.dip2px(8));
            } else {
                tv_username.setPadding(0, 0, 0, 0);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            if (list != null) {
                return list.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<GroupMember> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }

    PagerAdapter mPagerAdapter = new PagerAdapter() {
        //viewpager中的组件数量
        @Override
        public int getCount() {
            return viewContainter.size();
        }

        //滑动切换的时候销毁当前的组件
        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            (container).removeView(viewContainter.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            (container).addView(viewContainter.get(position));
            return viewContainter.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleContainer.get(position);
        }
    };

    private ViewPager.OnPageChangeListener mPageChangerLister = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            if (pager.getCurrentItem() == 0) {
                setTabSelectedBG(tv_underline_page1, tv_title_read);
                setTabUnSelectedBG(tv_underline_page2, tv_title_unread);
                setGridViewHeight(mGridRead);
            } else if (pager.getCurrentItem() == 1) {
                setTabSelectedBG(tv_underline_page2, tv_title_unread);
                setTabUnSelectedBG(tv_underline_page1, tv_title_read);
                setGridViewHeight(mGridUnread);
            }
        }

    };

    private void setTabSelectedBG(TextView vLine, TextView vText) {
        vLine.setBackgroundColor(getResources().getColor(R.color.ac_filter_string_color));
        vLine.setHeight(6);
        vText.setTextColor(getResources().getColor(R.color.ac_filter_string_color));
    }

    private void setTabUnSelectedBG(TextView vLine, TextView vText) {
        vLine.setBackgroundColor(getResources().getColor(R.color.text_line_color));
        vLine.setHeight(1);
        vText.setTextColor(getResources().getColor(R.color.text_color));
    }

    private Friend getUserInfoById(String userId) {
        if (!TextUtils.isEmpty(userId)) {
            return DBManager.getInstance().getDaoSession().getFriendDao().queryBuilder().where(FriendDao.Properties.UserId.eq(userId)).unique();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        SealAppContext.getInstance().popActivity(this);
        super.onBackPressed();
    }
}
