package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.SearchTitleModel;

public class SearchTitleViewHolder extends BaseViewHolder<SearchTitleModel> {
    private TextView textView;

    public SearchTitleViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.tv_title);
    }

    @Override
    public void update(SearchTitleModel searchTitleModel) {
        textView.setText(itemView.getContext().getString(searchTitleModel.getBean()));
    }
}
