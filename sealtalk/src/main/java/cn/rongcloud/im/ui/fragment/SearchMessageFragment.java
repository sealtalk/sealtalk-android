package cn.rongcloud.im.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.List;

public class SearchMessageFragment extends SearchBaseFragment {
    private ConversationIdentifier identifier;
    private String name;
    private String portraitUrl;

    public void init(
            OnMessageRecordClickListener onMessageRecordClickListener,
            ConversationIdentifier identifier,
            String name,
            String portraitUrl) {
        init(null, null, null, null, onMessageRecordClickListener);
        this.identifier = identifier != null ? identifier : new ConversationIdentifier();
        this.name = name;
        this.portraitUrl = portraitUrl;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        viewModel
                .getMessageSearch()
                .observe(
                        getViewLifecycleOwner(),
                        new Observer<List<SearchModel>>() {
                            @Override
                            public void onChanged(List<SearchModel> searchModels) {
                                updateData(searchModels);
                            }
                        });
        if (!TextUtils.isEmpty(initSearch)) {
            search(initSearch);
        }
        return view;
    }

    @Override
    public void search(String search) {
        super.search(search);
        if (viewModel != null
                && !TextUtils.isEmpty(identifier.getTargetId())
                && identifier.getType() != Conversation.ConversationType.NONE) {
            viewModel.searchMessage(identifier, name, portraitUrl, search);
        }
    }
}
