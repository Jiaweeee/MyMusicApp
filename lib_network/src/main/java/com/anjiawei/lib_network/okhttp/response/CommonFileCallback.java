package com.anjiawei.lib_network.okhttp.response;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.anjiawei.lib_network.okhttp.exception.OkHttpException;
import com.anjiawei.lib_network.okhttp.response.listener.DisposeDataHandle;
import com.anjiawei.lib_network.okhttp.response.listener.DisposeDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 处理文件类型的相应
 */
public class CommonFileCallback implements Callback {
    /**
     * the java layer exception, do not same to the logic error
     */
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int IO_ERROR = -2; // the JSON relative error
    protected final String EMPTY_MSG = "";
    /**
     * 将其它线程的数据转发到UI线程
     */
    private static final int PROGRESS_MESSAGE = 0x01;
    private Handler mDeliveryHandler;
    private DisposeDownloadListener mListener;
    private String mFilePath;
    private int mProgress; // 当前进度

    public CommonFileCallback(DisposeDataHandle handle) {
        mListener = (DisposeDownloadListener) handle.mListener;
        mFilePath = handle.mSource;
        mDeliveryHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PROGRESS_MESSAGE:
                        mListener.onProgress((int) msg.obj);
                        break;
                }
            }
        };
    }

    @Override
    public void onFailure(Call call, IOException e) {
        mDeliveryHandler.post(() -> mListener.onFailure(new OkHttpException(NETWORK_ERROR, e)));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        File file = handleResponse(response);
        mDeliveryHandler.post(() -> {
            if (file == null) {
                mListener.onFailure(new OkHttpException(IO_ERROR, EMPTY_MSG));
            } else {
                mListener.onSuccess(file);
            }
        });
    }

    private File handleResponse(Response response) {
        if (response == null) {
            return null;
        }
        checkLocalFilePath(mFilePath);
        File file = new File(mFilePath);
        byte[] buffer = new byte[2048];
        int length;
        int curLen = 0;
        double sumLen;
        try (InputStream inputStream = response.body().byteStream();
             FileOutputStream fos = new FileOutputStream(file)) {

            sumLen = response.body().contentLength();
            while ((length = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
                curLen += length;
                mProgress = (int) (curLen / sumLen * 100);
                mDeliveryHandler.obtainMessage(PROGRESS_MESSAGE, mProgress).sendToTarget();
            }
            fos.flush();
        } catch (Exception e) {
            file = null;
        }
        return file;
    }

    private void checkLocalFilePath(String localFilePath) {
        File path = new File(localFilePath.substring(0, localFilePath.lastIndexOf('/') + 1));
        File file = new File(localFilePath);
        if (!path.exists()) {
            path.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
