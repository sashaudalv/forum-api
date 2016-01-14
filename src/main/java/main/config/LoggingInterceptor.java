package main.config;

import com.google.gson.Gson;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * alex on 10.01.16.
 */
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (httpServletRequest.getMethod().equalsIgnoreCase("GET")) {
            System.out.println("Got request: url " + httpServletRequest.getRequestURI());
            System.out.println("get params:  " + new Gson().toJson(httpServletRequest.getParameterMap()));
        }
        return true;
    }
}
