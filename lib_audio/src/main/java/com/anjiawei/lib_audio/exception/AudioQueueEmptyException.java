package com.anjiawei.lib_audio.exception;

public class AudioQueueEmptyException extends RuntimeException {
    public AudioQueueEmptyException(String error) {
        super(error);
    }
}
