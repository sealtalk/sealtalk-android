package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.SearchConversationModel;
import cn.rongcloud.im.ui.fragment.SearchBaseFragment;
import cn.rongcloud.im.ui.fragment.SearchMessageFragment;
import cn.rongcloud.im.ui.fragment.SearchUltraGroupFragment;
import cn.rongcloud.im.ui.interfaces.OnChatItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import cn.rongcloud.im.viewmodel.SearchMessageModel;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.SearchConversationResult;

public class SealSearchUltraGroupActivity extends SealSearchBaseActivity
        implements OnChatItemClickListener, OnMessageRecordClickListener {

    public static final String TYPE = "type";
    public static final String TYPE_CONVERSATION_IDENTIFIER = "identifier";
    public static final int TYPE_ALL_TARGET = 1;
    public static final int TYPE_TARGET = 2;

    private SearchBaseFragment currentFragment; // 当前Fragment

    public static void start(Activity activity, int type, ConversationIdentifier identifier) {
        Intent intent = new Intent(activity, SealSearchUltraGroupActivity.class);
        intent.putExtra(SealSearchUltraGroupActivity.TYPE, type);
        if (identifier != null) {
            intent.putExtra(SealSearchUltraGroupActivity.TYPE_CONVERSATION_IDENTIFIER, identifier);
        }
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra(TYPE, 0);
        if (type == TYPE_ALL_TARGET) {
            SearchUltraGroupFragment ultraGroupFragment = new SearchUltraGroupFragment();
            ultraGroupFragment.init(this);
            currentFragment = ultraGroupFragment;
        } else {
            ConversationIdentifier identifier =
                    getIntent().getParcelableExtra(TYPE_CONVERSATION_IDENTIFIER);
            SearchMessageFragment searchMessageFragment = new SearchMessageFragment();
            searchMessageFragment.init(this, identifier, "", "");
            currentFragment = searchMessageFragment;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content_fragment, currentFragment);
        transaction.commit();
    }

    @Override
    public void search(String match) {
        currentFragment.search(match);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 1) {
            // 只有searchAllFragment
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void OnChatItemClicked(SearchConversationModel searchConversationModel) {
        SearchConversationResult result = searchConversationModel.getBean();
        RongIM.getInstance()
                .startConversation(
                        this,
                        ConversationIdentifier.obtain(result.getConversation()),
                        searchConversationModel.getName(),
                        result.getConversation().getSentTime());
    }

    @Override
    public void onMessageRecordClick(SearchMessageModel searchMessageModel) {
        Message message = searchMessageModel.getBean();
        RongIM.getInstance()
                .startConversation(
                        this,
                        ConversationIdentifier.obtain(message),
                        searchMessageModel.getName(),
                        message.getSentTime());
    }
}
