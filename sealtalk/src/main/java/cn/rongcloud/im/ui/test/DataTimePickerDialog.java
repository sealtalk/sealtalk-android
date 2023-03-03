package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.Calendar;

/** 选年月日时分秒滚轮弹窗 */
public class DataTimePickerDialog extends BottomSheetDialog {

    public DataTimePickerDialog(@NonNull Context context) {
        super(context);
        initView(context);
    }

    private NumberPicker np_year, np_month, np_day, np_hours, np_minutes, np_second;
    private PickerPositiveListener mPositiveListener;

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_date, null, false);
        setContentView(view);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() { // 禁止该弹窗可滑动
                    @Override
                    public void onStateChanged(
                            @NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        if (newState == BottomSheetBehavior.STATE_SETTLING) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
                });

        np_year = findViewById(R.id.np_year);
        np_month = findViewById(R.id.np_month);
        np_day = findViewById(R.id.np_day);
        np_hours = findViewById(R.id.np_hours);
        np_minutes = findViewById(R.id.np_minutes);
        np_second = findViewById(R.id.np_second);
        findViewById(R.id.tv_cancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.tv_ok)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String curDate =
                                        np_year.getValue()
                                                + "-"
                                                + np_month.getValue()
                                                + "-"
                                                + np_day.getValue()
                                                + " "
                                                + np_hours.getValue()
                                                + ":"
                                                + np_minutes.getValue()
                                                + ":"
                                                + np_second.getValue();
                                mPositiveListener.click(curDate);
                                dismiss();
                            }
                        });

        Calendar cal = Calendar.getInstance();
        // 当前年
        int year = cal.get(Calendar.YEAR);
        // 当前月
        int month = (cal.get(Calendar.MONTH)) + 1;
        // 当前月的第几天：即当前日
        int day_of_month = cal.get(Calendar.DAY_OF_MONTH);
        // 获取当前月的天数
        int days = cal.getActualMaximum(Calendar.DATE);
        // 当前时：HOUR_OF_DAY-24小时制；HOUR-12小时制
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        // 当前分
        int minute = cal.get(Calendar.MINUTE);
        // 获取当前秒
        int second = cal.get(Calendar.SECOND);

        np_year.setMaxValue(year);
        np_year.setMinValue(year);
        np_year.setValue(year);
        np_year.setWrapSelectorWheel(false);
        np_year.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        np_month.setMaxValue(12);
        np_month.setMinValue(1);
        np_month.setValue(month);
        np_month.setWrapSelectorWheel(false);
        np_month.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        np_month.setOnValueChangedListener(
                new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int old, int current) {
                        int newDays = getDays(year, current);
                        np_day.setMaxValue(newDays);
                        np_day.setMinValue(1);
                        np_day.setValue(1);
                    }
                });
        np_day.setMaxValue(days);
        np_day.setMinValue(1);
        np_day.setValue(day_of_month);
        np_day.setWrapSelectorWheel(false);
        np_day.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        np_hours.setMaxValue(23);
        np_hours.setMinValue(0);
        np_hours.setValue(hour);
        np_hours.setWrapSelectorWheel(false);
        np_hours.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        np_minutes.setMaxValue(59);
        np_minutes.setMinValue(0);
        np_minutes.setValue(minute);
        np_minutes.setWrapSelectorWheel(false);
        np_minutes.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        np_second.setMaxValue(59);
        np_second.setMinValue(0);
        np_second.setValue(second);
        np_second.setWrapSelectorWheel(false);
        np_second.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
    }

    public void setPositiveListener(PickerPositiveListener mPositiveListener) {
        this.mPositiveListener = mPositiveListener;
    }

    public interface PickerPositiveListener {
        void click(String curDate);
    }

    // 判断闰年
    boolean isLeap(int year) {
        if (((year % 100 == 0) && year % 400 == 0) || ((year % 100 != 0) && year % 4 == 0))
            return true;
        else return false;
    }

    // 返回当月天数
    int getDays(int year, int month) {
        int days;
        int FebDay = 28;
        if (isLeap(year)) FebDay = 29;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                days = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                days = 30;
                break;
            case 2:
                days = FebDay;
                break;
            default:
                days = 0;
                break;
        }
        return days;
    }
}
