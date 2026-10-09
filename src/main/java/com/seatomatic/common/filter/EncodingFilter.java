package com.seatomatic.common.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(filterName = "encodingFilter", urlPatterns = "/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (response instanceof javax.servlet.http.HttpServletResponse httpResponse) {
            httpResponse.setHeader("Content-Type", "text/html;charset=UTF-8");
        }
        chain.doFilter(request, response);
    }
}
