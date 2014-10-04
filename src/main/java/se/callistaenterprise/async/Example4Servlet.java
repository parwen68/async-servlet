package se.callistaenterprise.async;


import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@WebServlet(urlPatterns = "/example4", asyncSupported = true)
public class Example4Servlet extends HttpServlet {

    Logger log = LoggerFactory.getLogger(Example4Servlet.class);

    private ExecutorService executorService;

    private AsyncHttpClient httpClient;

    @Override
    public void init() throws ServletException {
        executorService = (ExecutorService) getServletContext().getAttribute("executorService");
        httpClient = (AsyncHttpClient) getServletContext().getAttribute("httpClient");
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        final AsyncContext asyncContext = request.startAsync();

        List<String> urls = Arrays.asList(
                "http://gp.se",
                "http://dn.se",
                "http://svd.se",
                "http://expressen.se",
                "http://aftonbladet.se"
        );


        executorService.submit(() -> {
            Map<String, String> result = Collections.synchronizedMap(new HashMap<>());
            CountDownLatch latch = new CountDownLatch(urls.size());
            try {
                urls.stream().parallel().forEach(url -> {
                    try {
                        synchronized (asyncContext.getResponse()) {
                            asyncContext.getResponse().getWriter().write("Calling " + url + "(" + Thread.currentThread() + ")<br>");
                        }
                        httpClient.prepareHead(url).execute(new AsyncCompletionHandler<Response>() {
                            @Override
                            public Response onCompleted(Response response) throws Exception {
                                result.put(url, response.getHeader("Server"));
                                latch.countDown();
                                return response;
                            }
                        });
                    } catch (IOException e) {
                        latch.countDown();
                        result.put(url, "Error");
                    }
                });
                latch.await();
                asyncContext.getRequest().setAttribute("result", result);
                asyncContext.getResponse().getWriter().write("In Async(" + Thread.currentThread() + ")<br>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            asyncContext.dispatch("/WEB-INF/jsp/example4.jsp");
        });

    }

}