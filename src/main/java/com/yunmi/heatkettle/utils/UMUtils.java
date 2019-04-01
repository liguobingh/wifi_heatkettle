package com.yunmi.heatkettle.utils;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 * Created by William on 2017/6/1.
 */

public class UMUtils {
    /**
     * 提取字符串中所有数字
     */
    public static int getIntFromStr(String str) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String result = m.replaceAll("").trim();
        return Integer.valueOf(result);
    }

    /**
     * dp2px
     */
    public static int dp2px(int values, Context context) {
        //getResources()：返回应用程序包的Resources实例。
        //getDisplayMetrics()：返回对此资源对象有效的当前显示指标。 返回的对象应视为只读。
        /*density：显示器的逻辑密度。 这是密度独立像素单元的缩放因子，其中一个DIP是大约160dpi屏幕上的一个像素（例如240x320,1.5“x2”屏幕），提供系统显示的基线。 因此，在160dpi屏幕上，此密度值将为1; 在120 dpi的屏幕上它将是.75; 等等
        此值并不完全遵循实际屏幕大小（由xdpi和ydpi给出，而是用于根据显示dpi中的总体更改逐步缩放整个UI的大小。例如，240x320屏幕将具有 密度为1，即使其宽度为1.8“，1.3”等。但是，如果屏幕分辨率增加到320x480，但屏幕尺寸保持为1.5“x2”，那么密度将增加（可能增加到1.5）。*/
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (values * density + 0.5f);
    }

    /**
     * 获取 density
     */
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static byte[] longToByte(long number) {
        byte[] statusByte = new byte[26];
        long temp = number;
        for (int i = 0; i < statusByte.length; i++) {
            statusByte[i] = (byte) (temp & 1);
            temp = temp >> 1;
        }
        return statusByte;
    }
}
