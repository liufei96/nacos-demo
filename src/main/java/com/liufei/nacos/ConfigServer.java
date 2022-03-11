package com.liufei.nacos;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.liufei.nacos.service.ConfigClient3;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@SpringBootApplication
public class ConfigServer implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Data
    private static class AsyncTask {
        // 长轮询请求的上下文，包含请求和响应体
        private AsyncContext asyncContext;
        // 超时标记
        private boolean timeout;

        public AsyncTask(AsyncContext asyncContext, boolean timeout) {
            this.asyncContext = asyncContext;
            this.timeout = timeout;
        }
    }

    // guava 提供的多值 Map，一个 key 可以对应多个 value
    private Multimap<String, AsyncTask> dataIdContext = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("longPolling-timeout-checker-%d")
            .build();
    private ScheduledExecutorService timeoutChecker = new ScheduledThreadPoolExecutor(1, threadFactory);

    // 配置监听接入点
    @RequestMapping("/listener")
    public void addListener(HttpServletRequest request, HttpServletResponse response) {

        String dataId = request.getParameter("dataId");

        // 开启异步
        AsyncContext asyncContext = request.startAsync(request, response);
        AsyncTask asyncTask = new AsyncTask(asyncContext, true);

        // 维护 dataId 和异步请求上下文的关联
        dataIdContext.put(dataId, asyncTask);

        // 启动定时器，30s 后写入 304 响应
        timeoutChecker.schedule(() -> {
            if (asyncTask.isTimeout()) {
                dataIdContext.remove(dataId, asyncTask);
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                asyncContext.complete();
            }
        }, 30000, TimeUnit.MILLISECONDS);
    }

    // 配置发布接入点
    @RequestMapping("/publishConfig")
    @SneakyThrows
    public String publishConfig(String dataId, String configInfo) {
        log.info("publish configInfo dataId: [{}], configInfo: {}", dataId, configInfo);
        Collection<AsyncTask> asyncTasks = dataIdContext.removeAll(dataId);
        for (AsyncTask asyncTask : asyncTasks) {
            asyncTask.setTimeout(false);
            HttpServletResponse response = (HttpServletResponse) asyncTask.getAsyncContext().getResponse();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(configInfo);
            asyncTask.getAsyncContext().complete();
        }
        return "success";
    }

    // 配置发布接入点
    @GetMapping("/ping")
    @SneakyThrows
    public String ping(HttpServletRequest request) {
        log.info("http version: " + ((HttpServletRequestImpl) request).getExchange().getProtocol().toString());
        return "ping";
    }


    @Resource
    private ConfigClient3 configClient;


    // 配置发布接入点
    @GetMapping("/client")
    public void openClient(HttpServletRequest request) {
        // httpClient 会打印很多 debug 日志，关闭掉
        try {
            Logger logger = (Logger) LoggerFactory.getLogger("org.apache.http");
            logger.setLevel(Level.INFO);
            logger.setAdditive(false);

            // ③ 对 dataId: user 进行配置监听
            configClient.longPolling("https://127.0.0.1:8443/listener", "user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }


    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        factory.addBuilderCustomizers(builder -> {
            builder.addHttpListener(8081, "0.0.0.0");
        });
    }
}