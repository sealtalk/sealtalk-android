package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.fragment.SearchMessageFragment;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import cn.rongcloud.im.viewmodel.SearchMessageModel;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;

public class SearchHistoryMessageActivity extends SealSearchBaseActivity
        implements OnMessageRecordClickListener {

    private SearchMessageFragment messageFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getIntent().getStringExtra(IntentExtra.STR_CHAT_NAME);
        String portrait = getIntent().getStringExtra(IntentExtra.STR_CHAT_PORTRAIT);
        ConversationIdentifier conversationIdentifier = initConversationIdentifier();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        messageFragment = new SearchMessageFragment();
        messageFragment.init(this, conversationIdentifier, name, portrait);
        transaction.replace(R.id.fl_content_fragment, messageFragment);
        transaction.commit();
    }

    @Override
    public void onMessageRecordClick(SearchMessageModel searchMessageModel) {
        Message message = searchMessageModel.getBean();
        Bundle bundle = new Bundle();
        bundle.putString(RouteUtils.TITLE, searchMessageModel.getName());
        bundle.putLong(RouteUtils.INDEX_MESSAGE_TIME, message.getSentTime());
        RouteUtils.routeToConversationActivity(
                this, ConversationIdentifier.obtain(message), bundle);
    }

    @Override
    public void search(String search) {
        messageFragment.search(search);
    }
}
