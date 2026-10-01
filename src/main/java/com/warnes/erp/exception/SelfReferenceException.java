package com.warnes.erp.exception;

public class SelfReferenceException extends RuntimeException {
    public SelfReferenceException(String message) {
        super(message);
    }
}
