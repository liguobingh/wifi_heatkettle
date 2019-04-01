package com.yunmi.heatkettle.utils;

import android.util.Log;

/**
 * 打印日志
 * Created by William on 2017/5/15.
 */

public class log {
    private static boolean isDebug = true;

    public static void d(String tag, String msg) {
        if (isDebug) Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug) Log.e(tag, msg);
    }
}
