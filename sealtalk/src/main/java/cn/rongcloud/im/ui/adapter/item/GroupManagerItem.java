package cn.rongcloud.im.ui.adapter.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class GroupManagerItem extends RelativeLayout {
    private LinearLayout firstCharll;
    private TextView firstCharTv;
    private UserInfoItemView memberUiv;
    private CheckBox selectCb;

    public GroupManagerItem(Context context) {
        super(context);
        initView();
    }

    public GroupManagerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public GroupManagerItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.item_group_manager, this);
        firstCharll= findViewById(R.id.ll_firstchar);
        firstCharTv = findViewById(R.id.tv_char);
        memberUiv = findViewById(R.id.uiv_member);
        selectCb = findViewById(R.id.cb_select);
    }

    public void setData(GroupMember data, boolean isFirst) {
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (isFirst) {
            firstCharll.setVisibility(View.VISIBLE);
            firstCharTv.setText(data.getNameSpelling());
        } else {
            firstCharll.setVisibility(View.GONE);
        }

        memberUiv.setName(data.getName());
        ImageLoaderUtils.displayUserPortraitImage(data.getPortraitUri(), memberUiv.getHeaderImageView());
    }

    /**
     * 选择选择问题
     * @param checked
     */
    public void setChecked(boolean checked) {
        selectCb.setChecked(checked);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        selectCb.setEnabled(enabled);
    }

    public void setCheckVisibility(int visibility) {
        selectCb.setVisibility(visibility);
    }


    //    /**
//     * 设置选择监听
//     * @param listener
//     */
//    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
//        selectCb.setOnCheckedChangeListener(listener);
//    }
}
