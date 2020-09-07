package com.anjiawei.lib_audio.mediaplayer.core;

import com.anjiawei.lib_audio.events.AudioCompleteEvent;
import com.anjiawei.lib_audio.events.AudioErrorEvent;
import com.anjiawei.lib_audio.events.AudioPlayModeEvent;
import com.anjiawei.lib_audio.exception.AudioQueueEmptyException;
import com.anjiawei.lib_audio.model.AudioBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Random;

public class AudioController {
    /**
     * 播放模式
     */
    public enum PlayMode {
        /**
         * 列表循环
         */
        LOOP,
        /**
         * 随机
         */
        RANDOM,
        /**
         * 单曲循环
         */
        REPEAT
    }

    private AudioPlayer mAudioPlayer;
    private int mQueueIndex;
    private ArrayList<AudioBean> mQueue;
    private PlayMode mPlayMode;

    private AudioController() {
        mAudioPlayer = new AudioPlayer();
        mQueueIndex = 0;
        mQueue = new ArrayList<>();
        mPlayMode = PlayMode.LOOP;
        EventBus.getDefault().register(this);
    }

    public AudioController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static AudioController INSTANCE = new AudioController();
    }

    /*
     * 获取播放器当前状态
     */
    private CustomMediaPlayer.Status getStatus() {
        return mAudioPlayer.getStatus();
    }

    private AudioBean getPlaying() {
        if (mQueue != null && !mQueue.isEmpty() && mQueueIndex >= 0 && mQueueIndex < mQueue.size()) {
            return mQueue.get(mQueueIndex);
        } else {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
    }

    private AudioBean getNextPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + 1) % mQueue.size();
                break;
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                break;
            case REPEAT:
                break;
        }
        return getPlaying();
    }

    private AudioBean getPreviousPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex - 1) % mQueue.size();
                break;
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                break;
            case REPEAT:
                break;
        }
        return getPlaying();
    }

    private void load(AudioBean bean) {
        mAudioPlayer.load(bean);
    }

    public ArrayList<AudioBean> getQueue() {
        return mQueue == null ? new ArrayList<AudioBean>() : mQueue;
    }

    /**
     * 设置播放队列
     *
     * @param queue
     */
    public void setQueue(ArrayList<AudioBean> queue) {
        this.setQueue(queue, 0);
    }

    public void setQueue(ArrayList<AudioBean> queue, int queueIndex) {
        mQueue.addAll(queue);
        mQueueIndex = queueIndex;
    }

    private void addCustomAudio(int index, AudioBean bean) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueue.add(index, bean);
    }

    private int queryAudio(AudioBean bean) {
        return mQueue.indexOf(bean);
    }


    /**
     * 添加单一歌曲到指定位置
     *
     * @param bean
     */
    public void addAudio(AudioBean bean) {
        this.addAudio(0, bean);
    }

    public void addAudio(int index, AudioBean bean) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空");
        }
        int query = queryAudio(bean);
        if (query <= -1) {
            //没有添加过
            addCustomAudio(index, bean);
            setPlayIndex(index);
        } else {
            AudioBean currentBean = getNowPlaying();
            if (!currentBean.id.equals(bean.id)) {
                //已经添加过且不在播放中
                setPlayIndex(query);
            }
        }
    }

    public PlayMode getPlayMode() {
        return mPlayMode;
    }

    /**
     * 对外提供设置播放模式
     *
     * @param playMode
     */
    public void setPlayMode(PlayMode playMode) {
        mPlayMode = playMode;
        //还要对外发送切换事件，更新UI
        EventBus.getDefault().post(new AudioPlayModeEvent(mPlayMode));
    }

    public void setPlayIndex(int index) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueueIndex = index;
        play();
    }

    public int getPlayIndex() {
        return mQueueIndex;
    }

    /**
     * 对外提供的获取当前歌曲信息
     */
    public AudioBean getNowPlaying() {
        return getPlaying();
    }

    /**
     * 对外提供的play方法
     */
    public void play() {
        AudioBean bean = getNowPlaying();
        load(bean);
    }

    public void pause() {
        mAudioPlayer.pause();
    }

    public void resume() {
        mAudioPlayer.resume();
    }

    public void release() {
        mAudioPlayer.release();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 播放下一首歌曲
     */
    public void next() {
        AudioBean bean = getNextPlaying();
        load(bean);
    }

    /**
     * 播放下一首
     */
    public void previous() {
        AudioBean bean = getPreviousPlaying();
        load(bean);
    }

    /**
     * 自动切换播放/暂停
     */
    public void playOrPause() {
        if (isStartState()) {
            pause();
        } else if (isPauseState()) {
            resume();
        }
    }

    /**
     * 对外提供是否播放中状态
     */
    public boolean isStartState() {
        return CustomMediaPlayer.Status.STARTED == getStatus();
    }

    /**
     * 对外提提供是否暂停状态
     */
    public boolean isPauseState() {
        return CustomMediaPlayer.Status.PAUSED == getStatus();
    }

    //插放完毕事件处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioCompleteEvent(AudioCompleteEvent event) {
        next();
    }

    //播放出错事件处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioErrorEvent(AudioErrorEvent event) {
        next();
    }
}
