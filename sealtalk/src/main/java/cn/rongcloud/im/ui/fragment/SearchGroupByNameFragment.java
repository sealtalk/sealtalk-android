package cn.rongcloud.im.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import java.util.List;

import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;

public class SearchGroupByNameFragment extends SearchBaseFragment {
    private String lastSearchWord = "";
    private SearchResultListener resultListener;

    public void init(OnGroupItemClickListener onGroupItemClickListener) {
        init(null, onGroupItemClickListener, null, null, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        viewModel.getGroupContactSearhByName().observe(this, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchModels) {
                updateData(searchModels);

                if (resultListener != null) {
                    resultListener.onSearchResult(lastSearchWord, searchModels);
                }
            }
        });

        viewModel.getGroupContactList().observe(this, resource -> {
            if (resource.status != Status.LOADING) {
                search(lastSearchWord);
            }
        });
        viewModel.requestGroupContactList();
        return view;
    }

    @Override
    public void search(String search) {
        super.search(search);
        if (viewModel != null) {
            viewModel.searchGroupByName(search);
            lastSearchWord = search;
        }
    }

    public void setOnSearchResultListener(SearchResultListener listener) {
        resultListener = listener;
    }

    public interface SearchResultListener {
        void onSearchResult(String lastKeyWord, List<SearchModel> searchModels);
    }
}
