package com.ivan.musiclog.exception;

public class DuplicateAlbumReviewException extends RuntimeException {
    public DuplicateAlbumReviewException(String message) {
        super(message);
    }
}
