package cn.rongcloud.im.model;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class QuietHours {
    static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    public String startTime;
    public int spanMinutes;
    public boolean isDonotDistrab;


    /**
     * 获得开始时间的格式化
     *
     * @return
     */
    public String getStartTimeFormat() {
        if (TextUtils.isEmpty(startTime)) {
            return "";
        }
        return startTime;
    }

    /**
     * 结束时间
     *
     * @return
     */
    public String getEndTimeFormat() {
        if (TextUtils.isEmpty(startTime)) {
            return "";
        }

        try {
            Long startTimeLong = getStartTime();
            Date date = new Date(startTimeLong + spanMinutes * 60 * 1000);
            return format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得开始时间 long
     *
     * @return
     */
    public long getStartTime() {
        try {
            Date parse = format.parse(startTime);
            return parse.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 获得结束时间
     *
     * @return
     */
    public long getEndTime() {
        try {
            Long startTimeLong = getStartTime();
            return startTimeLong + spanMinutes * 60 * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获得时间的小时
     *
     * @param time
     * @return
     */
    public static int getHours(long time) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            return calendar.get(Calendar.HOUR_OF_DAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获得时间的分钟
     *
     * @param time
     * @return
     */
    public static int getMinutes(long time) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            return calendar.get(Calendar.MINUTE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取时间间隔
     *
     * @param startTimeFormat
     * @param endTimeFormat
     * @return
     */
    public static int getSpanMinutes(String startTimeFormat, String endTimeFormat) {
        try {
            Date startDate = format.parse(startTimeFormat);
            long startTime = startDate.getTime();
            Date endDate = format.parse(endTimeFormat);
            long endTime = endDate.getTime();

            if (startTime > endTime) {
                endTime = endTime + 24 * 60 * 60 * 1000;
            }

            long spanMinutes = (endTime - startTime) / 60 / 1000;

            return (int) spanMinutes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 得到"HH:mm:ss"类型时间
     *
     * @param hourOfDay 小时
     * @param minite    分钟
     * @return "HH:mm:ss"类型时间
     */
    public static String getFormatTime(final int hourOfDay, final int minite) {
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
}
