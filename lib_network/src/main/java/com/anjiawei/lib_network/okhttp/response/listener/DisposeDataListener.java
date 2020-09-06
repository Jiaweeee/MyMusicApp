package com.anjiawei.lib_network.okhttp.response.listener;

/**
 * 回调接口，供UI层回调更新页面
 */
public interface DisposeDataListener {
    /**
     * 请求成功回调事件处理
     *
     * @param responseObj
     */
    void onSuccess(Object responseObj);

    /**
     * 请求失败回调事件处理
     *
     * @param reasonObj
     */
    void onFailure(Object reasonObj);
}
