package se.callistaenterprise.async;


import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import rx.Observable;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@WebServlet(urlPatterns = "/example6", asyncSupported = true)
public class Example6Servlet extends HttpServlet {

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

        executorService.submit(() ->
            Observable
                 .from(urls)
                 .flatMap(url ->
                         observable(url).onErrorReturn((t) -> "Error:" + t.toString()))
                 .subscribe(
                         (v) -> write(asyncContext, v),
                         (e) -> write(asyncContext, e.toString()),
                         asyncContext::complete)
        );
    }

    private void write(AsyncContext asyncContext, String s) {
        try {
            asyncContext.getResponse().getWriter().write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Observable<String> observable(String url) {
        return Observable.create(subscriber -> {
            try {
                httpClient.prepareHead(url).execute(new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        subscriber.onNext(url + " >>> " + response.getHeader("Server") + "  (" +  Thread.currentThread() + ")<br>");
                        subscriber.onCompleted();
                        return response;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        subscriber.onError(t);
                    }
                });
            } catch (Exception e) {
                System.out.println("Err");
                subscriber.onError(e);
            }
        });
    }


}