package cn.rongcloud.im.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.linkpreview.LinkPreviewCallback;
import cn.rongcloud.im.ui.widget.linkpreview.SourceContent;
import cn.rongcloud.im.ui.widget.linkpreview.TextCrawler;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;

/**
 * Created by AMing on 16/7/12.
 * Company RongCloud
 */
public class SharedReceiverActivity extends BaseActivity {

    private List<Conversation> conversationsList;
    private List<Groups> mGroupData;
    private List<Friend> mFriendData;
    private List<NewConversation> newConversationsList = new ArrayList<>();
    private ListView shareListView;
    private String mTitle;
    private boolean mIsPlainNormalText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            /** 截获 Intent 部分 **/
            try {
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    String linkInfo = getIntent().getClipData().toString();
                    if (linkInfo != null) {
                        if (linkInfo.contains("file://")) {
                            NToast.shortToast(mContext, "暂时不支持");
                            finish();
                            return;
                        }
                        String titleInfo = null;
                        int start = linkInfo.indexOf("T:");
                        int end = linkInfo.indexOf("http");
                        if (end > start) {
                            titleInfo = linkInfo.substring(start, end).trim();
                        }
                        if (titleInfo != null && titleInfo.length() > 3) {
                            mTitle = titleInfo.substring(3, titleInfo.length());
                        }
                    }
                }

                if (getIntent().getExtras() != null) {
                    String shareVia = (String) getIntent().getExtras().get(Intent.EXTRA_TEXT);
                    if (shareVia != null) {
                        if (shareVia.toLowerCase().contains("http://")
                                || shareVia.toLowerCase().contains("https://")) {
                            TextCrawler textCrawler = new TextCrawler();
                            textCrawler.makePreview(callback, shareVia);
                        } else {
                            mIsPlainNormalText = true;
                            description = shareVia;
                        }
                    } else {
                        NToast.shortToast(mContext, "暂时不支持");
                        finish();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_share_receiver);
        initView();
        LoadDialog.show(mContext);

        /** 异步取数据的部分 包括群组数据 和 单聊数据 **/
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
            getConversations();
        } else {
            String cacheToken = getSharedPreferences("config", MODE_PRIVATE).getString("loginToken", "");
            if (!TextUtils.isEmpty(cacheToken)) {
                RongIM.connect(cacheToken, new RongIMClient.ConnectCallback() {
                    @Override
                    public void onTokenIncorrect() {

                    }

                    @Override
                    public void onSuccess(String s) {
                        getConversations();
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
            }
        }
    }

    private void initView() {
        setTitle(R.string.select_contact);
        shareListView = (ListView) findViewById(R.id.share_listview);
        shareListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
                    Toast.makeText(mContext, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newConversationsList != null) {
                    final AlertDialog dlg = new AlertDialog.Builder(mContext).create();
                    dlg.show();
                    dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    final Window window = dlg.getWindow();
                    window.setContentView(R.layout.share_dialog);
                    Button ok = (Button) window.findViewById(R.id.share_ok);
                    Button cancel = (Button) window.findViewById(R.id.share_cancel);
                    TextView content = (TextView) window.findViewById(R.id.share_cotent);
                    TextView from = (TextView) window.findViewById(R.id.share_from);
                    ImageView image = (ImageView) window.findViewById(R.id.share_image);
                    TextView title = (TextView) window.findViewById(R.id.share_title);

                    if (!TextUtils.isEmpty(description)) {
                        content.setText(description);
                    }
                    if (mIsPlainNormalText) {
                        title.setVisibility(View.GONE);
                        image.setVisibility(View.GONE);
                        from.setVisibility(View.GONE);
                    } else {
                        if (TextUtils.isEmpty(mTitle)) {
                            title.setText(titleString);
                        } else {
                            title.setText(mTitle);
                        }
                        if (!TextUtils.isEmpty(imageString)) {
                            ImageLoader.getInstance().displayImage(imageString, image);
                        }
                        if (!TextUtils.isEmpty(fromString)) {
                            from.setText(getString(R.string.ac_share_receiver_from, fromString));
                        } else {
                            from.setVisibility(View.GONE);
                        }
                    }
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Conversation.ConversationType conversationType = newConversationsList.get(position).getmConversationType();
                            String targetId = newConversationsList.get(position).getTargetId();
                            LoadDialog.show(mContext);
                            final EditText say = (EditText) window.findViewById(R.id.share_say);
                            String remindText = say.getText().toString();
                            if (!TextUtils.isEmpty(remindText)) {
                                sendRemindMessage(conversationType, targetId, remindText);
                            }

                            NLog.e("share", "分享:" + titleString + "\n" + finalUri + "\n" + "来自:" + fromString);
                            if (mIsPlainNormalText) {
                                sendShareMessage(conversationType, targetId, TextMessage.obtain(description));
                            } else {
                                RichContentMessage richContentMessage;
                                if (TextUtils.isEmpty(mTitle)) {
                                    richContentMessage = RichContentMessage.obtain(titleString, TextUtils.isEmpty(description) ? finalUri : description, imageString, finalUri);
                                } else {
                                    richContentMessage = RichContentMessage.obtain(mTitle, TextUtils.isEmpty(description) ? finalUri : description, imageString, finalUri);
                                }
                                sendShareMessage(conversationType, targetId, richContentMessage);
                            }
                            dlg.cancel();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dlg.cancel();
                        }
                    });
                }
            }
        });
    }

    private void sendShareMessage(Conversation.ConversationType conversationType, String targetId, MessageContent content) {
        RongIM.getInstance().sendMessage(conversationType, targetId, content, null, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                NLog.e("share", e.getValue());
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "分享失败");
            }

            @Override
            public void onSuccess(Integer integer) {
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "分享成功");
            }
        });
    }

    private void sendRemindMessage(Conversation.ConversationType conversationType, String targetId, String content) {
        RongIM.getInstance().sendMessage(conversationType, targetId, TextMessage.obtain(content), null, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer messageId, RongIMClient.ErrorCode e) {

            }

            @Override
            public void onSuccess(Integer integer) {

            }
        });
    }


    class ShareAdapter extends BaseAdapter {

        private List<NewConversation> list;

        private Context context;

        private ViewHolder holder;


        public ShareAdapter(List<NewConversation> newConversationsList, Context mContext) {
            this.list = newConversationsList;
            this.context = mContext;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.share_item, parent, false);
                holder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.share_icon);
                holder.title = (TextView) convertView.findViewById(R.id.share_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            NewConversation bean = list.get(position);
            ImageLoader.getInstance().displayImage(bean.getPortraitUri(), holder.mImageView, App.getOptions());
            holder.title.setText(bean.getTitle());
            return convertView;
        }
    }


    final static class ViewHolder {
        /**
         * 头像
         */
        SelectableRoundedImageView mImageView;

        TextView title;
    }


    private void getConversations() {
        final Conversation.ConversationType[] conversationTypes = {
            Conversation.ConversationType.PRIVATE,
            Conversation.ConversationType.GROUP,
        };
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
            RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
                @Override
                public void onSuccess(List<Conversation> conversations) {
                    conversationsList = conversations;
                    getGroups();
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {

                }
            }, conversationTypes);
        }

    }


    private void getGroups() {
        SealUserInfoManager.getInstance().openDB();
        SealUserInfoManager.getInstance().getGroups(new SealUserInfoManager.ResultCallback<List<Groups>>() {
            @Override
            public void onSuccess(List<Groups> groupses) {
                mGroupData  = groupses;
                getFriends();
            }

            @Override
            public void onError(String errString) {

            }
        });
    }

    private void getFriends() {
        SealUserInfoManager.getInstance().getFriends(new SealUserInfoManager.ResultCallback<List<Friend>>() {
            @Override
            public void onSuccess(List<Friend> friendList) {
                mFriendData = friendList;
                //双重循环过滤已经被解散或者退出的群组数据
                List<Conversation> tempList = new ArrayList<>();
                if (conversationsList != null) {
                    for (Conversation conversation : conversationsList) {
                        if (conversation.getConversationType().equals(Conversation.ConversationType.GROUP)) {
                            for (Groups group : mGroupData) {
                                if (group.getGroupsId().equals(conversation.getTargetId())) {
                                    tempList.add(conversation);
                                }
                            }
                        } else { // 后期如果做删除好友接口后也可能需要处理 private 类型的数据
                            tempList.add(conversation);
                        }
                    }
                }

                if (tempList.size() > 0) {
                    for (Conversation conversation : tempList) {
                        if (conversation.getConversationType().equals(Conversation.ConversationType.PRIVATE)) {//TODO 会话中包含自己本身会 crash
                            Friend friend = getFriendInfoById(conversation.getSenderUserId());
                            String portraitUri = null;
                            if (friend == null)
                                continue;
                            if (friend != null && !TextUtils.isEmpty(friend.getPortraitUri().toString())) {
                                portraitUri = friend.getPortraitUri().toString();
                            } else {
                                if (friend != null) {
                                    portraitUri = RongGenerate.generateDefaultAvatar(friend.getName(), friend.getUserId());
                                }

                            }
                            newConversationsList.add(
                                new NewConversation(Conversation.ConversationType.PRIVATE,
                                                    conversation.getTargetId(),
                                                    portraitUri,
                                                    friend == null ? null : friend.getName()));
                        } else {
                            Groups groups = getGroupInfoById(conversation.getTargetId());
                            String portraitUri = null;
                            if (groups == null)
                                continue;
                            if (groups != null && !TextUtils.isEmpty(groups.getPortraitUri())) {
                                portraitUri = groups.getPortraitUri();
                            } else {
                                if (groups != null) {
                                    portraitUri = RongGenerate.generateDefaultAvatar(groups.getName(), groups.getGroupsId());
                                }
                            }
                            newConversationsList.add(
                                new NewConversation(Conversation.ConversationType.GROUP,
                                                    conversation.getTargetId(),
                                                    portraitUri,
                                                    groups == null ? null : groups.getName()));
                        }
                    }
                    if (newConversationsList != null && newConversationsList.size() > 0) {
                        ShareAdapter shareAdapter = new ShareAdapter(newConversationsList, mContext);
                        shareListView.setAdapter(shareAdapter);
                        LoadDialog.dismiss(mContext);
                    }
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
    }

    class NewConversation {
        Conversation.ConversationType mConversationType;
        String targetId;
        String portraitUri;
        String title;

        public NewConversation(Conversation.ConversationType mConversationType, String targetId, String portraitUri, String title) {
            this.mConversationType = mConversationType;
            this.targetId = targetId;
            this.portraitUri = portraitUri;
            this.title = title;
        }

        public Conversation.ConversationType getmConversationType() {
            return mConversationType;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public String getPortraitUri() {
            return portraitUri;
        }

        public void setPortraitUri(String portraitUri) {
            this.portraitUri = portraitUri;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }


    private Friend getFriendInfoById(String userId) {
        if (mFriendData != null) {
            for (Friend friend : mFriendData) {
                if (friend.getUserId().equals(userId)) {
                    return friend;
                }
            }
        }
        return null;
    }

    private Groups getGroupInfoById(String groupId) {
        if (mGroupData != null) {
            for (Groups group : mGroupData) {
                if (group.getGroupsId().equals(groupId)) {
                    return group;
                }
            }
        }
        return null;
    }

    private String imageString;

    private String fromString;

    private String description;

    private String titleString;

    private String finalUri;
    /** Callback to update your view. Totally customizable. */
    /** onPre() will be called before the crawling. onPos() after. */
    /**
     * You can customize this to update your view
     */
    private LinkPreviewCallback callback = new LinkPreviewCallback() {

        @Override
        public void onPre() {
            NLog.e("share", "onPre");
            LoadDialog.show(mContext);
        }

        @Override
        public void onPos(SourceContent sourceContent, boolean isNull) {
            if (sourceContent != null) {
                NLog.e("share", sourceContent.getImages().size());
                NLog.e("share", sourceContent.getCannonicalUrl());
                NLog.e("share", sourceContent.getDescription());
                NLog.e("share", sourceContent.getFinalUrl());
                NLog.e("share", sourceContent.getTitle());

                if (sourceContent.getImages().size() > 0) {
                    imageString = sourceContent.getImages().get(0);
                }
                fromString = sourceContent.getCannonicalUrl();
                description = sourceContent.getDescription();
                titleString = sourceContent.getTitle();
                finalUri = sourceContent.getFinalUrl();
                LoadDialog.dismiss(mContext);
            }
        }
    };

}
