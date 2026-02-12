package com.fenix.bibliotech.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final Object[] args;
    public BusinessException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}
