package com.anjiawei.lib_network.okhttp.response;

import android.os.Handler;
import android.os.Looper;

import com.anjiawei.lib_network.okhttp.exception.OkHttpException;
import com.anjiawei.lib_network.okhttp.response.listener.DisposeDataHandle;
import com.anjiawei.lib_network.okhttp.response.listener.DisposeDataListener;
import com.anjiawei.lib_network.okhttp.utils.ResponseEntityToModule;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CommonJsonCallback implements Callback {
    /**
     * the logic layer exception, may alter in different app
     */
    protected final String RESULT_CODE = "ecode"; // 有返回则对于http请求来说是成功的，但还有可能是业务逻辑上的错误
    protected final int RESULT_CODE_VALUE = 0;
    protected final String ERROR_MSG = "emsg";
    protected final String EMPTY_MSG = "";

    /**
     * the java layer exception, do not same to the logic error
     */
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int JSON_ERROR = -2; // the JSON relative error
    protected final int OTHER_ERROR = -3; // the unknow error

    private DisposeDataListener mListener;
    private Class<?> mClass;
    private Handler mDeliveryHandler; // 主线程handler，把回调函数扔回主线程执行

    public CommonJsonCallback(DisposeDataHandle handle) {
        mListener = handle.mListener;
        mClass = handle.mClass;
        mDeliveryHandler = new Handler(Looper.getMainLooper());
    }


    @Override
    public void onFailure(Call call, IOException e) {
        mDeliveryHandler.post(() -> mListener.onFailure(new OkHttpException(NETWORK_ERROR, e)));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String result = response.body().string();
        mDeliveryHandler.post(() -> handleResponse(result));
    }

    private void handleResponse(String result) {
        if (result == null || result.trim().length() == 0) {
            mListener.onFailure(new OkHttpException(NETWORK_ERROR, EMPTY_MSG));
            return;
        }
        try {
            // 不需要解析返回的json
            if (mClass == null) {
                mListener.onSuccess(result);
            } else {
                Object obj = ResponseEntityToModule.parseJsonToModule(result, mClass);
                if (obj == null) {
                    mListener.onFailure(new OkHttpException(JSON_ERROR, EMPTY_MSG));
                } else {
                    mListener.onSuccess(obj);
                }
            }
        } catch (Exception e) {
            mListener.onFailure(new OkHttpException(OTHER_ERROR, e));
        }
    }
}
