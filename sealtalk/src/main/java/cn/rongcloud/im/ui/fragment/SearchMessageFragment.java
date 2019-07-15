package cn.rongcloud.im.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import java.util.List;

import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import io.rong.imlib.model.Conversation;

public class SearchMessageFragment extends SearchBaseFragment {
    private String targetId;
    private Conversation.ConversationType conversationType;
    private String name;
    private String portraitUrl;

    public void init(OnMessageRecordClickListener onMessageRecordClickListener,
                     String targetId,
                     Conversation.ConversationType conversationType,
                     String name,
                     String portraitUrl) {
        init(null,
                null,
                null,
                null,
                onMessageRecordClickListener);

        this.targetId = targetId;
        this.conversationType = conversationType;
        this.name = name;
        this.portraitUrl = portraitUrl;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        viewModel.getMessageSearch().observe(this, new Observer<List<SearchModel>>() {
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
        if (viewModel != null && !TextUtils.isEmpty(targetId) && conversationType != null) {
            viewModel.searchMessage(targetId, conversationType, name, portraitUrl, search);
        }

    }
}
