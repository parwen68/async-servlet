package se.callistaenterprise.async;


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
@WebServlet(urlPatterns = "/example7", asyncSupported = true)
public class Example7Servlet extends HttpServlet {

    ExecutorService executorService;

    @Override
    public void init() throws ServletException {
        executorService = (ExecutorService) getServletContext().getAttribute("executorService");
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        response.getWriter().write("Servlet::doGet: >>> Start (" + Thread.currentThread() + ")<br>");

        final AsyncContext asyncContext = request.startAsync();

        executorService.submit(() -> {
            try {
                Thread.sleep(50L);
                asyncContext.getResponse().getWriter().write("Servlet::In Async(" + Thread.currentThread() + ")<br>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            asyncContext.dispatch("/WEB-INF/jsp/example7.jsp");
        });

        response.getWriter().write("Servlet::doGet: >>> End (" + Thread.currentThread() + ")<br>");
    }
}