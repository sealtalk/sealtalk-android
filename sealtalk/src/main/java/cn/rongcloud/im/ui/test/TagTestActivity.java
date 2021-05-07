package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.TagTestInputDialog;
import io.rong.common.RLog;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.ConversationTagInfo;
import io.rong.imlib.model.TagInfo;

public class TagTestActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "TagTestActivity";
    Button getConversation, addTag, delTag, updateTag, getTags, addConversationTag, removeConversationTag, removeTagsFromConversation,
            getConversationTags, getConversationTop, getConversationsFromTagByPage, getUnreadCountByTag, btn_set_con_top;
    private ArrayList<String> contentList = new ArrayList<>();
    private TagTestActivity.MyAdapter mAdapter;
    private ListView lvContent;
    private Handler handler = new Handler();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("消息扩展");
        setContentView(R.layout.activity_msg_tag);
        intData();
        initView();
        mContext = this;
    }


    private void intData() {
        Intent intent = getIntent();
        if (intent == null) return;
        lvContent = findViewById(R.id.lv_content);
        mAdapter = new TagTestActivity.MyAdapter();
        lvContent.setAdapter(mAdapter);

    }

    private void initView() {
        getConversation = findViewById(R.id.btn_conversation);
        getConversation.setOnClickListener(this);
        addTag = findViewById(R.id.btn_add_tag);
        addTag.setOnClickListener(this);
        delTag = findViewById(R.id.btn_del_tag);
        delTag.setOnClickListener(this);

        updateTag = findViewById(R.id.btn_update_tag);
        updateTag.setOnClickListener(this);
        getTags = findViewById(R.id.btn_get_tags);
        getTags.setOnClickListener(this);
        addConversationTag = findViewById(R.id.btn_add_tag_conversation);
        addConversationTag.setOnClickListener(this);
        removeConversationTag = findViewById(R.id.btn_remove_conversation_tag);
        removeConversationTag.setOnClickListener(this);
        removeTagsFromConversation = findViewById(R.id.btn_remove_conversation_tags);
        removeTagsFromConversation.setOnClickListener(this);
        getConversationTags = findViewById(R.id.btn_get_conversation_tag);
        getConversationTags.setOnClickListener(this);
        getConversationTop = findViewById(R.id.btn_get_conversation_top);
        getConversationTop.setOnClickListener(this);
        getConversationsFromTagByPage = findViewById(R.id.btn_get_conversation_tag_by_page);
        getConversationsFromTagByPage.setOnClickListener(this);
        getUnreadCountByTag = findViewById(R.id.btn_get_unread_count_for_tag);
        getUnreadCountByTag.setOnClickListener(this);
        btn_set_con_top = findViewById(R.id.btn_set_con_top);
        btn_set_con_top.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_conversation:
                getConversation();
                break;
            case R.id.btn_add_tag:
                TagTestInputDialog tagTestInputDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_SET);
                tagTestInputDialog.getSureView().setOnClickListener(v1 -> {
                    String id = tagTestInputDialog.getEtTagId().getText().toString();
                    String name = tagTestInputDialog.getEtTagName().getText().toString();
                    addTag(id, name);
                    tagTestInputDialog.cancel();
                });

                tagTestInputDialog.getCancelView().setOnClickListener(v12 -> tagTestInputDialog.cancel());
                tagTestInputDialog.show();

                break;
            case R.id.btn_del_tag:
                TagTestInputDialog testInputDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_DELETE);
                testInputDialog.getSureView().setOnClickListener(v1 -> {
                    String tagId = testInputDialog.getEtTagId().getText().toString();
                    deleteTag(tagId);
                    testInputDialog.cancel();
                });

                testInputDialog.getCancelView().setOnClickListener(v12 -> testInputDialog.cancel());
                testInputDialog.show();
                break;
            case R.id.btn_update_tag:
                TagTestInputDialog updateTag = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_SET);
                updateTag.getSureView().setOnClickListener(v1 -> {
                    String id = updateTag.getEtTagId().getText().toString();
                    String name = updateTag.getEtTagName().getText().toString();
                    updateTag(id, name);
                    updateTag.cancel();
                });

                updateTag.getCancelView().setOnClickListener(v12 -> updateTag.cancel());
                updateTag.show();
                break;

            case R.id.btn_get_tags:
                getAllTags();
                break;
            case R.id.btn_add_tag_conversation:
                TagTestInputDialog addDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_ADD_CONVERSATION);
                List<ConversationIdentifier> list = new ArrayList<>();
                addDialog.getSureView().setOnClickListener(v1 -> {
                    String id = addDialog.getEtTagId().getText().toString();
                    int type = Integer.parseInt(addDialog.getEtType().getText().toString());
                    String targetId = addDialog.getEtTargetId().getText().toString();
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier(Conversation.ConversationType.setValue(type), targetId);
                    list.add(conversationIdentifier);
                    addTagToConversation(id, list);
                    addDialog.cancel();
                });

                addDialog.getAddView().setOnClickListener(v13 -> {
                    int type = Integer.parseInt(addDialog.getEtType().getText().toString());
                    String targetId = addDialog.getEtTargetId().getText().toString();
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier(Conversation.ConversationType.setValue(type), targetId);
                    list.add(conversationIdentifier);
                    addDialog.getEtType().getText().clear();
                    addDialog.getEtTargetId().getText().clear();

                });

                addDialog.getCancelView().setOnClickListener(v12 -> addDialog.cancel());
                addDialog.show();
                break;
            case R.id.btn_remove_conversation_tag:
                TagTestInputDialog removeDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_ADD_CONVERSATION);
                List<ConversationIdentifier> removeList = new ArrayList<>();
                removeDialog.getSureView().setOnClickListener(v1 -> {
                    String id = removeDialog.getEtTagId().getText().toString();
                    int type = Integer.parseInt(removeDialog.getEtType().getText().toString());
                    String targetId = removeDialog.getEtTargetId().getText().toString();
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier(Conversation.ConversationType.setValue(type), targetId);
                    removeList.add(conversationIdentifier);
                    removeConversationTag(id, removeList);
                    removeDialog.cancel();
                });

                removeDialog.getAddView().setOnClickListener(v13 -> {
                    int type = Integer.parseInt(removeDialog.getEtType().getText().toString());
                    String targetId = removeDialog.getEtTargetId().getText().toString();
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier(Conversation.ConversationType.setValue(type), targetId);
                    removeList.add(conversationIdentifier);
                    removeDialog.getEtType().getText().clear();
                    removeDialog.getEtTargetId().getText().clear();

                });

                removeDialog.getCancelView().setOnClickListener(v12 -> removeDialog.cancel());
                removeDialog.show();
                break;
            case R.id.btn_remove_conversation_tags:
                List<String> ids = new ArrayList<>();
                TagTestInputDialog removeTags = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_REMOVE_TAGS);
                removeTags.getSureView().setOnClickListener(v1 -> {
                    String id = removeTags.getEtTagId().getText().toString();
                    int type = Integer.parseInt(removeTags.getEtType().getText().toString());
                    String targetId = removeTags.getEtTargetId().getText().toString();
                    ids.add(id);
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier(Conversation.ConversationType.setValue(type), targetId);
                    removeTagsFromConversation(conversationIdentifier, ids);
                    removeTags.cancel();
                });

                removeTags.getAddView().setOnClickListener(v13 -> {
                    String tagId = removeTags.getEtTagId().getText().toString();
                    ids.add(tagId);
                    removeTags.getEtTagId().getText().clear();

                });

                removeTags.getCancelView().setOnClickListener(v12 -> removeTags.cancel());
                removeTags.show();
                break;
            case R.id.btn_get_conversation_tag:
                TagTestInputDialog getConTags = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_GET_CONVERSATION_TAGS);
                getConTags.getSureView().setOnClickListener(v1 -> {
                    int type = Integer.parseInt(getConTags.getEtType().getText().toString());
                    String targetId = getConTags.getEtTargetId().getText().toString();
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier();
                    conversationIdentifier.setType(Conversation.ConversationType.setValue(type));
                    conversationIdentifier.setTargetId(targetId);
                    getConversationTags(conversationIdentifier);
                    getConTags.cancel();
                });

                getConTags.getCancelView().setOnClickListener(v12 -> getConTags.cancel());
                getConTags.show();
                break;
            case R.id.btn_get_conversation_top:
                TagTestInputDialog topInputDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_GET_CONVERSATION_TOP);
                topInputDialog.getSureView().setOnClickListener(v1 -> {
                    int type = Integer.parseInt(topInputDialog.getEtType().getText().toString());
                    String targetId = topInputDialog.getEtTargetId().getText().toString();
                    String tagId = topInputDialog.getEtTagId().getText().toString();
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier();
                    conversationIdentifier.setType(Conversation.ConversationType.setValue(type));
                    conversationIdentifier.setTargetId(targetId);
                    getConversationTags(conversationIdentifier);
                    getConversationTopStatusInTag(conversationIdentifier, tagId);
                    topInputDialog.cancel();
                });

                topInputDialog.getCancelView().setOnClickListener(v12 -> topInputDialog.cancel());
                topInputDialog.show();
                break;
            case R.id.btn_get_conversation_tag_by_page:
                TagTestInputDialog getConForTagDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_GET_CONVERSATION_FOR_TAG);
                getConForTagDialog.getSureView().setOnClickListener(v1 -> {
                    String tagId = getConForTagDialog.getEtTagId().getText().toString();
                    long ts = Long.parseLong(getConForTagDialog.getEtType().getText().toString());
                    int count = Integer.parseInt(getConForTagDialog.getEtTargetId().getText().toString());
                    getConversationsFromTagByPage(tagId, ts, count);
                    getConForTagDialog.cancel();
                });
                getConForTagDialog.getCancelView().setOnClickListener(v12 -> getConForTagDialog.cancel());
                getConForTagDialog.show();
                break;
            case R.id.btn_get_unread_count_for_tag:
                TagTestInputDialog getUnreadDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_GET_UNREAD_FOR_TAG);
                getUnreadDialog.getSureView().setOnClickListener(v1 -> {
                    String tagId = getUnreadDialog.getEtTagId().getText().toString();
                    boolean containBlocked = Boolean.parseBoolean(getUnreadDialog.getEtTargetId().getText().toString());
                    getUnreadCountByTag(tagId, containBlocked);
                    getUnreadDialog.cancel();
                });
                getUnreadDialog.getCancelView().setOnClickListener(v12 -> getUnreadDialog.cancel());
                getUnreadDialog.show();
                break;
            case R.id.btn_set_con_top:
                TagTestInputDialog setTopDialog = new TagTestInputDialog(mContext, TagTestInputDialog.TYPE_SET_TOP);
                setTopDialog.getSureView().setOnClickListener(v1 -> {
                    String tagId = setTopDialog.getEtTagId().getText().toString();
                    int type = Integer.parseInt(setTopDialog.getEtType().getText().toString());
                    String targetId = setTopDialog.getEtTargetId().getText().toString();
                    boolean isTop = Boolean.parseBoolean(setTopDialog.getEtTagName().getText().toString());
                    ConversationIdentifier conversationIdentifier = new ConversationIdentifier();
                    conversationIdentifier.setType(Conversation.ConversationType.setValue(type));
                    conversationIdentifier.setTargetId(targetId);
                    setConversationToTopInTag(tagId, conversationIdentifier, isTop);
                    setTopDialog.cancel();
                });
                setTopDialog.getCancelView().setOnClickListener(v12 -> setTopDialog.cancel());
                setTopDialog.show();
                break;
            default:
                break;

        }
    }

    private void setConversationToTopInTag(String tagId, ConversationIdentifier conversationIdentifier, boolean isTop) {
        RongCoreClient.getInstance().setConversationToTopInTag(tagId, conversationIdentifier, isTop, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " setConversationToTopInTag 成功");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " setConversationToTopInTag 失败， e : " + coreErrorCode.getValue());
            }
        });
    }

    private void getUnreadCountByTag(String tagId, boolean containBlocked) {
        RongCoreClient.getInstance().getUnreadCountByTag(tagId, containBlocked, new IRongCoreCallback.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                addToList(getStringDate() + " getUnreadCountByTag 成功， count = " + integer);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                addToList(getStringDate() + " getUnreadCountByTag 失败, e : " + e.getValue());
            }
        });
    }

    private void getConversationsFromTagByPage(String tagId, long ts, int count) {
        RongCoreClient.getInstance().getConversationsFromTagByPage(tagId, ts, count, new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations == null) {
                    addToList(getStringDate() + " getConversationsFromTagByPage 失败");
                    return;
                }
                for (Conversation conversation : conversations) {
                    addToList(getStringDate() + " getConversationsFromTagByPage 成功 : conversation-->" + conversation);
                }
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                addToList(getStringDate() + " getConversationsFromTagByPage 失败， e : " + e.getValue());
            }
        });
    }

    private void getConversationTopStatusInTag(ConversationIdentifier conversationIdentifier, String tagId) {
        RongCoreClient.getInstance().getConversationTopStatusInTag(conversationIdentifier, tagId, new IRongCoreCallback.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                addToList(getStringDate() + " getConversationTopStatusInTag 成功");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                addToList(getStringDate() + " getConversationTopStatusInTag 失败, " + e.getValue());
            }
        });
    }

    private void getConversationTags(ConversationIdentifier conversationIdentifier) {
        RongCoreClient.getInstance().getTagsFromConversation(conversationIdentifier, new IRongCoreCallback.ResultCallback<List<ConversationTagInfo>>() {
            @Override
            public void onSuccess(List<ConversationTagInfo> conversationTagInfos) {
                if (conversationTagInfos == null) {
                    addToList(getStringDate() + " getTagsFromConversation 失败");
                    return;
                }
                for (ConversationTagInfo conversationTagInfo : conversationTagInfos) {
                    addToList(getStringDate() + " getTagsFromConversation 成功 tagid :  " + conversationTagInfo.getTagInfo().getTagId()
                            + ", tag name :" + conversationTagInfo.getTagInfo().getTagName() + ", isTop : " + conversationTagInfo.isTop());
                }

            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                addToList(getStringDate() + " getTagsFromConversation 失败, code :  " + e.getValue());
            }
        });
    }


    private void removeTagsFromConversation(ConversationIdentifier conversationIdentifier, List<String> ids) {
        RongCoreClient.getInstance().removeTagsFromConversation(conversationIdentifier, ids, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " 删除指定会话中的某些标签 ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " 删除指定会话中的某些标签失败, code :  " + coreErrorCode.getValue());
            }
        });
    }

    private void removeConversationTag(String id, List<ConversationIdentifier> list) {
        RongCoreClient.getInstance().removeConversationsFromTag(id, list, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " 删除指定标签会话成功 ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " 删除指定标签会话失败, code :  " + coreErrorCode.getValue());
            }
        });
    }

    private void addTagToConversation(String id, List<ConversationIdentifier> list) {

        RongCoreClient.getInstance().addConversationsToTag(id, list, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " 添加会话标签成功 ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " 添加会话标签失败, code :  " + coreErrorCode.getValue());
            }
        });
    }

    private void getAllTags() {
        RongCoreClient.getInstance().getTags(new IRongCoreCallback.ResultCallback<List<TagInfo>>() {
            @Override
            public void onSuccess(List<TagInfo> tagInfos) {
                for (TagInfo tagInfo : tagInfos) {
                    addToList(getStringDate() + " 获取 tag 成功, id : " + tagInfo.getTagId() +
                            ", name: " + tagInfo.getTagName());
                }
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                addToList(getStringDate() + " 获取 tag 失败, code :  " + e.getValue());
            }
        });
    }

    private void updateTag(String id, String name) {
        addToList(getStringDate() + "updateTag id : " + id);
        addToList(getStringDate() + "updateTag name : " + name);
        TagInfo tagInfo = new TagInfo(id, name);
        RongCoreClient.getInstance().updateTag(tagInfo, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " 更新 tag 成功 ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " 更新 tag 失败, code :  " + coreErrorCode.getValue());
            }
        });

    }

    private void deleteTag(String tagId) {
        RongCoreClient.getInstance().removeTag(tagId, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " 删除 tag 成功 ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " 删除 tag 失败, code :  " + coreErrorCode.getValue());
            }
        });
    }

    private void addTag(String id, String name) {
        addToList(getStringDate() + "addTag id : " + id);
        addToList(getStringDate() + "addTag name : " + name);
        TagInfo tagInfo = new TagInfo(id, name);
        RongCoreClient.getInstance().addTag(tagInfo, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList(getStringDate() + " 添加 tag 成功 ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                addToList(getStringDate() + " 添加 tag 失败, code :  " + coreErrorCode.getValue());
            }
        });

    }

    private void getConversation() {
        RongCoreClient.getInstance().getConversationList(new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (Conversation c : conversations) {
                    addToList(getStringDate() + " 获取会话列表：会话类型 " + c.getConversationType() + "  targetId：" + c.getTargetId());
                }
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                addToList(getStringDate() + " 获取会话列表失败 ： CoreErrorCode" + e.code);
            }
        });
    }


    private void addToList(String str) {
        contentList.add(str);
        handler.post(() -> mAdapter.notifyDataSetChanged());
        handler.postDelayed(() -> {
            if (lvContent != null && mAdapter != null) {
                lvContent.setSelection(mAdapter.getCount() - 1);
                Log.e("addToList", "**" + mAdapter.getCount() + "**" + contentList.size());
            }
        }, 300);
    }

    public String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public String formatTime(long time) {
        Date timeDate = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(timeDate);
        return dateString;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return contentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_extra_status, null);
            }
            TextView tvCotent = convertView.findViewById(R.id.tv_content);
            tvCotent.setText(contentList.get(position));
            return convertView;
        }
    }
}
