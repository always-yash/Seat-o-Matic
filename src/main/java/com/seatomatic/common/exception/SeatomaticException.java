package com.seatomatic.common.exception;

public class SeatomaticException extends RuntimeException {

    public SeatomaticException(String message) {
        super(message);
    }

    public SeatomaticException(String message, Throwable cause) {
        super(message, cause);
    }
}
