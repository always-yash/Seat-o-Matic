package com.seatomatic.common.exception;

public final class ErrorMapper {

    private ErrorMapper() {
    }

    public static int mapToStatus(Throwable throwable) {
        if (throwable instanceof SecurityException) {
            return 403;
        }
        if (throwable instanceof ValidationException) {
            return 400;
        }
        return 500;
    }
}
