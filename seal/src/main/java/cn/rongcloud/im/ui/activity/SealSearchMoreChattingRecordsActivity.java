package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.FriendDao;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.db.GroupsDao;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.SearchConversationResult;
import io.rong.imlib.model.UserInfo;

/**
 * Created by tiankui on 16/10/8.
 */

public class SealSearchMoreChattingRecordsActivity extends Activity {

    private static final int SEARCH_TYPE_FLAG = 1;

    private TextView mTitleTextView;
    private EditText mSearchEditText;
    private ListView mChattingRecordsListView;
    private TextView mSearchNoResultsTextView;
    private ImageView mPressBackImageView;

    private String mFilterString;
    private ArrayList<SearchConversationResult> mSearchConversationResultsArrayList;
    private List<SearchConversationResult> mSearchConversationResultsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seal_search_more_info);
        Intent intent = getIntent();
        mFilterString = intent.getStringExtra("filterString");
        mSearchConversationResultsArrayList = intent.getParcelableArrayListExtra("conversationRecords");
        initView();
        initData();
    }
    private void initView() {
        mSearchEditText = (EditText) findViewById(R.id.ac_et_search);
        mTitleTextView = (TextView)findViewById(R.id.ac_tv_seal_search_more_info_title);
        mChattingRecordsListView = (ListView)findViewById(R.id.ac_lv_more_info_list_detail_info);
        mSearchNoResultsTextView = (TextView)findViewById(R.id.ac_tv_search_no_results);
        mPressBackImageView = (ImageView) findViewById(R.id.ac_iv_press_back);

    }

    private void initData() {
        mTitleTextView.setText(getString(R.string.ac_search_chatting_records));
        mSearchEditText.setText(mFilterString);
        mSearchConversationResultsList = new ArrayList<>();

        ChattingRecordsAdapter adapter = new ChattingRecordsAdapter(mSearchConversationResultsArrayList);
        mChattingRecordsListView.setAdapter(adapter);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFilterString = s.toString();
                RongIMClient.getInstance().searchConversations(mFilterString,
                        new Conversation.ConversationType[] {Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP},
                new String[] {"RC:TxtMsg", "RC:ImgTextMsg", "RC:FileMsg"}, new RongIMClient.ResultCallback<List<SearchConversationResult>>() {
                    @Override
                    public void onSuccess(List<SearchConversationResult> searchConversationResults) {
                        mSearchConversationResultsList = searchConversationResults;

                        if (searchConversationResults.size() > 0) {
                            mChattingRecordsListView.setVisibility(View.VISIBLE);
                            mTitleTextView.setVisibility(View.VISIBLE);

                        } else {
                            mChattingRecordsListView.setVisibility(View.GONE);
                            mTitleTextView.setVisibility(View.GONE);
                        }
                        if (mFilterString.equals("")) {
                            mChattingRecordsListView.setVisibility(View.GONE);
                            mTitleTextView.setVisibility(View.GONE);
                        }
                        if (mSearchConversationResultsList.size() == 0) {
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
                        ChattingRecordsAdapter chattingRecordsAdapter = new ChattingRecordsAdapter(mSearchConversationResultsList);
                        mChattingRecordsListView.setAdapter(chattingRecordsAdapter);

                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        if (mFilterString.equals("")) {
                            mChattingRecordsListView.setVisibility(View.GONE);
                            mTitleTextView.setVisibility(View.GONE);
                        }
                        if (mSearchConversationResultsList.size() == 0) {
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
                });

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
                SealSearchMoreChattingRecordsActivity.this.finish();
            }
        });

        mChattingRecordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object selectObj = parent.getItemAtPosition(position);
                if (selectObj instanceof SealSearchConversationResult) {
                    SealSearchConversationResult result = (SealSearchConversationResult) selectObj;
                    int count = result.getMatchCount();
                    Conversation conversation = result.getConversation();
                    if (count == 1) {
                        RongIM.getInstance().startConversation(SealSearchMoreChattingRecordsActivity.this, conversation.getConversationType(), conversation.getTargetId(), result.getTitle(),result.getConversation().getSentTime());
                    } else {
                        Intent intent = new Intent(SealSearchMoreChattingRecordsActivity.this, SealSearchChattingDetailActivity.class);
                        intent.putExtra("filterString", mFilterString);
                        intent.putExtra("searchConversationResult", result);
                        intent.putExtra("flag", SEARCH_TYPE_FLAG);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private class ChattingRecordsAdapter extends BaseAdapter {

        private List<SealSearchConversationResult> searchConversationResults;

        public ChattingRecordsAdapter(List<SearchConversationResult> searchConversationResults) {
            this.searchConversationResults = CommonUtils.convertSearchResult(searchConversationResults);
        }

        @Override
        public int getCount() {
            if (searchConversationResults != null) {
                return searchConversationResults.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (searchConversationResults == null)
                return null;

            if (position >= searchConversationResults.size())
                return null;

            return searchConversationResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ChattingRecordsViewHolder viewHolder;
            final SealSearchConversationResult searchResult = (SealSearchConversationResult) getItem(position);
            Conversation conversation = searchResult.getConversation();
            final int searchResultCount = searchResult.getMatchCount();
            if (convertView == null) {
                viewHolder = new ChattingRecordsViewHolder();
                convertView = View.inflate(getBaseContext(), R.layout.item_filter_chatting_records_list, null);
                viewHolder.portraitImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.item_iv_record_image);
                viewHolder.chatDetailLinearLayout = (LinearLayout) convertView.findViewById(R.id.item_ll_chatting_records_detail);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_tv_chat_name);
                viewHolder.chatRecordsDetailTextView = (TextView) convertView.findViewById(R.id.item_tv_chatting_records_detail);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChattingRecordsViewHolder) convertView.getTag();
            }
            if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
                Friend friend = DBManager.getInstance().getDaoSession().getFriendDao().queryBuilder().where(FriendDao.Properties.UserId.eq(conversation.getTargetId())).unique();
                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                String currentUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
                String currentUserName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
                String currentUserPortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
                if (friend != null) {
                    searchResult.setId(friend.getUserId());
                    String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(friend);
                    searchResult.setPortraitUri(portraitUri);
                    ImageLoader.getInstance().displayImage(portraitUri, viewHolder.portraitImageView, App.getOptions());
                    if (!TextUtils.isEmpty(friend.getDisplayName())) {
                        searchResult.setTitle(friend.getDisplayName());
                        viewHolder.nameTextView.setText(friend.getDisplayName());
                    } else {
                        searchResult.setTitle(friend.getName());
                        viewHolder.nameTextView.setText(friend.getName());
                    }
                } else if (conversation.getTargetId().equals(currentUserId)) {
                    searchResult.setId(currentUserId);
                    UserInfo userInfo = new UserInfo(currentUserId, currentUserName, Uri.parse(currentUserPortrait));
                    String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(userInfo);
                    searchResult.setPortraitUri(portraitUri);
                    ImageLoader.getInstance().displayImage(portraitUri, viewHolder.portraitImageView, App.getOptions());
                    if (!TextUtils.isEmpty(currentUserName)) {
                        searchResult.setTitle(currentUserName);
                        viewHolder.nameTextView.setText(currentUserName);
                    } else {
                        searchResult.setTitle(currentUserId);
                        viewHolder.nameTextView.setText(currentUserId);
                    }
                } else {
                    UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(conversation.getTargetId());
                    String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(userInfo);
                    searchResult.setPortraitUri(portraitUri);
                    searchResult.setId(conversation.getTargetId());
                    ImageLoader.getInstance().displayImage(portraitUri, viewHolder.portraitImageView, App.getOptions());
                    if (userInfo != null) {
                        if (!TextUtils.isEmpty(userInfo.getName())) {
                            searchResult.setTitle(userInfo.getName());
                            viewHolder.nameTextView.setText(userInfo.getName());
                        } else {
                            searchResult.setTitle(userInfo.getUserId());
                            viewHolder.nameTextView.setText(userInfo.getUserId());
                        }
                    } else {
                        searchResult.setTitle(conversation.getTargetId());
                        viewHolder.nameTextView.setText(conversation.getTargetId());
                    }
                }

            }
            if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
                Groups groupInfo = DBManager.getInstance().getDaoSession().getGroupsDao().queryBuilder().where(GroupsDao.Properties.GroupsId.eq(conversation.getTargetId())).unique();
                if (groupInfo != null) {
                    String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(groupInfo);
                    searchResult.setId(groupInfo.getGroupsId());
                    searchResult.setPortraitUri(portraitUri);
                    ImageLoader.getInstance().displayImage(portraitUri, viewHolder.portraitImageView, App.getOptions());
                    if (!TextUtils.isEmpty(groupInfo.getName())) {
                        searchResult.setTitle(groupInfo.getName());
                        viewHolder.nameTextView.setText(groupInfo.getName());
                    } else {
                        searchResult.setTitle(groupInfo.getGroupsId());
                        viewHolder.nameTextView.setText(groupInfo.getGroupsId());
                    }
                }
            }
            if (searchResultCount == 1) {
                viewHolder.chatRecordsDetailTextView.setText(CharacterParser.getInstance().getColoredChattingRecord(mFilterString, searchResult.getConversation().getLatestMessage()));
            } else {
                viewHolder.chatRecordsDetailTextView.setText(getResources().getString(R.string.search_item_chat_records, searchResultCount));
            }
            return convertView;
        }
    }

    class ChattingRecordsViewHolder {
        SelectableRoundedImageView portraitImageView;
        LinearLayout chatDetailLinearLayout;
        TextView nameTextView;
        TextView chatRecordsDetailTextView;
    }

    @Override
    protected void onResume() {
        mSearchEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mSearchEditText, 0);
        super.onResume();
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        super.onPause();
    }
}
