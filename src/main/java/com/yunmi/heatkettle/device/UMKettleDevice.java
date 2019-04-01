package com.yunmi.heatkettle.device;

import android.util.Log;

import com.xiaomi.smarthome.device.api.BaseDevice;
import com.xiaomi.smarthome.device.api.Callback;
import com.xiaomi.smarthome.device.api.DeviceStat;
import com.xiaomi.smarthome.device.api.DeviceUpdateInfo;
import com.xiaomi.smarthome.device.api.Parser;
import com.xiaomi.smarthome.device.api.XmPluginHostApi;
import com.yunmi.heatkettle.callback.RequestCallback;
import com.yunmi.heatkettle.data.UMDeviceInfo;
import com.yunmi.heatkettle.data.UMGetPropDecode;
import com.yunmi.heatkettle.utils.log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// 设备功能处理
public class UMKettleDevice extends BaseDevice {
    private static final String TAG = UMKettleDevice.class.getSimpleName();
    public static final String MODEL = "yunmi.waterpuri.h11";

    // 缓存设备状态数据，每次进入不需要立即更新数据
    private static ArrayList<UMKettleDevice> DEVICE_CACHE = new ArrayList<>();

    private UMDeviceInfo mUMWaterFilterInfo = new UMDeviceInfo();
    private String errorMsg = null;  // 请求错误信息
    public boolean isUpdateRunning = false; // 判断是否更新数据中

    // 先从缓存中获取Device，并更新DeviceStat
    public static synchronized UMKettleDevice getDevice(DeviceStat deviceStat) {
        for (UMKettleDevice device : DEVICE_CACHE) {
            if (deviceStat.did.equals(device.getDid())) {
                device.mDeviceStat = deviceStat;
                return device;
            }
        }

        UMKettleDevice device = new UMKettleDevice(deviceStat);
        DEVICE_CACHE.add(device);
        return device;
    }

    // 通过did获取Device
    public static synchronized UMKettleDevice getDevice(String did) {
        for (UMKettleDevice device : DEVICE_CACHE) {
            if (did.equals(device.getDid())) {
                return device;
            }
        }
        return null;
    }

    public UMKettleDevice(DeviceStat deviceStat) {
        super(deviceStat);
    }

    // 刷新属性
    public void updateProperty(final String[] properties) {
        isUpdateRunning = true;
        if (properties == null || properties.length == 0) {
            isUpdateRunning = false;
            return;
        }

        JSONArray params = new JSONArray();
        for (String prop : properties) {
            params.put(prop);
        }

        callMethod("get_prop", params, new Callback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject result) {
                isUpdateRunning = false;
                log.d(TAG, "get_prop success,result:" + result.toString());
                if (result.length() == 0) return;
                mUMWaterFilterInfo = UMGetPropDecode.decode(result);
                errorMsg = null;
                if (mUMWaterFilterInfo != null) notifyStateChanged();
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                isUpdateRunning = false;
                errorMsg = errorInfo;
                if (mUMWaterFilterInfo != null) notifyStateChanged();
                log.e(TAG, "get_prop fail,errorCode=" + errorCode + "errorInfo:" + errorInfo);
            }
        }, new Parser<JSONObject>() {

            @Override
            public JSONObject parse(String result) throws JSONException {
                return new JSONObject(result);
            }
        });
    }

    /**
     * 设置属性
     *
     * @param method: 方法
     * @param values: 设置参数
     */
    public void setProperty(String method, Object[] values, final RequestCallback<JSONObject> callback) {
        if (method == null || values == null || values.length == 0) {
            callback.onFailure(101, "params error");
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (Object param : values) {
            jsonArray.put(param);
            log.d(TAG, "setProperty result:" + param + "++789");
        }


        callMethod(method, jsonArray, new Callback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                log.d(TAG, "setProperty result:" + jsonObject);
                log.d(TAG, "setProperty result:" + jsonObject + "456");
                if (jsonObject == null) {
                    callback.onFailure(-99, "json null");
                    return;
                }
                JSONArray array = jsonObject.optJSONArray("result");
                if (array == null || array.length() == 0) {
                    callback.onFailure(101, "result null");
                    return;
                }
                String message = array.optString(0);
                log.d(TAG, message);
                log.d(TAG, message + "123");
                if (!message.equals("ok")) {
                    callback.onFailure(103, message);
                    return;
                }
                callback.onSuccess(jsonObject);
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                callback.onFailure(errorCode, errorInfo);
            }
        }, new Parser<JSONObject>() {
            @Override
            public JSONObject parse(String result) throws JSONException {
                return new JSONObject(result);
            }
        });
    }

    // 订阅属性变化，每次只维持3分钟订阅事件
    public void subscribeProperty(String[] props, Callback<Void> callback) {
        ArrayList<String> propList = new ArrayList<String>();
        for (String prop : props) {
            if (prop.startsWith("prop.")) {
                propList.add(prop);
            } else {
                propList.add("prop." + prop);
            }
        }
        XmPluginHostApi.instance()
                .subscribeDevice(getDid(), mDeviceStat.pid, propList, 3, callback);
    }

    // 订阅事件信息，每次只维持3分钟订阅事件
    public void subscribeEvent(String[] events, Callback<Void> callback) {
        ArrayList<String> eventList = new ArrayList<String>();
        for (String event : events) {
            if (event.startsWith("event.")) {
                eventList.add(event);
            } else {
                eventList.add("event." + event);
            }
        }
        XmPluginHostApi.instance().subscribeDevice(getDid(), mDeviceStat.pid, eventList, 3,
                callback);
    }

    // 收到订阅的信息
    public void onSubscribeData(String data) {
        Log.d(UMKettleDevice.MODEL, "DevicePush :" + data);
        //TODO 处理订阅信息
    }


    // 取消订阅属性，在订阅后，再次订阅前取消，避免重复订阅
    public void unSubscribeProperty(String[] props, Callback<Void> callback) {
        ArrayList<String> propList = new ArrayList<String>();
        for (String prop : props) {
            if (prop.startsWith("prop.")) {
                propList.add(prop);
            } else {
                propList.add("prop." + prop);
            }
        }
        XmPluginHostApi.instance()
                .unsubscribeDevice(getDid(), mDeviceStat.pid, propList, callback);
    }

    // 取消订阅事件消息，在订阅后，再次订阅前取消，避免重复订阅
    public void unSubscribeEvent(String[] events, Callback<Void> callback) {
        ArrayList<String> eventList = new ArrayList<String>();
        for (String event : events) {
            if (event.startsWith("event.")) {
                eventList.add(event);
            } else {
                eventList.add("event." + event);
            }
        }
        XmPluginHostApi.instance().unsubscribeDevice(getDid(), mDeviceStat.pid, eventList,
                callback);
    }

    // 检查固件更新
    public void checkUpdateInfo(final RequestCallback<DeviceUpdateInfo> callback) {
        checkDeviceUpdateInfo(new Callback<DeviceUpdateInfo>() {
            @Override
            public void onSuccess(DeviceUpdateInfo deviceUpdateInfo) {
                callback.onSuccess(deviceUpdateInfo);
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                callback.onFailure(errorCode, errorInfo);
            }
        });
    }

    public UMDeviceInfo getDeviceInfo() {
        return mUMWaterFilterInfo;
    }

    public String getErrorInfo() {
        return errorMsg;
    }
}

