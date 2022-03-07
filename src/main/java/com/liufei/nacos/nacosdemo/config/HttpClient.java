package com.liufei.nacos.nacosdemo.config;

import lombok.Data;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

/**
 * @author :CX
 * @Date :Create in 2018/8/17 9:57
 * @Effect :
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "httpclient")
public class HttpClient {

    private Integer maxTotal;

    private Integer defaultMaxPerRoute;

    private Integer connectTimeout;

    private Integer connectionRequestTimeout;

    private Integer socketTimeout;

    private boolean staleConnectionCheckEnabled;

    //https 证书 路径
    private String keyStorePath;
    // 证书密码
    private String keyStorepass;

    @Bean(name = "sslcontext")
    public SSLContext getSslcontext() {
        SSLContext sc = null;
        FileInputStream instream = null;
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance("JKS");
            instream = new FileInputStream(ResourceUtils.getFile(keyStorePath));
            trustStore.load(instream, keyStorepass.toCharArray());
            // 相信自己的CA和所有自签名的证书
            sc = SSLContexts.custom().loadKeyMaterial(trustStore, keyStorepass.toCharArray())
                    .setProtocol("TLSv1.2")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                instream.close();
            } catch (IOException e) {
            }
        }
        return sc;
    }

    /**
     * 首先实例化一个连接池管理器，设置最大连接数、并发连接数
     *
     * @return
     */
    @Bean(name = "httpClientConnectionManager")
    public PoolingHttpClientConnectionManager getHttpClientConnectionManager(@Qualifier("sslcontext") SSLContext sslcontext) {
        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager httpClientConnectionManager =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        //最大连接数
        httpClientConnectionManager.setMaxTotal(maxTotal);
        //并发数
        httpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return httpClientConnectionManager;
    }

    /**
     * 实例化连接池，设置连接池管理器。
     * 这里需要以参数形式注入上面实例化的连接池管理器
     *
     * @param httpClientConnectionManager
     * @return
     */
    @Bean(name = "httpClientBuilder")
    public HttpClientBuilder getHttpClientBuilder(@Qualifier("httpClientConnectionManager") PoolingHttpClientConnectionManager httpClientConnectionManager) {
//HttpClientBuilder中的构造方法被protected修饰，所以这里不能直接使用new来实例化一个HttpClientBuilder，可以使用HttpClientBuilder提供的静态方法create()来获取HttpClientBuilder对象
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        httpClientBuilder.setConnectionManager(httpClientConnectionManager);

        return httpClientBuilder;
    }

    /**
     * 注入连接池，用于获取httpClient
     *
     * @param httpClientBuilder
     * @return
     */
    @Bean(name = "httpclient")
    public CloseableHttpClient getCloseableHttpClient(@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder) {
        return httpClientBuilder.build();
    }

    /**
     * Builder是RequestConfig的一个内部类
     * 通过RequestConfig的custom方法来获取到一个Builder对象
     * 设置builder的连接信息
     * 这里还可以设置proxy，cookieSpec等属性。有需要的话可以在此设置
     *
     * @return
     */
    @Bean(name = "builder")
    public RequestConfig.Builder getBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom();
        return builder.setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout)
                .setStaleConnectionCheckEnabled(staleConnectionCheckEnabled);
    }

    /**
     * 使用builder构建一个RequestConfig对象
     *
     * @param builder
     * @return
     */
    @Bean(name = "requestConfig")
    public RequestConfig getRequestConfig(@Qualifier("builder") RequestConfig.Builder builder) {
        return builder.build();
    }
}