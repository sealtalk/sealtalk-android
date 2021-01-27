package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.fragment.SelectMultiFriendFragment;
import cn.rongcloud.im.ui.interfaces.OnSelectCountChangeListener;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.boundview.BoundedHorizontalScrollView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.viewmodel.SelectMultiViewModel;

/**
 * 不要直接请求此 Activity
 */
public class SelectMultiFriendsActivity extends SelectBaseActivity implements View.OnClickListener, OnSelectCountChangeListener {
    private SelectMultiFriendFragment selectMultiFriendFragment;
    private SelectMultiViewModel selectMultiViewModel;
    private TextView titleConfirmTv;
    private LinearLayout llSelectContent;
    private BoundedHorizontalScrollView scrollView;
    private Map<String, View> selectViewMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llSelectContent = findViewById(R.id.ll_select_content);
        scrollView = findViewById(R.id.sl_scroll_view);
        SealTitleBar sealTitleBar = getTitleBar();
        titleConfirmTv = sealTitleBar.getTvRight();
        titleConfirmTv.setText(R.string.seal_select_confirm);
        titleConfirmTv.setOnClickListener(this);
        selectMultiFriendFragment = getSelectMultiFriendFragment();
        selectMultiFriendFragment.setOnSelectCountChangeListener(this);
        sealTitleBar.setTitle(getString(R.string.seal_select_group_member));
        sealTitleBar.getBtnRight().setVisibility(View.GONE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_fragment_container, selectMultiFriendFragment);
        transaction.commit();

        initViewModel();
    }

    private void initViewModel() {
        selectMultiViewModel = ViewModelProviders.of(this).get(SelectMultiViewModel.class);

        selectMultiViewModel.getSelectedCount().observe(this, selectCount -> {
            if (selectCount > 0) {
                setConfirmEnable(true);
            } else if (confirmEnabledWhenNoChecked()) {
                setConfirmEnable(true);
            } else {
                setConfirmEnable(false);
            }
        });

        selectMultiViewModel.getCheckedChangeData().observe(this, new Observer<CheckableContactModel>() {
            @Override
            public void onChanged(CheckableContactModel checkableContactModel) {
                updateSelectContent(checkableContactModel);
            }
        });
    }

    private void updateSelectContent(CheckableContactModel checkableContactModel) {
        Object bean = checkableContactModel.getBean();
        if (bean instanceof FriendShipInfo) {
            FriendShipInfo friendShipInfo = (FriendShipInfo) bean;
            String portraitUri = friendShipInfo.getUser().getPortraitUri();
            if (checkableContactModel.getCheckType() == CheckType.CHECKED) {
                View view = LayoutInflater.from(this).inflate(R.layout.item_select_content, null, false);
                SelectableRoundedImageView ivPortrait = view.findViewById(R.id.iv_portrait);
                if (!TextUtils.isEmpty(portraitUri)) {
                    ImageLoaderUtils.displayUserPortraitImage(portraitUri, ivPortrait);
                }
                selectViewMap.put(friendShipInfo.getUser().getId(), view);
                llSelectContent.addView(view);
                llSelectContent.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
                    }
                });
            } else if (checkableContactModel.getCheckType() == CheckType.NONE) {
                View view = selectViewMap.get(friendShipInfo.getUser().getId());
                if (view != null) {
                    llSelectContent.removeView(view);
                }
            }

        } else if (bean instanceof GroupMember) {
            GroupMember groupMember = (GroupMember) bean;
            String portraitUri = groupMember.getPortraitUri();
            if (checkableContactModel.getCheckType() == CheckType.CHECKED) {
                View view = LayoutInflater.from(this).inflate(R.layout.item_select_content, null, false);
                SelectableRoundedImageView ivPortrait = view.findViewById(R.id.iv_portrait);
                if (!TextUtils.isEmpty(portraitUri)) {
                    ImageLoaderUtils.displayUserPortraitImage(portraitUri, ivPortrait);
                }
                view.setTag(groupMember.getUserId());
                selectViewMap.put(groupMember.getUserId(), view);
                llSelectContent.addView(view);
                llSelectContent.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
                    }
                });
            } else if (checkableContactModel.getCheckType() == CheckType.NONE) {
                View view = selectViewMap.get(groupMember.getUserId());
                if (view != null) {
                    llSelectContent.removeView(view);
                }
            }
        }
    }

    /**
     * 设置可以点击确定
     *
     * @param isEnable
     */
    private void setConfirmEnable(boolean isEnable) {
        if (isEnable) {
            titleConfirmTv.setClickable(true);
            titleConfirmTv.setTextColor(getResources().getColor(R.color.color_blue_9F));
        } else {
            titleConfirmTv.setClickable(false);
            titleConfirmTv.setTextColor(getResources().getColor(R.color.seal_group_detail_clean_tips));
        }
    }


    protected SelectMultiFriendFragment getSelectMultiFriendFragment() {
        return new SelectMultiFriendFragment();
    }

    /**
     * @param v 右上角点击
     */
    @Override
    public void onClick(View v) {
        onConfirmClicked(selectMultiFriendFragment.getCheckedList(), selectMultiFriendFragment.getCheckedInitGroupList());
    }

    /**
     * 右下角点击事件
     */
    @Override
    protected void onConfirmClick() {
        onConfirmClicked(selectMultiFriendFragment.getCheckedList(), selectMultiFriendFragment.getCheckedInitGroupList());
    }


    @Override
    public void onSelectCountChange(int groupCount, int userCount) {
    }

    public ArrayList<String> getCheckedFriendIds() {
        return selectMultiFriendFragment.getCheckedFriendList();
    }

    public ArrayList<String> getCheckedGroupIds() {
        return selectMultiFriendFragment.getCheckedGroupList();
    }

    /**
     * 是否在没有选择时可以点击确定,默认为未选择时不可点击
     * 重写此方法以开启状态在未选择时可点击确认
     *
     * @return
     */
    public boolean confirmEnabledWhenNoChecked() {
        return false;
    }

    @Override
    protected boolean isSearchable() {
        return true;
    }

    @Override
    public void onSearch(String keyword) {
        if (selectMultiFriendFragment != null) {
            if (TextUtils.isEmpty(keyword)) {
                selectMultiFriendFragment.loadAll();
            } else {
                selectMultiFriendFragment.search(keyword);
            }
        }
    }
}
