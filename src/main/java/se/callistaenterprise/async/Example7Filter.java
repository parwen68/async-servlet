package se.callistaenterprise.async;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@WebFilter(urlPatterns = {"/example7"}, asyncSupported = true, dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ASYNC})
public class Example7Filter implements Filter {

    ExecutorService executorService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        executorService = (ExecutorService) filterConfig.getServletContext().getAttribute("executorService");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.getWriter().write("Filter::doFilter: >>> Start<br>");

        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            response.getWriter().write("Filter::doFilter: REQUEST: Start >>> Start("+ Thread.currentThread() +")<br>");
            AsyncContext ac = request.startAsync();
            ac.addListener(new MyAsyncListener());

            executorService.submit(() -> {
                try {
                    ac.getResponse().getWriter().write("Filter::doFilter: ASYNC Start >>> ("+ Thread.currentThread() +")<br>");
                    ac.getRequest().setAttribute("attr", "Hello World!");
                    Thread.sleep(2000L);
                    ac.getResponse().getWriter().write("Filter::doFilter: ASYNC End <<< (" + Thread.currentThread() + ")<br>");
                    ac.dispatch();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            response.getWriter().write("Filter::doFilter: REQUEST: End <<< (" + Thread.currentThread() + ")<br>");

        } else if (request.getDispatcherType() == DispatcherType.ASYNC) {
            response.getWriter().write("Filter::chain.doFilter: Before >>> ("+ Thread.currentThread() +")<br>");
            chain.doFilter(request, response);
            response.getWriter().write("Filter::chain.doFilter: After <<< (" + Thread.currentThread() + ")<br>");
            if(request.isAsyncStarted()) {
                MyAsyncListener l = request.getAsyncContext().createListener(MyAsyncListener.class);
                request.getAsyncContext().addListener(l);
            }
        }
        response.getWriter().write("Filter::doFilter: <<< End <br>");
    }

    @Override
    public void destroy() {

    }

    public static class MyAsyncListener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("AsyncListener::onComplete("+Thread.currentThread()+")<br>");
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
            event.getSuppliedResponse().getWriter().write("AsyncListener::onStartAsync("+Thread.currentThread()+")<br>");
        }
    }

}
