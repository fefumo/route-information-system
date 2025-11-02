package se.ifmo.route_information_system;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ErrorTapFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ErrorTapFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        if ("/error".equals(r.getRequestURI()) &&
                r.getAttribute("jakarta.servlet.error.status_code") == null &&
                r.getAttribute("javax.servlet.error.status_code") == null) {
            log.warn("Direct /error hit. Referer={}, Query={}", r.getHeader("Referer"), r.getQueryString());
        }
        chain.doFilter(req, res);
    }
}
