package com.seatomatic.common.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

public class AuditContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestId = UUID.randomUUID().toString();
        httpRequest.setAttribute("requestId", requestId);
        if (httpRequest.getSession(false) != null) {
            httpRequest.getSession(false).setAttribute("requestId", requestId);
        }
        chain.doFilter(request, response);
    }
}
