package se.callistaenterprise.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 *
 */
@WebServlet(urlPatterns = "/example2", asyncSupported = true)
public class Example2Servlet extends HttpServlet {

    Logger log = LoggerFactory.getLogger(Example2Servlet.class);

    private Timer timer;

    @Override
    public void init() throws ServletException {
        timer = new Timer(true);
    }

    @Override
    public void destroy() {
        timer.cancel();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext ac = req.startAsync();
        ac.setTimeout(-1);

        ac.addListener(new AsyncListener() {
            @Override
             public void onComplete(AsyncEvent event) throws IOException {
                log.debug("onComplete");
             }

             @Override
             public void onTimeout(AsyncEvent event) throws IOException {
                 log.debug("onTimeout");
             }

             @Override
             public void onError(AsyncEvent event) throws IOException {
                 log.debug("onError");
             }

             @Override
             public void onStartAsync(AsyncEvent event) throws IOException {
                 log.debug("onStartAsync");
             }
        });

        timer.schedule(new MyTimerTask(ac), 0, 100);
    }

    class MyTimerTask extends TimerTask {

        private final AsyncContext ac;

        public MyTimerTask(AsyncContext ac) {
            this.ac = ac;
        }

        @Override
        public void run() {
            try {
                log.debug("tick");
                ac.getResponse().getWriter().write("<script>parent.callback('" + UUID.randomUUID().toString() + "');</script>");
                ac.getResponse().flushBuffer();
            } catch (Exception e) {
                log.debug("Cancel timer! :{}", e.getClass());
                ac.complete();
                this.cancel();
            }
        }
    }
}
