package se.callistaenterprise.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 *
 */
@WebFilter(urlPatterns = {"/example1","/example3"}, asyncSupported = true)
public class Example1Filter implements Filter {

    private static Logger log = LoggerFactory.getLogger(Example1Filter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("doFilter: Start");
        response.getWriter().write("Filter Before("+ Thread.currentThread() +")<br>");

        chain.doFilter(request, response);
        if(request.isAsyncStarted()) {
            MyAsyncListener l = request.getAsyncContext().createListener(MyAsyncListener.class);
            request.getAsyncContext().addListener(l);
        }
        response.getWriter().write("Filter After("+ Thread.currentThread()+")<br>");
        log.debug("doFilter: End");
    }

    @Override
    public void destroy() {

    }

    public static class MyAsyncListener implements AsyncListener {

        private static Logger log = LoggerFactory.getLogger(MyAsyncListener.class);

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            log.debug("Filter onComplete");
            event.getSuppliedResponse().getWriter().write("AsyncListener OnComplete("+Thread.currentThread()+")\n");
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("onTimeout\n");
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("onError\n");
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("onStartAsync\n");
        }
    }

}
