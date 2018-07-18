package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.db.GroupMemberDao;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.db.GroupsDao;
import cn.rongcloud.im.model.SearchResult;
import cn.rongcloud.im.ui.adapter.GroupListAdapter;
import de.greenrobot.dao.query.QueryBuilder;
import io.rong.imkit.RongIM;

/**
 * Created by tiankui on 16/9/20.
 */
public class SealSearchMoreGroupActivity extends Activity {

    private EditText mSearchEditText;
    private ListView mGroupsListView;
    private TextView mSearchNoResultsTextView;
    private ImageView mPressBackImageView;
    private LinearLayout mGroupListResultsLinearLayout;

    private String mFilterString;
    private List<String> mFilterGroupId;

    private AsyncTask mAsyncTask;
    private ThreadPoolExecutor mExecutor;


    private static final String SQL_DISTINCT_GROUP_ID = "SELECT DISTINCT " + GroupMemberDao.Properties.GroupId.columnName + " FROM " + GroupMemberDao.TABLENAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_groups_info);

        Intent intent = getIntent();
        mFilterString = intent.getStringExtra("filterString");
        mFilterGroupId = intent.getStringArrayListExtra("filterGroupId");
        initView();
        initData();

    }
    public void initView() {
        mSearchEditText = (EditText)findViewById(R.id.ac_et_search);
        mGroupsListView = (ListView)findViewById(R.id.ac_lv_group_list_detail_info);
        mSearchNoResultsTextView = (TextView)findViewById(R.id.ac_tv_search_no_results);
        mPressBackImageView = (ImageView)findViewById(R.id.ac_iv_press_back);
        mGroupListResultsLinearLayout = (LinearLayout)findViewById(R.id.ac_ll_group_list_result);
        mSearchEditText.setText(mFilterString);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFilterString = s.toString();
                mAsyncTask = new AsyncTask<String, Void, SearchResult>() {
                    @Override
                    protected void onPreExecute() {
                    }

                    @Override
                    protected SearchResult doInBackground(String... params) {
                        return filterInfo(mFilterString);
                    }

                    @Override
                    protected void onPostExecute(SearchResult searchResult) {

                        if (searchResult.getFilterStr().equals(mFilterString)) {

                            Map<String, List<GroupMember>> filterGroupNameListMap = searchResult.getFilterGroupNameListMap();
                            Map<String, List<GroupMember>> filterGroupMemberNameListMap = searchResult.getFilterGroupMemberNameListMap();
                            List<String> filterGroupId = searchResult.getFilterGroupId();
                            if (filterGroupId.size() > 0) {
                                mGroupListResultsLinearLayout.setVisibility(View.VISIBLE);
                                mGroupsListView.setVisibility(View.VISIBLE);
                                GroupListAdapter groupListAdapter = new GroupListAdapter(SealSearchMoreGroupActivity.this, filterGroupId, filterGroupNameListMap, filterGroupMemberNameListMap, mFilterString);
                                mGroupsListView.setAdapter(groupListAdapter);
                            } else {
                                mGroupListResultsLinearLayout.setVisibility(View.GONE);
                                mGroupsListView.setVisibility(View.GONE);
                            }
                            if (mFilterString.equals("")) {
                                mSearchNoResultsTextView.setVisibility(View.GONE);
                            }
                            if ( filterGroupId.size() == 0 ) {
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
                        mSearchEditText.setText("");
                        mSearchEditText.clearFocus();
                        return true;
                    }
                }
                return false;
            }
        });

        mPressBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SealSearchMoreGroupActivity.this.finish();
            }
        });

        mGroupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object selectObject = parent.getItemAtPosition(position);
                if (selectObject instanceof String) {
                    String groupId = (String) selectObject;
                    Groups groupInfo = DBManager.getInstance().getDaoSession().getGroupsDao().queryBuilder().where(GroupsDao.Properties.GroupsId.eq(groupId)).unique();
                    if (groupInfo != null) {
                        RongIM.getInstance().startGroupChat(SealSearchMoreGroupActivity.this, groupInfo.getGroupsId(), groupInfo.getName());
                    }
                }
            }
        });

    }
    public void initData() {
        filterList(mFilterString);

        mExecutor = new ThreadPoolExecutor(3, 5, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    }
    private void filterList(String filterStr) {
        Map<String, List<GroupMember>> filterGroupNameListMap = new HashMap<>();
        Map<String, List<GroupMember>> filterGroupMemberNameListMap = new HashMap<>();
        for (String groupId : mFilterGroupId) {
            QueryBuilder groupNameQueryBuilder = DBManager.getInstance().getDaoSession().getGroupMemberDao().queryBuilder();
            List<GroupMember> filterGroupNameList = groupNameQueryBuilder.where(GroupMemberDao.Properties.GroupId.eq(groupId),
                                                    groupNameQueryBuilder.or(GroupMemberDao.Properties.GroupName.like("%" + mFilterString + "%"),
                                                            GroupMemberDao.Properties.GroupNameSpelling.like(mFilterString + "%"))).orderAsc(GroupMemberDao.Properties.GroupNameSpelling).build().list();
            QueryBuilder groupMemberNameQueryBuilder = DBManager.getInstance().getDaoSession().getGroupMemberDao().queryBuilder();
            List<GroupMember> filterGroupMemberNameList = groupMemberNameQueryBuilder.where(GroupMemberDao.Properties.GroupId.eq(groupId),
                    groupMemberNameQueryBuilder.or(GroupMemberDao.Properties.Name.like("%" + mFilterString + "%"),
                                                   GroupMemberDao.Properties.NameSpelling.like(mFilterString + "%"),
                                                   GroupMemberDao.Properties.DisplayName.like("%" + mFilterString + "%"),
                                                   GroupMemberDao.Properties.DisplayNameSpelling.like(mFilterString + "%"))
                                                                                           ).orderAsc(GroupMemberDao.Properties.NameSpelling, GroupMemberDao.Properties.DisplayNameSpelling).build().list();
            if (filterGroupNameList.size() != 0) {
                filterGroupNameListMap.put(groupId, filterGroupNameList);
            } else {
                filterGroupNameListMap.put(groupId, null);
            }
            if (filterGroupMemberNameList.size() != 0) {
                filterGroupMemberNameListMap.put(groupId, filterGroupMemberNameList);
            } else {
                filterGroupMemberNameListMap.put(groupId, null);
            }
        }
        GroupListAdapter groupListAdapter = new GroupListAdapter(this, mFilterGroupId, filterGroupNameListMap, filterGroupMemberNameListMap, filterStr);
        mGroupsListView.setAdapter(groupListAdapter);
    }
    private synchronized SearchResult filterInfo(String filterStr) {

        List<String> filterGroupId = new ArrayList<>();
        Map<String, List<GroupMember>> filterGroupNameListMap = new HashMap<>();
        Map<String, List<GroupMember>> filterGroupMemberNameListMap = new HashMap<>();
        SearchResult searchResult = new SearchResult();

        if (filterStr.equals("")) {
            SearchResult result = new SearchResult();
            result.setFilterStr("");
            result.setFilterGroupId(filterGroupId);
            result.setFilterGroupNameListMap(filterGroupNameListMap);
            result.setFilterGroupNameListMap(filterGroupMemberNameListMap);
            return result;
        }
        if (filterStr.contains("'")) {
            SearchResult result = new SearchResult();
            result.setFilterStr(filterStr);
            result.setFilterGroupId(filterGroupId);
            result.setFilterGroupNameListMap(filterGroupNameListMap);
            result.setFilterGroupNameListMap(filterGroupMemberNameListMap);
            return result;
        }
        /**
         * 从数据库里边查询符合条件的数据
         */

        Cursor cursor = DBManager.getInstance().getDaoSession().getDatabase().rawQuery(SQL_DISTINCT_GROUP_ID
                        + " WHERE " + GroupMemberDao.Properties.GroupName.columnName + " LIKE " + "'" + "%" + filterStr + "%" + "'" + " or "
                        + GroupMemberDao.Properties.GroupNameSpelling.columnName + " like " + "'" + filterStr + "%" + "'" + " or "
                        + GroupMemberDao.Properties.Name.columnName + " like " + "'" + "%" + filterStr + "%" + "'" + " or "
                        + GroupMemberDao.Properties.NameSpelling.columnName + " like " + "'" + filterStr + "%" + "'" + " or "
                        + GroupMemberDao.Properties.DisplayName.columnName + " like " + "'" + "%" + filterStr + "%" + "'" + " or "
                        + GroupMemberDao.Properties.DisplayNameSpelling.columnName + " like " + "'" + filterStr + "%" + "'", null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    filterGroupId.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        for (String groupId : filterGroupId) {
            QueryBuilder groupNameQueryBuilder = DBManager.getInstance().getDaoSession().getGroupMemberDao().queryBuilder();
            List<GroupMember> filterGroupNameList = groupNameQueryBuilder.where(GroupMemberDao.Properties.GroupId.eq(groupId),
                                                    groupNameQueryBuilder.or(GroupMemberDao.Properties.GroupName.like("%" + filterStr + "%"),
                                                            GroupMemberDao.Properties.GroupNameSpelling.like(filterStr + "%"))).orderAsc(GroupMemberDao.Properties.GroupNameSpelling).build().list();
            QueryBuilder groupMemberNameQueryBuilder = DBManager.getInstance().getDaoSession().getGroupMemberDao().queryBuilder();
            List<GroupMember> filterGroupMemberNameList = groupMemberNameQueryBuilder.where(GroupMemberDao.Properties.GroupId.eq(groupId),
                    groupMemberNameQueryBuilder.or(GroupMemberDao.Properties.Name.like("%" + filterStr + "%"),
                                                   GroupMemberDao.Properties.NameSpelling.like(filterStr + "%"),
                                                   GroupMemberDao.Properties.DisplayName.like("%" + filterStr + "%"),
                                                   GroupMemberDao.Properties.DisplayNameSpelling.like(filterStr + "%"))
                                                                                           ).orderAsc(GroupMemberDao.Properties.NameSpelling, GroupMemberDao.Properties.DisplayNameSpelling).build().list();
            if (filterGroupNameList.size() != 0) {
                filterGroupNameListMap.put(groupId, filterGroupNameList);
            } else {
                filterGroupNameListMap.put(groupId, null);
            }
            if (filterGroupMemberNameList.size() != 0) {
                filterGroupMemberNameListMap.put(groupId, filterGroupMemberNameList);
            } else {
                filterGroupMemberNameListMap.put(groupId, null);
            }
        }
        searchResult.setFilterStr(filterStr);
        searchResult.setFilterGroupId(filterGroupId);
        searchResult.setFilterGroupNameListMap(filterGroupNameListMap);
        searchResult.setFilterGroupMemberNameListMap(filterGroupMemberNameListMap);
        return searchResult;
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
