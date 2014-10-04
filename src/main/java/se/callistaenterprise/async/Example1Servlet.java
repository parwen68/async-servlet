package se.callistaenterprise.async;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@WebServlet(urlPatterns = "/example1", asyncSupported = true)
public class Example1Servlet extends HttpServlet {

    Logger log = LoggerFactory.getLogger(Example1Servlet.class);

    ExecutorService executorService;

    @Override
    public void init() throws ServletException {
        executorService = (ExecutorService) getServletContext().getAttribute("executorService");
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        final AsyncContext asyncContext = request.startAsync();

        executorService.submit(() -> {
            try {
                Thread.sleep(50L);
                log.debug("In Async");
                asyncContext.getResponse().getWriter().write("In Async(" + Thread.currentThread() + ")<br>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            asyncContext.dispatch("/WEB-INF/jsp/example1.jsp");
        });

    }
}