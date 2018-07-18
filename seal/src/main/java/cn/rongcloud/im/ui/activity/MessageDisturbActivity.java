package cn.rongcloud.im.ui.activity;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Date;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import cn.rongcloud.im.utils.DateUtils;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by AMing on 16/8/29.
 * Company RongCloud
 */
public class MessageDisturbActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = DisturbActivity.class.getSimpleName();

    private LinearLayout mIsShowSetting;
    /**
     * 开始时间的 TextView
     */
    private TextView mStartTimeNotification;
    /**
     * 关闭时间的 TextView
     */
    private TextView mEndTimeNotification;
    /**
     * 开始时间
     */
    private String mStartTime;
    /**
     * 结束时间
     */
    private String mEndTime;
    /**
     * 小时
     */
    int hourOfDays;
    /**
     * 分钟
     */
    int minutes;

    private String mTimeFormat = "HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disturb);
        setTitle(R.string.message_notice);
        initView();
    }


    private void initView() {
        SwitchButton mSwitchButton = (SwitchButton) findViewById(R.id.disturb_switch);
        mIsShowSetting = (LinearLayout) findViewById(R.id.is_show_notification);
        RelativeLayout mStartNotification = (RelativeLayout) findViewById(R.id.start_notification);
        RelativeLayout mEndStartNotification = (RelativeLayout) findViewById(R.id.end_notification);
        mStartTimeNotification = (TextView) findViewById(R.id.start_time_notification);
        mEndTimeNotification = (TextView) findViewById(R.id.end_time_notification);
        mStartNotification.setOnClickListener(this);
        mEndStartNotification.setOnClickListener(this);
        mSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedCheckBox();
                    // 开启消息免打扰
                    mIsShowSetting.setVisibility(View.VISIBLE);
                    String startTime = getSharedPreferences("config", MODE_PRIVATE).getString("startTime", "");
                    String endTime = getSharedPreferences("config", MODE_PRIVATE).getString("endTime", "");

                    if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
                        Date dataStart = DateUtils.stringToDate(startTime, mTimeFormat);
                        Date dataEnd = DateUtils.stringToDate(endTime, mTimeFormat);
                        long spansTime = DateUtils.compareMin(dataStart, dataEnd);
                        mStartTimeNotification.setText(startTime);
                        mEndTimeNotification.setText(endTime);
                        setConversationTime(startTime, (int) spansTime);
                    } else {
                        mStartTimeNotification.setText("23:00:00");
                        mEndTimeNotification.setText("07:00:00");
                        SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
                        editor.putString("startTime", "23:00:00");
                        editor.putString("endTime", "07:00:00");
                        editor.commit();
                        setConversationTime("23:00:00", 480);
                    }

                } else {
                    unSelectCheckBox();
                    //关闭消息免打扰
                    mIsShowSetting.setVisibility(View.GONE);
                    RongIM.getInstance().removeNotificationQuietHours(new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
//                            getWindow().getDecorView().post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    NToast.shortToast(mContext, "关闭成功");
//                                }
//                            });
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }
            }
        });

        mSwitchButton.setChecked(getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDisturb", false));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_notification:
                String startTime = getSharedPreferences("config", MODE_PRIVATE).getString("startTime", "");
                if (!TextUtils.isEmpty(startTime)) {
                    hourOfDays = Integer.parseInt(startTime.substring(0, 2));
                    minutes = Integer.parseInt(startTime.substring(3, 5));
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(MessageDisturbActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mStartTime = getDaysTime(hourOfDay, minute);
                        mStartTimeNotification.setText(mStartTime);
                        SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
                        editor.putString("startTime", mStartTime);
                        editor.commit();

                        String endTime = getSharedPreferences("config", MODE_PRIVATE).getString("endTime", "");
                        if (!TextUtils.isEmpty(endTime)) {
                            Date dataStart = DateUtils.stringToDate(mStartTime, mTimeFormat);
                            Date dataEnd = DateUtils.stringToDate(endTime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(dataStart, dataEnd);
                            setConversationTime(mStartTime, (int) Math.abs(spansTime));
                        }
                    }
                }, hourOfDays, minutes, true);
                timePickerDialog.show();
                break;
            case R.id.end_notification:
                String endTime = getSharedPreferences("config", MODE_PRIVATE).getString("endTime", "");
                if (!TextUtils.isEmpty(endTime)) {
                    hourOfDays = Integer.parseInt(endTime.substring(0, 2));
                    minutes = Integer.parseInt(endTime.substring(3, 5));
                }

                timePickerDialog = new TimePickerDialog(MessageDisturbActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mEndTime = getDaysTime(hourOfDay, minute);
                        mEndTimeNotification.setText(mEndTime);
                        SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
                        editor.putString("endTime", mEndTime);
                        editor.commit();

                        String startTime = getSharedPreferences("config", MODE_PRIVATE).getString("startTime", "");
                        if (!TextUtils.isEmpty(startTime)) {
                            Date dataStart = DateUtils.stringToDate(startTime, mTimeFormat);
                            Date dataEnd = DateUtils.stringToDate(mEndTime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(dataStart, dataEnd);
                            setConversationTime(startTime, (int) Math.abs(spansTime));
                        }
                    }
                }, hourOfDays, minutes, true);
                timePickerDialog.show();
                break;
        }
    }


    /**
     * 设置勿扰时间
     *
     * @param startTime 设置勿扰开始时间 格式为：HH:mm:ss
     * @param spanMins  0 < 间隔时间 < 1440
     */
    private void setConversationTime(final String startTime, final int spanMins) {

        if (!TextUtils.isEmpty(startTime)) {
            new android.os.Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (spanMins > 0 && spanMins < 1440) {
                        Log.e("", "----设置勿扰时间startTime；" + startTime + "---spanMins:" + spanMins);

                        RongIM.getInstance().setNotificationQuietHours(startTime, spanMins, new RongIMClient.OperationCallback() {

                            @Override
                            public void onSuccess() {
//                                getWindow().getDecorView().post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        NToast.shortToast(mContext, "设置消息免打扰成功,设置时长: " + spanMins + "分钟");
//                                    }
//                                });
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "----yb----设置会话通知周期-oonError:" + errorCode.getValue());
                            }
                        });
                    } else {
                        getWindow().getDecorView().post(new Runnable() {
                            @Override
                            public void run() {
                                NToast.shortToast(mContext, "间隔时间必须>0");
                            }
                        });
                    }
                }
            });
        }
    }


    /**
     * 得到"HH:mm:ss"类型时间
     *
     * @param hourOfDay 小时
     * @param minite    分钟
     * @return "HH:mm:ss"类型时间
     */
    private String getDaysTime(final int hourOfDay, final int minite) {
        String daysTime;
        String hourOfDayString = "0" + hourOfDay;
        String minuteString = "0" + minite;
        if (hourOfDay < 10 && minite >= 10) {
            daysTime = hourOfDayString + ":" + minite + ":00";
        } else if (minite < 10 && hourOfDay >= 10) {
            daysTime = hourOfDay + ":" + minuteString + ":00";
        } else if (hourOfDay < 10 && minite < 10) {
            daysTime = hourOfDayString + ":" + minuteString + ":00";
        } else {
            daysTime = hourOfDay + ":" + minite + ":00";
        }
        return daysTime;
    }

    private void selectedCheckBox() {
        mIsShowSetting.setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
        editor.putBoolean("isDisturb", true);
        editor.commit();
    }

    private void unSelectCheckBox() {
        mIsShowSetting.setVisibility(View.GONE);
        SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
        editor.putBoolean("isDisturb", false);
        editor.commit();
    }
}
