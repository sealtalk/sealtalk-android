package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.GroupNoticeListAdapter;
import cn.rongcloud.im.ui.dialog.ClearGroupNoticeDialog;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.GroupNoticeInfoViewModel;
import io.rong.imkit.RongIM;

public class GroupNoticeListActivity extends TitleBaseActivity {

    private GroupNoticeInfoViewModel groupNoticeInfoViewModel;
    private ListView groupNoticeList;
    private TextView isNull;
    private GroupNoticeListAdapter adapter;
    private List<String> showIdList = new ArrayList<>();
    private Map<String, String> portraitUrlMap = new HashMap<>();
    private boolean isCanClickIngore = true;
    private boolean isCanClickAgree = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_notice);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_conversation_notification_group);
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getTitleBar().getBtnRight().setImageDrawable(getResources().getDrawable(R.drawable.notice_clear));
        getTitleBar().getTvRight().setVisibility(View.GONE);
        getTitleBar().setOnBtnRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDialog();
            }
        });
        groupNoticeList = findViewById(R.id.lv_group_notice_list);
        isNull = findViewById(R.id.tv_is_null);
        adapter = new GroupNoticeListAdapter();
        adapter.setOnItemButtonClick(new GroupNoticeListAdapter.OnItemButtonClick() {
            @Override
            public boolean onButtonAgreeClick(View view, int position, GroupNoticeInfo info) {
                if (info != null) {
                    if (info.getGroupId() == null) {
                        ToastUtils.showToast(getString(R.string.seal_group_notice_no_group));
                        return false;
                    }
                    if (info.getStatus() == 2) {
                        //防止重复的多次点击请求服务器
                        if (!isCanClickAgree) {
                            return false;
                        }
                        isCanClickAgree = false;
                        //状态码1表示同意
                        LiveData<Resource<Void>> setStatusResult = groupNoticeInfoViewModel.setGroupNoticeStatus(info.getGroupId(), info.getReceiverId(),
                                "1", info.getId());
                        setStatusResult.observe(GroupNoticeListActivity.this, new Observer<Resource<Void>>() {
                            @Override
                            public void onChanged(Resource<Void> voidResource) {
                                if (voidResource.status == Status.SUCCESS) {
                                    setStatusResult.removeObserver(this);
                                    isCanClickAgree = true;
                                    ToastUtils.showToast(getString(R.string.seal_group_notice_success));
                                    LiveData<GroupEntity> groupEntityLiveData = groupNoticeInfoViewModel.showCertifiTipsDialog(info.getGroupId());
                                    groupEntityLiveData.observe(GroupNoticeListActivity.this, new Observer<GroupEntity>() {
                                        @Override
                                        public void onChanged(GroupEntity groupEntity) {
                                            if (groupEntity != null) {
                                                groupEntityLiveData.removeObserver(this);
                                                if (groupEntity.getCertiStatus() == 0
                                                        && info.getReceiverId().equals(RongIM.getInstance().getCurrentUserId())
                                                        && !info.getRequesterId().equals(groupEntity.getCreatorId())) {
                                                    showCertifiTipsDialog(info.getId());
                                                }
                                            }
                                        }
                                    });
                                } else if (voidResource.status == Status.ERROR) {
                                    setStatusResult.removeObserver(this);
                                    isCanClickAgree = true;
                                    ToastUtils.showToast(getString(R.string.common_network_unavailable));
                                }
                            }
                        });
                    }
                }
                return false;
            }

            @Override
            public boolean onButtonIgnoreClick(View view, int position, GroupNoticeInfo info) {
                if (info != null) {
                    if (info.getGroupId() == null) {
                        ToastUtils.showToast(getString(R.string.seal_group_notice_no_group));
                        return false;
                    }
                    if (info.getStatus() == 2) {
                        if (!isCanClickIngore) {
                            return false;
                        }
                        isCanClickIngore = false;
                        //状态码0表示忽略
                        LiveData<Resource<Void>> setStatusResult = groupNoticeInfoViewModel.setGroupNoticeStatus(info.getGroupId(), info.getReceiverId(),
                                "0", info.getId());
                        setStatusResult.observe(GroupNoticeListActivity.this, new Observer<Resource<Void>>() {
                            @Override
                            public void onChanged(Resource<Void> voidResource) {
                                if (voidResource.status == Status.SUCCESS) {
                                    setStatusResult.removeObserver(this);
                                    ToastUtils.showToast(getString(R.string.seal_group_notice_success));
                                    isCanClickIngore = true;
                                } else if (voidResource.status == Status.ERROR) {
                                    setStatusResult.removeObserver(this);
                                    isCanClickIngore = true;
                                    ToastUtils.showToast(getString(R.string.common_network_unavailable));
                                }
                            }
                        });
                    }
                }
                return false;
            }
        });
        adapter.setOnRequestInfoListener(new GroupNoticeListAdapter.OnRequestInfoListener() {
            @Override
            public boolean onRequestGroupInfo(View view, int position, GroupNoticeInfo info) {
                if (view instanceof SelectableRoundedImageView) {
                    LiveData<Resource<GroupEntity>> groupResult = groupNoticeInfoViewModel.getGroupInfo(info.getGroupId());
                    groupResult.observe(GroupNoticeListActivity.this, new Observer<Resource<GroupEntity>>() {
                        @Override
                        public void onChanged(Resource<GroupEntity> groupEntityResource) {
                            if (groupEntityResource.status != Status.LOADING && groupEntityResource.data != null) {
                                groupResult.removeObserver(this);
                                //防止重复请求同一地址的头像图片
                                if (portraitUrlMap.get(info.getGroupId()) == null || !portraitUrlMap.get(info.getGroupId()).equals(groupEntityResource.data.getPortraitUri())) {
                                    portraitUrlMap.put(info.getGroupId(), groupEntityResource.data.getPortraitUri());
                                    ImageLoaderUtils.displayUserPortraitImage(groupEntityResource.data.getPortraitUri(), (SelectableRoundedImageView) view);
                                }
                            }
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onRequestUserInfo(View view, int position, GroupNoticeInfo info) {
                if (view instanceof SelectableRoundedImageView) {
                    LiveData<Resource<UserInfo>> userResult = groupNoticeInfoViewModel.getUserInfo(info.getReceiverId());
                    userResult.observe(GroupNoticeListActivity.this, new Observer<Resource<UserInfo>>() {
                        @Override
                        public void onChanged(Resource<UserInfo> userInfoResource) {
                            if (userInfoResource.status != Status.LOADING && userInfoResource.data != null) {
                                userResult.removeObserver(this);
                                //防止重复请求同一地址的头像图片
                                if (portraitUrlMap.get(info.getReceiverId()) == null || !portraitUrlMap.get(info.getReceiverId()).equals(userInfoResource.data.getPortraitUri())) {
                                    portraitUrlMap.put(info.getReceiverId(), userInfoResource.data.getPortraitUri());
                                    ImageLoaderUtils.displayUserPortraitImage(userInfoResource.data.getPortraitUri(), (SelectableRoundedImageView) view);
                                }

                            }
                        }
                    });

                }
                return false;
            }
        });
        groupNoticeList.setAdapter(adapter);
    }

    private void initViewModel() {
        groupNoticeInfoViewModel = ViewModelProviders.of(this).get(GroupNoticeInfoViewModel.class);
        groupNoticeInfoViewModel.getGroupNoticeInfo().observe(this, new Observer<Resource<List<GroupNoticeInfo>>>() {
            @Override
            public void onChanged(Resource<List<GroupNoticeInfo>> listResource) {
                if (listResource.status != Status.LOADING) {
                    if (listResource.data != null) {
                        Log.e("getGroupNoticeInfo", listResource.data.toString() + "");
                        if (listResource.data.size() > 0) {
                            isNull.setVisibility(View.GONE);
                        } else {
                            isNull.setVisibility(View.VISIBLE);
                        }
                        portraitUrlMap.clear();
                        adapter.updateList(listResource.data);
                    }
                }
            }
        });
        groupNoticeInfoViewModel.getClearNoticeResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.seal_group_notice_clean_success);
                }
            }
        });
    }

    private synchronized void showCertifiTipsDialog(String showId) {
        //确保一条邀请信息只展示一次认证 dialog
        if (showIdList.contains(showId)) {
            return;
        }
        showIdList.add(showId);
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_add_certification_need_certifi));
        builder.setIsOnlyConfirm(true);
        CommonDialog certifiTipsDialog = builder.build();
        certifiTipsDialog.show(getSupportFragmentManager().beginTransaction(), "AddCategoriesDialogFragment");
    }

    private void showClearDialog() {
        ClearGroupNoticeDialog dialog = new ClearGroupNoticeDialog();
        dialog.setmOnClearClick(new ClearGroupNoticeDialog.ClearClickListener() {
            @Override
            public void onClearClick() {
                groupNoticeInfoViewModel.clearNotice();
            }
        });
        dialog.show(getSupportFragmentManager(), "ClearNotice");
    }
}
