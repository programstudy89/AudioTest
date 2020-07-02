package com.samsung.test.audioproejct.support;

public class FormatUtils {

    public static String formatSecond(long milli) {
        long minDuration = calculateMinDuration(milli);
        long h = minDuration / 60;
        long m = minDuration % 60;
        long s = milli / 1000 % 60;

        if (milli >= (1000L * 60 * 60) ) {
            return String.format("%d:%d:%d", h, m, s);
        } else if (milli >= (1000L * 60) ) {
            return String.format("%d:%d", m, s);
        } else {
            return String.format("0:%d", s);
        }
    }

    public static long calculateMinDuration(long milli) {
        return milli / 1000 / 60;
    }

}
