package com.seatomatic.common.web;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JsonResponse {

    private static final Gson GSON = new Gson();

    private JsonResponse() {
    }

    public static void send(HttpServletResponse response, Object payload) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(payload));
    }

    public static Object success(Object data) {
        return new ResponseEnvelope(true, data, null);
    }

    public static Object error(String message) {
        return new ResponseEnvelope(false, null, message);
    }

    public static Object of(boolean success, Object data, String message) {
        return new ResponseEnvelope(success, data, message);
    }

    private record ResponseEnvelope(boolean success, Object data, String message) {
    }
}
