package com.liufei.nacos.nacosdemo.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.liufei.nacos.nacosdemo.config.HttpClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Service
public class ConfigClient3 {

    @Resource
    private HttpClient httpClient;

    @SneakyThrows
    public void longPolling(String url, String dataId) {
        String endpoint = url + "?dataId=" + dataId;
        HttpGet request = new HttpGet(endpoint);

        SSLContext sslcontext = httpClient.getSslcontext();
        PoolingHttpClientConnectionManager httpClientConnectionManager = httpClient.getHttpClientConnectionManager(sslcontext);
        HttpClientBuilder httpClientBuilder = httpClient.getHttpClientBuilder(httpClientConnectionManager);
        CloseableHttpClient closeableHttpClient = httpClient.getCloseableHttpClient(httpClientBuilder);
        CloseableHttpResponse response = closeableHttpClient.execute(request);
        switch (response.getStatusLine().getStatusCode()) {
            case 200: {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                response.close();
                String configInfo = result.toString();
                log.info("dataId: [{}] changed, receive configInfo: {}", dataId, configInfo);
                longPolling(url, dataId);
                break;
            }
            // ② 304 响应码标记配置未变更
            case 304: {
                log.info("longPolling dataId: [{}] once finished, configInfo is unchanged, longPolling again", dataId);
                longPolling(url, dataId);
                break;
            }
            default: {
                // throw new RuntimeException("unExcepted HTTP status code");
                longPolling(url, dataId);
                log.error("http error: " + response.getEntity().getContent().toString());
            }
        }

    }

    public static void main(String[] args) {
        // httpClient 会打印很多 debug 日志，关闭掉
        Logger logger = (Logger) LoggerFactory.getLogger("org.apache.http");
        logger.setLevel(Level.INFO);
        logger.setAdditive(false);

        ConfigClient3 configClient = new ConfigClient3();
        // ③ 对 dataId: user 进行配置监听 
        configClient.longPolling("http://127.0.0.1:8080/listener", "user");
    }
}