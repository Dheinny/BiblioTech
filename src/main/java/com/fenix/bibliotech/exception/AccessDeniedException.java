package com.fenix.bibliotech.exception;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException (String message, Object... args) {
        super(message, args);
    }
}
