package com.anjiawei.lib_network.okhttp.response.listener;

public interface DisposeDownloadListener extends DisposeDataListener {
    void onProgress(int progress);
}
