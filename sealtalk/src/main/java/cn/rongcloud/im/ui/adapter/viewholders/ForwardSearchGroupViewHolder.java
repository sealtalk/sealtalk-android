package cn.rongcloud.im.ui.adapter.viewholders;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.SearchGroupModel;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class ForwardSearchGroupViewHolder extends ForwardCheckViewHolder<SearchGroupModel> {

    private TextView tvNickName;
    private TextView tvGroupMembers;
    private ImageView portrait;
    private View llDescription;
    private OnGroupItemClickListener groupItemClickListener;
    private CheckBox checkBox;
    private SearchGroupModel searchGroupModel;

    public ForwardSearchGroupViewHolder(@NonNull View itemView, OnGroupItemClickListener l) {
        super(itemView);
        this.groupItemClickListener = l;
        portrait = itemView.findViewById(R.id.iv_portrait);
        tvNickName = itemView.findViewById(R.id.tv_name);
        tvGroupMembers = itemView.findViewById(R.id.tv_detail);
        llDescription = itemView.findViewById(R.id.ll_description);
        checkBox = itemView.findViewById(R.id.cb_select);
        checkBox.setVisibility(View.VISIBLE);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupItemClickListener != null) {
                    if (searchGroupModel.getCheckType() != CheckType.NONE && searchGroupModel.getCheckType() != CheckType.DISABLE) {
                        if (searchGroupModel.getCheckType() == CheckType.CHECKED) {
                            searchGroupModel.setCheckType(CheckType.UNCHECKED);
                            checkBox.setChecked(false);
                        } else  if (searchGroupModel.getCheckType() == CheckType.UNCHECKED) {
                            searchGroupModel.setCheckType(CheckType.CHECKED);
                            checkBox.setChecked(true);
                        }
                    }
                    groupItemClickListener.onGroupClicked(searchGroupModel.getBean());
                }
            }
        });
    }

    @Override
    public void update(SearchGroupModel searchGroupModel) {
        this.searchGroupModel = searchGroupModel;
        // 更接数据类型进行显示
        if (searchGroupModel.getCheckType() == CheckType.NONE) {
            checkBox.setVisibility(View.GONE);
        } else  if (searchGroupModel.getCheckType() == CheckType.DISABLE) {
            checkBox.setEnabled(false);
        } else {
            checkBox.setVisibility(View.VISIBLE);
            if (searchGroupModel.getCheckType() == CheckType.CHECKED) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        }

        GroupEntity groupEntity = searchGroupModel.getBean();
        if (searchGroupModel.getGroupNameStart() == -1) {
            tvNickName.setText(groupEntity.getName());
        } else {
            tvNickName.setText(CharacterParser.getSpannable(groupEntity.getName(), searchGroupModel.getGroupNameStart(), searchGroupModel.getGroupNameEnd()));
        }
        ImageLoaderUtils.displayUserPortraitImage(groupEntity.getPortraitUri(), portrait);
        List<SearchGroupModel.GroupMemberMatch> memberMatches = searchGroupModel.getMatchedMemberlist();
        if (memberMatches == null || memberMatches.size() == 0) {
            llDescription.setVisibility(View.GONE);
        } else if (memberMatches.size() > 0) {
            llDescription.setVisibility(View.VISIBLE);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            for (int i = 0; i < memberMatches.size(); i++) {
                SearchGroupModel.GroupMemberMatch memberMatch = memberMatches.get(i);
                if (memberMatch.getNameStart() != -1) {
                    SpannableStringBuilder spannable = CharacterParser.getSpannable(memberMatch.getName(), memberMatch.getNameStart(), memberMatch.getNameEnd());
                    builder.append(spannable);
                    if (i != memberMatches.size() - 1) {
                        builder.append("、");
                    }
                }
            }
            tvGroupMembers.setText(builder);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked) {
            searchGroupModel.setCheckType(CheckType.CHECKED);
            checkBox.setChecked(true);
        } else {
            searchGroupModel.setCheckType(CheckType.UNCHECKED);
            checkBox.setChecked(false);
        }
    }
}
