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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@WebServlet(urlPatterns = "/example5", asyncSupported = true)
public class Example5Servlet extends HttpServlet {

    Logger log = LoggerFactory.getLogger(Example5Servlet.class);

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
            CountDownLatch latch = new CountDownLatch(urls.size());
            try {
                urls.stream().parallel().forEach(url -> {
                    try {
                        httpClient.prepareHead(url).execute(new AsyncCompletionHandler<Response>() {
                            @Override
                            public Response onCompleted(Response response) throws Exception {
                                String result = response.getHeader("Server");
                                synchronized (asyncContext.getResponse()) {
                                    asyncContext.getResponse().getWriter().write(url + " >>> " + result + "(" + Thread.currentThread() + ")<br>");
                                }
                                latch.countDown();
                                return response;
                            }
                        });
                    } catch (IOException e) {
                        latch.countDown();
                        synchronized (asyncContext.getResponse()) {
                            try {
                                asyncContext.getResponse().getWriter().write(url + " >>> Error (" + Thread.currentThread() + ")<br>");
                            } catch (IOException e1) {
                                // ???
                            }
                        }
                    }
                });
                asyncContext.getResponse().getWriter().write("In Async(" + Thread.currentThread() + ")<br>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asyncContext.complete();
        });

    }

}