package com.yunmi.heatkettle.data;

import com.yunmi.heatkettle.utils.UMUtils;
import com.yunmi.heatkettle.utils.log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 解析 GetProp Json
 * Created by William on 2017/5/18.
 */

public class UMGetPropDecode {
    private static final String TAG = UMGetPropDecode.class.getSimpleName();

    public static UMDeviceInfo decode(JSONObject jsonObject) {
        UMDeviceInfo info = new UMDeviceInfo();
        if (jsonObject == null) return null;

        // 非正常返回
        int code = jsonObject.optInt("code");
        if (code != 0) return null;

        try {
            JSONArray jsonArray = jsonObject.optJSONArray("result");
            if (jsonArray.length() <= 0) return null;

            // 开始解析
            int i = 0;
            info.water_remain_time = isError(i, jsonArray);
            i++;
            info.flush_time = isError(i, jsonArray);
            i++;
            info.flush_flag = isError(i, jsonArray);
            i++;
            info.tds = isError(i, jsonArray);
            i++;
            info.time = isError(i, jsonArray);
            i++;
            info.curr_tempe = isError(i, jsonArray);
            i++;
            info.setup_tempe = isError(i, jsonArray);
            i++;
            info.custom_tempe1 = isError(i, jsonArray);
            i++;
            info.min_set_tempe = isError(i, jsonArray);
            i++;
            info.drink_remind = isError(i, jsonArray);
            i++;
            info.drink_remind_time = isError(i, jsonArray);
            i++;
            info.run_status = isError(i, jsonArray);
            i++;
            info.work_mode = isError(i, jsonArray);
            i++;
            info.drink_time_count = isError(i, jsonArray);
            if (info.run_status != -2 && info.run_status != -1) {
                info.statusByte = UMUtils.longToByte(info.run_status);
                info.statusCount = 0;
                for (int j = 21; j < 25; j++) {
                    if (info.statusByte[j] == 1) {
                        info.statusCount++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.e(TAG, e.toString());
            return null;
        }
        return info;
    }

    private static int isError(int i, JSONArray jsonArray) {
        if (i >= jsonArray.length()) return -2;
        else if (jsonArray.optString(i).equals("error")) return -1;
        else return jsonArray.optInt(i);
    }

}
