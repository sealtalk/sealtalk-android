package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.adapter.MemberMentionedAdapter;
import cn.rongcloud.im.ui.widget.SideBar;
import cn.rongcloud.im.viewmodel.MemberMentionedViewModel;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;


public class MemberMentionedExActivity extends TitleBaseActivity {

    private MemberMentionedAdapter adapter;
    private MemberMentionedViewModel memberMentionedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_mention_members);

        // 舒适布局
        initView();
        initViewModel();
    }

    // 初始化布局
    private void initView() {
        getTitleBar().setTitle(R.string.rc_choose_members);
        EditText searchEt = findViewById(R.id.et_menber_search);
        ListView menberListlv = findViewById(R.id.lv_list);
        TextView sideTexttv = findViewById(R.id.tv_popup_bg);
        SideBar sideBarSb = (SideBar) findViewById(R.id.sb_sidebar);
        sideBarSb.setTextView(sideTexttv);

        adapter = new MemberMentionedAdapter();
        menberListlv.setAdapter(adapter);


        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterMember(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        menberListlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = adapter.getItem(position);
                if (item != null && item instanceof GroupMember) {
                    GroupMember member = (GroupMember) item;
                    //TODO
                    setMentionMember(member);
                }
                finish();
            }
        });

        //设置右侧触摸监听
        sideBarSb.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    menberListlv.setSelection(position);
                }
            }
        });
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {

        String targetId = getIntent().getStringExtra(RouteUtils.TARGET_ID);
        Conversation.ConversationType conversationType = Conversation.ConversationType.setValue(getIntent().getIntExtra(RouteUtils.CONVERSATION_TYPE, 0));
        memberMentionedViewModel = ViewModelProviders.of(this,
                new MemberMentionedViewModel.Factory(targetId, conversationType, getApplication())).get(MemberMentionedViewModel.class);

        // 成员列表
        memberMentionedViewModel.getMemberListResult().observe(this, new Observer<List<GroupMember>>() {
            @Override
            public void onChanged(List<GroupMember> groupMembers) {
                adapter.updateList(groupMembers);
            }
        });

        // 过滤成员列表
        memberMentionedViewModel.getFilterMenberList().observe(this, new Observer<List<GroupMember>>() {
            @Override
            public void onChanged(List<GroupMember> groupMembers) {
                adapter.updateList(groupMembers);
            }
        });

    }

    /**
     * 过滤朋友列表
     * @param filterStr
     */
    private void filterMember(String filterStr) {
        if (memberMentionedViewModel != null) {
            memberMentionedViewModel.filterMember(filterStr);
        }
    }

    /**
     * 设置 @ 的人员
     * @param mentionMember
     */
    private void setMentionMember(GroupMember mentionMember) {
        if (memberMentionedViewModel != null) {
            memberMentionedViewModel.setMentionMember(mentionMember);
        }
    }

}
