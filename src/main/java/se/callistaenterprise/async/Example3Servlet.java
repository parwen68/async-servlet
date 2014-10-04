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

/**
 *
 */
@WebServlet(urlPatterns = "/example3", asyncSupported = true)
public class Example3Servlet extends HttpServlet {

    Logger log = LoggerFactory.getLogger(Example3Servlet.class);


    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        final AsyncContext asyncContext = request.startAsync();

        asyncContext.start(() -> {
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