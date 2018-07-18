package cn.rongcloud.im.ui.activity;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.utils.DateUtils;
import cn.rongcloud.im.utils.SharedPreferencesContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;


public class DisturbActivity extends BaseActivity implements View.OnClickListener {


    private static final String TAG = DisturbActivity.class.getSimpleName();
    /**
     * 关闭勿扰模式
     */
    private LinearLayout mCloseNotification;
    /**
     * 开始时间 RelativeLayout
     */
    private RelativeLayout mStartNotification;
    /**
     * 关闭时间 RelativeLayout
     */
    private RelativeLayout mEndNotification;
    /**
     * 开始时间
     */
    private TextView mStartTimeNotification;
    /**
     * 关闭时间
     */
    private TextView mEndTimeNotification;
    /**
     * 开关
     */
    private CheckBox mNotificationCheckBox;
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
    boolean mIsSetting = false;
    private Handler mThreadHandler;
    private DisturbHandler mHandler = new DisturbHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disturb);

        initView();
        initData();
    }


    protected void initView() {
        mCloseNotification = (LinearLayout) findViewById(R.id.close_notification);
        mStartNotification = (RelativeLayout) findViewById(R.id.start_notification);
        mStartTimeNotification = (TextView) findViewById(R.id.start_time_notification);
        mEndNotification = (RelativeLayout) findViewById(R.id.end_notification);
        mEndTimeNotification = (TextView) findViewById(R.id.end_time_notification);
        mNotificationCheckBox = (CheckBox) findViewById(R.id.notification_checkbox);
        mThreadHandler = new Handler();
        Calendar calendar = Calendar.getInstance();
        hourOfDays = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
    }

    protected void initData() {
        setTitle(R.string.new_message_notice_title);

        mStartNotification.setOnClickListener(this);
        mEndNotification.setOnClickListener(this);
        mNotificationCheckBox.setOnClickListener(this);

        if (SharedPreferencesContext.getInstance().getSharedPreferences() != null) {
            mIsSetting = SharedPreferencesContext.getInstance().getSharedPreferences().getBoolean("IS_SETTING", false);
            if (mIsSetting) {

                Message msg = Message.obtain();
                msg.what = 3;
                mHandler.sendMessage(msg);
            } else {
                if (RongIM.getInstance() != null) {
                    mThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            RongIM.getInstance().getNotificationQuietHours(new RongIMClient.GetNotificationQuietHoursCallback() {
                                @Override
                                public void onSuccess(String startTime, int spanMins) {
                                    Log.e(TAG, "----yb----获取会话通知周期-onSuccess起始时间startTime:" + startTime + ",间隔分钟数spanMins:" + spanMins);
                                    if (spanMins > 0) {
                                        Message msg = Message.obtain();
                                        msg.what = 1;
                                        msg.obj = startTime;
                                        msg.arg1 = spanMins;
                                        mHandler.sendMessage(msg);
                                    } else {
                                        Message mssg = Message.obtain();
                                        mssg.what = 2;
                                        mHandler.sendMessage(mssg);
                                    }
                                }


                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    Log.e(TAG, "----yb----获取会话通知周期-oonError:" + errorCode);
                                    mNotificationCheckBox.setChecked(false);
                                    mCloseNotification.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }

            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_notification://开始时间

                if (SharedPreferencesContext.getInstance().getSharedPreferences() != null) {
                    String startTime = SharedPreferencesContext.getInstance().getSharedPreferences().getString("START_TIME", null);
                    if (startTime != null && !"".equals(startTime)) {
                        hourOfDays = Integer.parseInt(startTime.substring(0, 2));
                        minutes = Integer.parseInt(startTime.substring(3, 5));
                    }
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(DisturbActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mStartTime = getDaysTime(hourOfDay, minute);

                        mStartTimeNotification.setText(mStartTime);
                        SharedPreferences.Editor editor = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                        editor.putString("START_TIME", mStartTime);
                        editor.commit();

                        if (SharedPreferencesContext.getInstance().getSharedPreferences() != null) {
                            String endTime = SharedPreferencesContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                            if (endTime != null && !"".equals(endTime)) {
                                Date dataStart = DateUtils.stringToDate(mStartTime, mTimeFormat);
                                Date dataEnd = DateUtils.stringToDate(endTime, mTimeFormat);
                                long spansTime = DateUtils.compareMin(dataStart, dataEnd);
                                setConversationTime(mStartTime, (int) Math.abs(spansTime));
                            }
                        }
                    }
                }, hourOfDays, minutes, true);
                timePickerDialog.show();

                break;
            case R.id.end_notification://结束时间
                if (SharedPreferencesContext.getInstance().getSharedPreferences() != null) {
                    String endTime = SharedPreferencesContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                    if (endTime != null && !"".equals(endTime)) {
                        hourOfDays = Integer.parseInt(endTime.substring(0, 2));
                        minutes = Integer.parseInt(endTime.substring(3, 5));
                    }
                }
                Log.e("", "------结束时间---－－－－－－－－－－－－－－－－－－－－－-");
                timePickerDialog = new TimePickerDialog(DisturbActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Log.e("", "------结束时间---＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋");
                        mEndTime = getDaysTime(hourOfDay, minute);
                        mEndTimeNotification.setText(mEndTime);
                        SharedPreferences.Editor editor = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                        editor.putString("END_TIME", mEndTime);
                        editor.commit();

                        if (SharedPreferencesContext.getInstance().getSharedPreferences() != null) {
                            String startTime = SharedPreferencesContext.getInstance().getSharedPreferences().getString("START_TIME", null);
                            if (startTime != null && !"".equals(startTime)) {
                                Date dataStart = DateUtils.stringToDate(startTime, mTimeFormat);
                                Date dataEnd = DateUtils.stringToDate(mEndTime, mTimeFormat);
                                long spansTime = DateUtils.compareMin(dataStart, dataEnd);
                                Log.e("", "------结束时间----" + mEndTime);
                                Log.e("", "------开始时间----" + startTime);
                                Log.e("", "------时间间隔----" + spansTime);

                                setConversationTime(startTime, (int) Math.abs(spansTime));
                            }
                        }
                    }
                }, hourOfDays, minutes, true);
                timePickerDialog.show();

                break;
            case R.id.notification_checkbox://开关
                if (mNotificationCheckBox.isChecked()) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                } else {
                    if (RongIM.getInstance() != null) {

                        mThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                RongIM.getInstance().removeNotificationQuietHours(new RongIMClient.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.e(TAG, "----yb----移除会话通知周期-onSuccess");
                                        Message msg = Message.obtain();
                                        msg.what = 2;
                                        mHandler.sendMessage(msg);
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                        Log.e(TAG, "----yb-----移除会话通知周期-oonError:" + errorCode.getValue());
                                    }
                                });
                            }
                        });
                    }
                }
                break;
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

    /**
     * 设置勿扰时间
     *
     * @param startTime 设置勿扰开始时间 格式为：HH:mm:ss
     * @param spanMins  0 < 间隔时间 < 1440
     */
    private void setConversationTime(final String startTime, final int spanMins) {

        if (RongIM.getInstance() != null && startTime != null && !"".equals(startTime)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (spanMins > 0 && spanMins < 1440) {
                        Log.e("", "----设置勿扰时间startTime；" + startTime + "---spanMins:" + spanMins);

                        RongIM.getInstance().setNotificationQuietHours(startTime, spanMins, new RongIMClient.OperationCallback() {

                            @Override
                            public void onSuccess() {
                                Log.e(TAG, "----yb----设置会话通知周期-onSuccess");
                                SharedPreferences.Editor editor = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                                editor.putBoolean("IS_SETTING", true);
                                editor.commit();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        NToast.shortToast(mContext, "设置消息免打扰成功");
                                    }
                                });
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "----yb----设置会话通知周期-oonError:" + errorCode.getValue());
                            }
                        });
                    } else {
                        NToast.shortToast(mContext, "间隔时间必须>0");
                    }
                }
            });
        }
    }


    private static class DisturbHandler extends Handler {
        private final WeakReference<DisturbActivity> mActivity;

        public DisturbHandler(DisturbActivity activity) {
            mActivity = new WeakReference<DisturbActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SharedPreferences.Editor editor;
            if (msg != null) {
                DisturbActivity activity = mActivity.get();
                switch (msg.what) {
                    case 1:
                        activity.mNotificationCheckBox.setChecked(true);
                        activity.mCloseNotification.setVisibility(View.VISIBLE);
                        if (msg != null) {
                            activity.mStartTime = msg.obj.toString();
                            activity.hourOfDays = Integer.parseInt(activity.mStartTime.substring(0, 2));
                            activity.minutes = Integer.parseInt(activity.mStartTime.substring(3, 5));
                            int spanMins = msg.arg1;

                            String time = DateUtils.dateToString(DateUtils.addMinutes(DateUtils.stringToDate(activity.mStartTime, activity.mTimeFormat), spanMins), activity.mTimeFormat);
                            activity.mStartTimeNotification.setText(activity.mStartTime);
                            activity.mEndTimeNotification.setText(time);

                            editor = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                            editor.putString("START_TIME", activity.mStartTime);
                            editor.putString("END_TIME", DateUtils.dateToString(DateUtils.addMinutes(DateUtils.stringToDate(activity.mStartTime, activity.mTimeFormat), spanMins), activity.mTimeFormat));
                            editor.commit();
                        }
                        break;
                    case 2:
                        activity.mCloseNotification.setVisibility(View.GONE);
                        editor = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                        editor.remove("IS_SETTING");
                        editor.commit();
                        break;

                    case 3:
                        activity.mNotificationCheckBox.setChecked(true);
                        activity.mCloseNotification.setVisibility(View.VISIBLE);

                        if (SharedPreferencesContext.getInstance().getSharedPreferences() != null) {
                            String endtime = SharedPreferencesContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                            String starttimes = SharedPreferencesContext.getInstance().getSharedPreferences().getString("START_TIME", null);

                            if (endtime != null && starttimes != null && !"".equals(endtime) && !"".equals(starttimes)) {
                                Date datastart = DateUtils.stringToDate(starttimes, activity.mTimeFormat);
                                Date dataend = DateUtils.stringToDate(endtime, activity.mTimeFormat);
                                long spansTime = DateUtils.compareMin(datastart, dataend);
                                activity.mStartTimeNotification.setText(starttimes);
                                activity.mEndTimeNotification.setText(endtime);
                                activity.setConversationTime(starttimes, (int) spansTime);
                            } else {
                                activity.mStartTimeNotification.setText("23:59:59");
                                activity.mEndTimeNotification.setText("07:00:00");
                                editor = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                                editor.putString("START_TIME", "23:59:59");
                                editor.putString("END_TIME", "07:00:00");
                                editor.commit();
                            }
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
