package com.ivan.musiclog.exception;

public class SpotifyNotFoundException extends RuntimeException {
    public SpotifyNotFoundException(String message) {
        super(message);
    }
}
