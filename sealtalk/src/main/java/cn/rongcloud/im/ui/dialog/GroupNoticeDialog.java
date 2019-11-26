package cn.rongcloud.im.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.rongcloud.im.R;

/**
 * 群公告弹窗
 */
public class GroupNoticeDialog extends DialogFragment {

    private String noticeContent;
    private long updateTime;

    @Override
    public void onStart() {
        super.onStart();
        //透明化背景
        Window window = getDialog().getWindow();
        //背景色
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.profile_dialog_group_notice, container, false);
        TextView noticeContentTv = contentView.findViewById(R.id.profile_tv_dialog_group_notice_content);
        TextView noticeTimeTv = contentView.findViewById(R.id.profile_tv_dialog_group_notice_time);
        Button confirmBtn = contentView.findViewById(R.id.dialog_btn_positive);

        // 群公告内容
        if(TextUtils.isEmpty(noticeContent)){
            noticeContent = getString(R.string.profile_group_has_no_notice);
            noticeTimeTv.setVisibility(View.INVISIBLE);
        }
        noticeContentTv.setText(noticeContent);

        // 群公告更新时间
        String updateTimeFormattedStr = "0000-00-00 00:00:00";
        if(updateTime != 0){
            Date date = new Date(updateTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            updateTimeFormattedStr = simpleDateFormat.format(date);
        }
        String updateTimeMsg = getString(R.string.profile_group_notice_update_time_format, updateTimeFormattedStr);
        noticeTimeTv.setText(updateTimeMsg);

        // 确定按钮监听
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return contentView;
    }

    /**
     * 设置群公告内容
     * @param noticeContent
     */
    public void setNoticeContent(String noticeContent){
        this.noticeContent = noticeContent;
    }

    /**
     * 设置群公告更新时间
     *
     * @param updateTime
     */
    public void setNoticeUpdateTime(long updateTime){
        this.updateTime = updateTime;
    }
}
