package com.seatomatic.common.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

/**
 * Standard JSON response envelope and serialization utility.
 */
public final class JsonResponse {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private JsonResponse() {
    }

    public static void send(HttpServletResponse response, Object payload) throws IOException {
        send(response, HttpServletResponse.SC_OK, payload);
    }

    public static void send(HttpServletResponse response, int statusCode, Object payload) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(payload));
        response.getWriter().flush();
    }

    public static Envelope success(Object data) {
        return new Envelope(true, data, null, System.currentTimeMillis());
    }

    public static Envelope success(Object data, String message) {
        return new Envelope(true, data, message, System.currentTimeMillis());
    }

    public static Envelope error(String message) {
        return new Envelope(false, null, message, System.currentTimeMillis());
    }

    public static Envelope of(boolean success, Object data, String message) {
        return new Envelope(success, data, message, System.currentTimeMillis());
    }

    public record Envelope(boolean success, Object data, String message, long timestamp) {
    }
}
