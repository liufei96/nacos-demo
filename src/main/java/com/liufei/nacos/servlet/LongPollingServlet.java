package com.liufei.nacos.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet(urlPatterns = "/long-polling")
public class LongPollingServlet extends HttpServlet {

    private final Random random = new Random();

    private final AtomicLong sequence = new AtomicLong();

    private final AtomicLong value = new AtomicLong();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println();
        final long currentSequence = sequence.incrementAndGet();
        System.out.println("第" + (currentSequence) + "次 longpolling");

        //由于客户端设置的超时时间是50s，
        //为了更好的展示长轮询，这边random 100，模拟服务端hold住大于50和小于50的情况。
        //再具体场景中，这块在具体实现上，
        //对于同步servlet，首先这里必须阻塞，因为一旦doGet方法走完，容器就认为可以结束这次请求，返回结果给客户端。
        //所以一般实现如下：
        // while(结束){ //结束条件，超时或者拿到数据
        //    data = fetchData();
        //    if(data == null){
        //       sleep();
        //    }
        // }

        int sleepSecends = random.nextInt(100);

        System.out.println(currentSequence + " wait " + sleepSecends + " second");

        try {
            TimeUnit.SECONDS.sleep(sleepSecends);
        } catch (InterruptedException e) {

        }

        PrintWriter out = resp.getWriter();
        long result = value.getAndIncrement();
        out.write(Long.toString(result));
        out.flush();
    }

    public static void main(String[] args) {
        new LongPollingServlet();
    }
}
