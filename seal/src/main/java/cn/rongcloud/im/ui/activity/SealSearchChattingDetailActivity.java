package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.SearchConversationResult;
import io.rong.imlib.model.UserInfo;

/**
 * Created by tiankui on 16/10/8.
 */

public class SealSearchChattingDetailActivity extends Activity {
    private static final int SEARCH_TYPE_FLAG = 1;

    private TextView mTitleTextView;
    private EditText mSearchEditText;
    private ListView mChattingRecordsListView;
    private TextView mSearchNoResultsTextView;
    private ImageView mPressBackImageView;

    private String mFilterString;
    private List<Message> mMessages;
    private SealSearchConversationResult mResult;
    private int mFlag;
    private int mMatchCount;
    private int mMessageShowCount;
    private Message mLastMessage;
    private List<Message> mAdapterMessages;
    private ChattingRecordsAdapter mAdapter;
    private boolean mCompleteFlag;
    private long mLastMessagSentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seal_search_more_info);
        Intent intent = getIntent();
        mFilterString = intent.getStringExtra("filterString");
        mResult = intent.getParcelableExtra("searchConversationResult");
        mFlag = intent.getIntExtra("flag", -1);
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
        mCompleteFlag = true;
        if (mFlag == SEARCH_TYPE_FLAG) {
            mTitleTextView.setVisibility(View.GONE);
        }
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFilterString = s.toString();
                final Conversation conversation = mResult.getConversation();
                RongIMClient.getInstance().searchConversations(mFilterString, new Conversation.ConversationType[] {conversation.getConversationType()}, new String[] {"RC:TxtMsg", "RC:ImgTextMsg", "RC:FileMsg"}, new RongIMClient.ResultCallback<List<SearchConversationResult>>() {
                    @Override
                    public void onSuccess(List<SearchConversationResult> searchConversationResults) {
                        for (SearchConversationResult result : searchConversationResults) {
                            if (result.getConversation().getTargetId().equals(conversation.getTargetId())) {
                                mMatchCount = result.getMatchCount();
                                if (result.getMatchCount() == 0) {
                                    mTitleTextView.setVisibility(View.GONE);
                                    mChattingRecordsListView.setVisibility(View.GONE);
                                    mSearchNoResultsTextView.setVisibility(View.VISIBLE);
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                    spannableStringBuilder.append(getString(R.string.ac_search_no_result_pre));
                                    SpannableStringBuilder colorFilterStr = new SpannableStringBuilder(mFilterString);
                                    colorFilterStr.setSpan(new ForegroundColorSpan(Color.parseColor("#0099ff")), 0, mFilterString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                    spannableStringBuilder.append(colorFilterStr);
                                    spannableStringBuilder.append(getString(R.string.ac_search_no_result_suffix));
                                    mSearchNoResultsTextView.setText(spannableStringBuilder);
                                } else {
                                    mTitleTextView.setVisibility(View.VISIBLE);
                                    mSearchNoResultsTextView.setVisibility(View.GONE);
                                    mChattingRecordsListView.setVisibility(View.VISIBLE);
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                    spannableStringBuilder.append(getString(R.string.ac_search_chat_detail, result.getMatchCount()));
                                    SpannableStringBuilder colorFilterStr = new SpannableStringBuilder(mFilterString);
                                    colorFilterStr.setSpan(new ForegroundColorSpan(Color.parseColor("#0099ff")), 0, mFilterString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                    spannableStringBuilder.append(colorFilterStr);
                                    spannableStringBuilder.append(getString(R.string.ac_search_chat_detail_three));
                                    mTitleTextView.setText(spannableStringBuilder);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
                RongIMClient.getInstance().searchMessages(conversation.getConversationType(),
                conversation.getTargetId(), mFilterString, 50, 0, new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        mMessageShowCount = messages.size();
                        mMessages = messages;
                        if (mMessages.size() == 0) {
                            mTitleTextView.setVisibility(View.GONE);
                            mChattingRecordsListView.setVisibility(View.GONE);
                            mSearchNoResultsTextView.setVisibility(View.VISIBLE);
                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                            spannableStringBuilder.append(getString(R.string.ac_search_no_result_pre));
                            SpannableStringBuilder colorFilterStr = new SpannableStringBuilder(mFilterString);
                            colorFilterStr.setSpan(new ForegroundColorSpan(Color.parseColor("#0099ff")), 0, mFilterString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            spannableStringBuilder.append(colorFilterStr);
                            spannableStringBuilder.append(getString(R.string.ac_search_no_result_suffix));
                            mSearchNoResultsTextView.setText(spannableStringBuilder);
                        } else {
                            mTitleTextView.setVisibility(View.VISIBLE);
                            mSearchNoResultsTextView.setVisibility(View.GONE);
                            mChattingRecordsListView.setVisibility(View.VISIBLE);
                            mAdapterMessages = messages;
                            mAdapter = new ChattingRecordsAdapter();
                            mChattingRecordsListView.setAdapter(mAdapter);
                            mLastMessage = messages.get(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        if (mFilterString.equals("")) {
                            mChattingRecordsListView.setVisibility(View.GONE);
                            mTitleTextView.setVisibility(View.GONE);
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
                SealSearchChattingDetailActivity.this.finish();
            }
        });

        mChattingRecordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof Message) {
                    Message message = (Message)object;
                    Conversation conversation = mResult.getConversation();
                    RongIM.getInstance().startConversation(SealSearchChattingDetailActivity.this, conversation.getConversationType(), conversation.getTargetId(), mResult.getTitle(),mAdapterMessages.get(position).getSentTime());
                }
            }
        });
        //分页加载,每页加载50条记录;
        mChattingRecordsListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0)
                {
                    if (mMatchCount > mMessageShowCount && mCompleteFlag)
                    {
                        mCompleteFlag = false;
                        loadMoreChattingRecords();
                    }
                }
            }
        });

        mSearchEditText.setText(mFilterString);
    }

    private void loadMoreChattingRecords() {
        Conversation conversation = mResult.getConversation();
        RongIMClient.getInstance().searchMessages(conversation.getConversationType(),
        conversation.getTargetId(), mFilterString, 50, mLastMessage.getSentTime(), new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                mCompleteFlag = true;
                mMessageShowCount = mMessageShowCount + messages.size();
                if (mMatchCount >= mMessageShowCount) {
                    mAdapterMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged();
                }
                if (messages.size() > 0) {
                    mLastMessage = messages.get(messages.size() - 1);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });

    }

    private class ChattingRecordsAdapter extends BaseAdapter {

        public ChattingRecordsAdapter() {

        }
        @Override
        public int getCount() {
            if (mAdapterMessages != null) {
                return mAdapterMessages.size();
            }
            return 0;
        }
        @Override
        public Object getItem(int position) {
            if (mAdapterMessages == null)
                return null;

            if (position >= mAdapterMessages.size())
                return null;

            return mAdapterMessages.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ChattingRecordsViewHolder viewHolder;
            Message message = (Message) getItem(position);
            if (convertView == null) {
                viewHolder = new ChattingRecordsViewHolder();
                convertView = View.inflate(getBaseContext(), R.layout.item_filter_chatting_records_list, null);
                viewHolder.portraitImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.item_iv_record_image);
                viewHolder.chatDetailLinearLayout = (LinearLayout) convertView.findViewById(R.id.item_ll_chatting_records_detail);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_tv_chat_name);
                viewHolder.chatRecordsDetailTextView = (TextView) convertView.findViewById(R.id.item_tv_chatting_records_detail);
                viewHolder.chatRecordsDateTextView = (TextView)convertView.findViewById(R.id.item_tv_chatting_records_date);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChattingRecordsViewHolder) convertView.getTag();
            }

            String id = mResult.getId();
            String title = mResult.getTitle();
            UserInfo userInfo = new UserInfo(id, title, Uri.parse(mResult.getPortraitUri()));
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(userInfo);
            ImageLoader.getInstance().displayImage(portraitUri, viewHolder.portraitImageView, App.getOptions());
            viewHolder.nameTextView.setText(title);
            viewHolder.chatRecordsDetailTextView.setText(CharacterParser.getInstance().getColoredChattingRecord(mFilterString, message.getContent()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = simpleDateFormat.format(new Date(message.getSentTime()));
            String formatDate = date.replace("-", "/");
            viewHolder.chatRecordsDateTextView.setText(formatDate);
            return convertView;
        }
    }

    class ChattingRecordsViewHolder {
        SelectableRoundedImageView portraitImageView;
        LinearLayout chatDetailLinearLayout;
        TextView nameTextView;
        TextView chatRecordsDetailTextView;
        TextView chatRecordsDateTextView;
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
