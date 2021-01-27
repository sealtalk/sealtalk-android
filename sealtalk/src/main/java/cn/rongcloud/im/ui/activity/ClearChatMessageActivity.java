package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.SelectConversationAdapter;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.interfaces.OnCheckConversationClickListener;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.SelectConversationViewModel;
import io.rong.imlib.model.Conversation;

public class ClearChatMessageActivity extends TitleBaseActivity implements OnCheckConversationClickListener, View.OnClickListener {

    private RecyclerView rvContent;
    private SelectConversationAdapter adapter;
    private SelectConversationViewModel selectConversationViewModel;
    private TextView tvRemove;
    private LinearLayout llSelectAll;
    private CheckBox ckSelectAll;
    private int currentChatMessageCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_chat_message);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(getString(R.string.seal_clear_chat_message_select_title));
        rvContent = findViewById(R.id.rv_chat_message);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectConversationAdapter(this);
        rvContent.setAdapter(adapter);
        tvRemove = findViewById(R.id.tv_remove);
        tvRemove.setOnClickListener(this);
        llSelectAll = findViewById(R.id.ll_select_all);
        llSelectAll.setOnClickListener(this);
        ckSelectAll = findViewById(R.id.cb_select_all);
    }

    private void initViewModel() {
        selectConversationViewModel = ViewModelProviders.of(this).get(SelectConversationViewModel.class);
        selectConversationViewModel.loadConversation();
        selectConversationViewModel.getConersationLiveData().observe(this, new Observer<List<CheckableContactModel>>() {
            @Override
            public void onChanged(List<CheckableContactModel> checkableContactModels) {
                SLog.i("ClearChatMessage", "checkableContactModels,change**" +
                        checkableContactModels.size() + "***" + currentChatMessageCount);
                adapter.setData(checkableContactModels);
                //记录消息条目数量，判断是否被删除了
                if (currentChatMessageCount > checkableContactModels.size()) {
                    ToastUtils.showToast(R.string.seal_clear_chat_message_delete_success);
                }
                currentChatMessageCount = checkableContactModels.size();
            }
        });
        selectConversationViewModel.getSelectedCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                SLog.i("ClearChatMessage", integer.toString());
                updateTvRemoveStatus(integer);
            }
        });
    }

    private void updateTvRemoveStatus(int integer) {
        if (integer > 0) {
            tvRemove.setClickable(true);
            tvRemove.setTextColor(getResources().getColor(R.color.read_ff));
        } else {
            tvRemove.setClickable(false);
            tvRemove.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        //判断全选状态
        if (integer > 0 && integer == currentChatMessageCount) {
            ckSelectAll.setChecked(true);
        } else {
            ckSelectAll.setChecked(false);
        }
    }

    @Override
    public void onCheckConversationClick(CheckableContactModel<Conversation> conversation) {
        selectConversationViewModel.onItemClicked(conversation);
        adapter.notifyDataSetChanged();
    }

    /**
     * 清理消息
     */
    private void clearMessage() {
        selectConversationViewModel.clearMessage();
    }

    private void showDeleteConfirmDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_clear_chat_message_delete_dialog));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                clearMessage();
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog deleteDialog = builder.build();
        deleteDialog.show(getSupportFragmentManager().beginTransaction(), "AddCategoriesDialogFragment");
    }

    private void selectAll() {
        if (!ckSelectAll.isChecked()) {
            selectConversationViewModel.selectAllCheck();
            ckSelectAll.setChecked(true);
        } else {
            selectConversationViewModel.cancelAllCheck();
            ckSelectAll.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_remove:
                showDeleteConfirmDialog();
                break;
            case R.id.ll_select_all:
                selectAll();
                break;
        }
    }
}
