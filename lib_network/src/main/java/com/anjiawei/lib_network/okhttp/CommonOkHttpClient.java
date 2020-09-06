package com.anjiawei.lib_network.okhttp;

import com.anjiawei.lib_network.okhttp.response.CommonFileCallback;
import com.anjiawei.lib_network.okhttp.response.CommonJsonCallback;
import com.anjiawei.lib_network.okhttp.response.listener.DisposeDataHandle;

import java.util.concurrent.TimeUnit;


import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 用来发送get，post请求的工具类，包括设置一些请求的公共参数
 */
public class CommonOkHttpClient {

    private static final int TIME_OUT = 30;
    private static OkHttpClient mOkHttpClient;

    static {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.hostnameVerifier((hostname, session) -> true);
        /**
         * 添加公共请求头
         */
        okHttpClientBuilder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("User-Agent", "Imooc-mobile").build();
            return chain.proceed(request);
        });
        okHttpClientBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.followRedirects(true);
        mOkHttpClient = okHttpClientBuilder.build();
    }

    /**
     * 发送get请求
     *
     * @param request
     * @param handle
     * @return
     */
    public static Call get(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    /**
     * 发送post请求
     *
     * @param request
     * @param handle
     * @return
     */
    public static Call post(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    /**
     * 发送文件下载请求
     *
     * @param request
     * @param handle
     * @return
     */
    public static Call downloadFile(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonFileCallback(handle));
        return call;
    }
}
