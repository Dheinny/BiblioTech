package com.fenix.bibliotech.exception;

public class ResourceConflictException extends BusinessException {
    public ResourceConflictException(String message, Object... args) {
        super(message, args);

    }
}
