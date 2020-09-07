package com.anjiawei.lib_audio.mediaplayer.core;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.anjiawei.lib_audio.app.AudioHelper;
import com.anjiawei.lib_audio.events.AudioCompleteEvent;
import com.anjiawei.lib_audio.events.AudioErrorEvent;
import com.anjiawei.lib_audio.events.AudioLoadEvent;
import com.anjiawei.lib_audio.events.AudioPauseEvent;
import com.anjiawei.lib_audio.events.AudioReleaseEvent;
import com.anjiawei.lib_audio.events.AudioStartEvent;
import com.anjiawei.lib_audio.model.AudioBean;

import org.greenrobot.eventbus.EventBus;

/**
 * 1.播放音频
 * 2.对外发送各种类型的事件
 */
public class AudioPlayer implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener
        , MediaPlayer.OnErrorListener, AudioFocusManager.AudioFocusListener {

    private static final String TAG = "AudioPlayer";
    private static final int TIME_MSG = 0x01;
    private static final int TIME_INVAL = 100;

    //真正负责音频的播放
    private CustomMediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    //章频焦点监听器
    private AudioFocusManager mAudioFocusManager;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_MSG:
                    break;
            }
        }
    };
    private boolean isLossByFocusTransient;

    public AudioPlayer() {
        init();
    }

    //初始化
    private void init() {
        mMediaPlayer = new CustomMediaPlayer();
        mMediaPlayer.setWakeMode(null, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnErrorListener(this);
        //初始化wifilock
        mWifiLock = ((WifiManager) AudioHelper.getContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
        mAudioFocusManager = new AudioFocusManager(AudioHelper.getContext(), this);
    }

    // 内部开始播放
    private void start() {
        if (!mAudioFocusManager.requestAudioFocus()) {
            Log.e(TAG, "获取焦点失败");
        }
        mMediaPlayer.start();
        mWifiLock.acquire();
        // 对外发送start事件
        EventBus.getDefault().post(new AudioStartEvent());
    }

    // 内部设置音量
    private void setVolume(float leftVol, float rightVol) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(leftVol, rightVol);
        }
    }

    /**
     * 对外提供加载方法
     *
     * @param bean
     */
    public void load(AudioBean bean) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(bean.mUrl);
            mMediaPlayer.prepareAsync();
            // 对外发送load事件
            EventBus.getDefault().post(new AudioLoadEvent(bean));
        } catch (Exception e) {
            // 对外发送error事件
            EventBus.getDefault().post(new AudioErrorEvent());
        }
    }


    /**
     * 对外提供暂停方法
     */
    public void pause() {
        if (getStatus() == CustomMediaPlayer.Status.STARTED) {
            mMediaPlayer.pause();
            // 释放音频焦点wifilock
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
            // 释放音频焦点
            if (mAudioFocusManager != null) {
                mAudioFocusManager.abandonAudioFocus();
            }
            // 发送暂停事件
            EventBus.getDefault().post(new AudioPauseEvent());
        }
    }

    /**
     * 对外提供恢复方法
     */
    public void resume() {
        if (getStatus() == CustomMediaPlayer.Status.PAUSED) {
            start();
        }
    }

    /**
     * 对外提供销毁方法
     */
    public void release() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
        if (mAudioFocusManager != null) {
            mAudioFocusManager.abandonAudioFocus();
        }
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        mAudioFocusManager = null;
        mWifiLock = null;
        // 发送release销毁事件
        EventBus.getDefault().post(new AudioReleaseEvent());
    }

    /**
     * 获取播放器当前状态
     *
     * @return
     */
    public CustomMediaPlayer.Status getStatus() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getState();
        }
        return CustomMediaPlayer.Status.STOPPTED;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // 缓存进度回调
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 播放完毕回调
        EventBus.getDefault().post(new AudioCompleteEvent());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // 播放出错回调
        EventBus.getDefault().post(new AudioErrorEvent());
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // 准备完毕
        start();
    }

    @Override
    public void audioFocusGrant() {
        // 再次获得音频焦点
        setVolume(1.0F, 1.0F);
        if (isLossByFocusTransient) {
            resume();
        }
        isLossByFocusTransient = false;
    }

    @Override
    public void audioFocusLoss() {
        // 永久失去焦点
        pause();
    }

    @Override
    public void audioFocusLossTransient() {
        // 短暂失去焦点
        pause();
        isLossByFocusTransient = true;
    }

    @Override
    public void audioFocusLossDuck() {
        // 瞬间失去焦点
        setVolume(0.5F, 0.5F);
    }
}

