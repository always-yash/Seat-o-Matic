package com.seatomatic.common.web;

import javax.servlet.http.HttpSession;

/**
 * Flash notification utility for single-request message delivery across redirects.
 */
public class Flash {

    public static final String SESSION_KEY = "flashMessage";

    public enum Type {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    private final Type type;
    private final String message;

    public Flash(Type type, String message) {
        this.type = type != null ? type : Type.INFO;
        this.message = message != null ? message : "";
    }

    public static Flash success(String message) {
        return new Flash(Type.SUCCESS, message);
    }

    public static Flash error(String message) {
        return new Flash(Type.ERROR, message);
    }

    public static Flash info(String message) {
        return new Flash(Type.INFO, message);
    }

    public static Flash warning(String message) {
        return new Flash(Type.WARNING, message);
    }

    /**
     * Stores the flash message in the active session.
     */
    public static void set(HttpSession session, Flash flash) {
        if (session != null) {
            session.setAttribute(SESSION_KEY, flash);
        }
    }

    /**
     * Consumes (reads and clears) the flash message from the session.
     */
    public static Flash consume(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object attr = session.getAttribute(SESSION_KEY);
        if (attr instanceof Flash flash) {
            session.removeAttribute(SESSION_KEY);
            return flash;
        }
        return null;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
