package com.seatomatic.common.filter;

import com.seatomatic.common.security.CsrfTokenManager;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            HttpSession session = httpRequest.getSession(false);
            if (session != null && !httpRequest.getRequestURI().contains("/login")) {
                String submittedToken = httpRequest.getParameter("_csrf");
                if (submittedToken == null || !CsrfTokenManager.isValid(session, submittedToken)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
