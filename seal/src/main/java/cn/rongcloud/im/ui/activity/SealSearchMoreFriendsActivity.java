package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.FriendDao;
import cn.rongcloud.im.model.SearchResult;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import de.greenrobot.dao.query.QueryBuilder;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;

/**
 * Created by tiankui on 16/9/2.
 */
public class SealSearchMoreFriendsActivity extends Activity implements AdapterView.OnItemClickListener {

    private EditText mSearchEditText;
    private ListView mFriendListView;
    private TextView mSearchNoResultsTextView;
    private ImageView mPressBackImageView;
    private LinearLayout mFriendListResultLinearLayout;

    private String mFilterString;
    private ArrayList<Friend> mFilterFriendList;
    private AsyncTask mAsyncTask;
    private ThreadPoolExecutor mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_friends_detail_info);

        Intent intent = getIntent();
        mFilterString = intent.getStringExtra("filterString");
        mFilterFriendList = intent.getParcelableArrayListExtra("filterFriendList");
        initView();
        initData();
    }

    public void initView() {
        mSearchEditText = (EditText) findViewById(R.id.ac_et_search);
        mFriendListView = (ListView) findViewById(R.id.ac_lv_friend_list_detail_info);
        mSearchNoResultsTextView = (TextView) findViewById(R.id.ac_tv_search_no_results);
        mPressBackImageView = (ImageView) findViewById(R.id.ac_iv_press_back);
        mFriendListResultLinearLayout = (LinearLayout)findViewById(R.id.ac_ll_friend_list_result);
    }

    public void initData() {

        mExecutor = new ThreadPoolExecutor(3, 5, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFilterString = s.toString();
                mAsyncTask = new AsyncTask<String, Void, SearchResult>() {

                    @Override
                    protected SearchResult doInBackground(String... params) {
                        return filterInfo(mFilterString);
                    }

                    @Override
                    protected void onPostExecute(SearchResult searchResult) {

                        if (searchResult.getFilterStr().equals(mFilterString)) {
                            List<Friend> filterFriendList = searchResult.getFilterFriendList();
                            if (filterFriendList.size() > 0) {
                                mFriendListResultLinearLayout.setVisibility(View.VISIBLE);
                                mFriendListView.setVisibility(View.VISIBLE);
                                FriendListAdapter friendListAdapter = new FriendListAdapter(filterFriendList);
                                mFriendListView.setAdapter(friendListAdapter);
                            } else {
                                mFriendListResultLinearLayout.setVisibility(View.GONE);
                                mFriendListView.setVisibility(View.GONE);
                            }

                            if (mFilterString.equals("")) {
                                mSearchNoResultsTextView.setVisibility(View.GONE);
                            }
                            if (filterFriendList.size() == 0) {
                                if (mFilterString.equals("")) {
                                    mSearchNoResultsTextView.setVisibility(View.GONE);
                                } else {
                                    mSearchNoResultsTextView.setVisibility(View.VISIBLE);
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                    spannableStringBuilder.append(getResources().getString(R.string.ac_search_no_result_pre));
                                    SpannableStringBuilder colorFilterStr = new SpannableStringBuilder(mFilterString);
                                    colorFilterStr.setSpan(new ForegroundColorSpan(Color.parseColor("#0099ff")), 0, mFilterString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                    spannableStringBuilder.append(colorFilterStr);
                                    spannableStringBuilder.append(getResources().getString(R.string.ac_search_no_result_suffix));
                                    mSearchNoResultsTextView.setText(spannableStringBuilder);
                                }
                            } else {
                                mSearchNoResultsTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                } .executeOnExecutor(mExecutor, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mSearchEditText.getRight() - 2 * mSearchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        filterInfo("");
                        mSearchEditText.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        mPressBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SealSearchMoreFriendsActivity.this.finish();
            }
        });

        mFriendListView.setOnItemClickListener(this);

        mSearchEditText.setText(mFilterString);
    }

    private synchronized SearchResult filterInfo(String filterStr) {

        List<Friend> filterFriendList = new ArrayList<>();
        SearchResult searchResult = new SearchResult();

        if (filterStr.equals("")) {
            SearchResult result = new SearchResult();
            result.setFilterStr("");
            result.setFilterFriendList(filterFriendList);
            return result;
        }
        if (filterStr.contains("'")) {
            SearchResult result = new SearchResult();
            result.setFilterStr(filterStr);
            result.setFilterFriendList(filterFriendList);
            return result;
        }
        QueryBuilder queryBuilder = DBManager.getInstance().getDaoSession().getFriendDao().queryBuilder();
        filterFriendList = queryBuilder.where(queryBuilder.or(FriendDao.Properties.Name.like("%" + filterStr + "%"),
                                              FriendDao.Properties.DisplayName.like("%" + filterStr + "%"),
                                              FriendDao.Properties.NameSpelling.like(filterStr + "%"),
                                              FriendDao.Properties.DisplayNameSpelling.like(filterStr + "%"))).orderAsc(FriendDao.Properties.DisplayNameSpelling, FriendDao.Properties.NameSpelling).build().list();

        searchResult.setFilterStr(filterStr);
        searchResult.setFilterFriendList(filterFriendList);
        return searchResult;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object selectObject = parent.getItemAtPosition(position);
        if (selectObject instanceof Friend) {
            Friend friend = (Friend) selectObject;
            if (!TextUtils.isEmpty(friend.getDisplayName())) {
                RongIM.getInstance().startPrivateChat(SealSearchMoreFriendsActivity.this, friend.getUserId(), friend.getDisplayName());
            } else {
                RongIM.getInstance().startPrivateChat(SealSearchMoreFriendsActivity.this, friend.getUserId(), friend.getName());
            }
        }
    }

    private class FriendListAdapter extends BaseAdapter {

        private List<Friend> filterFriendList;
        public FriendListAdapter(List<Friend> filterFriendList) {
            this.filterFriendList = filterFriendList;
        }
        @Override
        public int getCount() {
            if (filterFriendList != null) {
                return filterFriendList.size();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            Friend friend = (Friend) getItem(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(SealSearchMoreFriendsActivity.this, R.layout.item_filter_friend_list, null);
                viewHolder.portraitImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.item_aiv_friend_image);
                viewHolder.nameDisplayNameLinearLayout = (LinearLayout) convertView.findViewById(R.id.item_ll_friend_name);
                viewHolder.displayNameTextView = (TextView) convertView.findViewById(R.id.item_tv_friend_display_name);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_tv_friend_name);
                viewHolder.nameSingleTextView = (TextView) convertView.findViewById(R.id.item_tv_friend_name_single);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(friend);
            ImageLoader.getInstance().displayImage(portraitUri, viewHolder.portraitImageView, App.getOptions());
            if (!TextUtils.isEmpty(friend.getDisplayName())) {
                viewHolder.nameSingleTextView.setVisibility(View.GONE);
                viewHolder.nameDisplayNameLinearLayout.setVisibility(View.VISIBLE);
                viewHolder.displayNameTextView.setText(CharacterParser.getInstance().getColoredDisplayName(mFilterString, friend.getDisplayName()));
                viewHolder.nameTextView.setText(CharacterParser.getInstance().getColoredName(mFilterString, friend.getName()));
            } else {
                viewHolder.nameDisplayNameLinearLayout.setVisibility(View.GONE);
                viewHolder.nameSingleTextView.setVisibility(View.VISIBLE);
                viewHolder.nameSingleTextView.setText(CharacterParser.getInstance().getColoredName(mFilterString, friend.getName()));
            }

            return convertView;
        }

        @Override
        public Object getItem(int position) {
            if (filterFriendList == null)
                return null;

            if (position >= filterFriendList.size())
                return null;

            return filterFriendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    class ViewHolder {
        SelectableRoundedImageView portraitImageView;
        LinearLayout nameDisplayNameLinearLayout;
        TextView nameTextView;
        TextView displayNameTextView;
        TextView nameSingleTextView;
    }

    @Override
    protected void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        super.onDestroy();
    }
}
