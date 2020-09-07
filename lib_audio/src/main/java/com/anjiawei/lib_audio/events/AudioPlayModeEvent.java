package com.anjiawei.lib_audio.events;

import com.anjiawei.lib_audio.mediaplayer.core.AudioController;

public class AudioPlayModeEvent {
    public AudioController.PlayMode mPlayMode;

    public AudioPlayModeEvent(AudioController.PlayMode playMode) {
        mPlayMode = playMode;
    }
}
