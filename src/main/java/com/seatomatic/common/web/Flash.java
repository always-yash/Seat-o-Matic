package com.seatomatic.common.web;

public class Flash {

    public enum Type {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    private final Type type;
    private final String message;

    public Flash(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
