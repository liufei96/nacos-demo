package com.liufei.nacos.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet(urlPatterns = "/long-polling-async", asyncSupported = true)
public class LongPollingAsyncServlet extends HttpServlet {

    private Random random = new Random();

    private final AtomicLong sequence = new AtomicLong();

    private final AtomicLong value = new AtomicLong();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 200, 50000L,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println();
        final long currentSequence = sequence.incrementAndGet();
        System.out.println("第" + (currentSequence) + "次 longpolling async");
        //设置request异步处理
        AsyncContext asyncContext = request.startAsync();

        //异步处理超时时间，这里需要注意，jetty容器默认的这个值设置的是30s，
        //如果超时，异步处理没有完成（通过是否asyncContext.complete()来进行判断），将会重试（会再次调用doGet方法）。
        //这里由于客户端long polling设置的是50s，所以这里如果小于50，会导致重试。
        asyncContext.setTimeout(51000);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {

            }

            //超时处理，注意asyncContext.complete();，表示请求处理完成
            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                AsyncContext asyncContext = event.getAsyncContext();
                asyncContext.complete();
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {

            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {

            }
        });

        //提交线程池异步写会结果
        //具体场景中可以有具体的策略进行操作
        executor.submit(new HandlePollingTask(currentSequence, asyncContext));

    }

    class HandlePollingTask implements Runnable {

        private AsyncContext asyncContext;

        private long sequense;

        public HandlePollingTask(long sequense, AsyncContext asyncContext) {
            this.sequense = sequense;
            this.asyncContext = asyncContext;
        }

        @Override
        public void run() {
            try {
                //通过asyncContext拿到response
                PrintWriter out = asyncContext.getResponse().getWriter();
                int sleepSecends = random.nextInt(100);


                System.out.println(sequense + " wait " + sleepSecends + " second");

                try {
                    TimeUnit.SECONDS.sleep(sleepSecends);
                } catch (InterruptedException e) {

                }

                long result = value.getAndIncrement();

                out.write(Long.toString(result));

            } catch (Exception e) {
                System.out.println(sequense + "handle polling failed");
            } finally {
                //数据写回客户端
                asyncContext.complete();
            }
        }
    }
}
