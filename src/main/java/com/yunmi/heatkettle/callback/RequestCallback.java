package com.yunmi.heatkettle.callback;

/**
 * 请求回调
 * Created by William on 2017/5/15.
 */

public interface RequestCallback<T> {

    /**
     * 请求成功回调
     *
     * @param data：返回数据
     */
    void onSuccess(T data);

    /**
     * 请求失败回调
     *
     * @param errorCode：错误码
     * @param errorInfo：错误信息
     */

    void onFailure(int errorCode, String errorInfo);
}
