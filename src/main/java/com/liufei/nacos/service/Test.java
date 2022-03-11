package com.liufei.nacos.service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 参考文档：https://www.jianshu.com/p/38b5452c9fec
 * https://www.jianshu.com/p/d2675fecbc5a
 */
public class Test {

    public static void main(String[] args) throws NacosException, IOException {
        String serverAddr = "localhost";
        String dataId = "appA";
        String group = "DEFAULT_GROUP";
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        ConfigService configService = NacosFactory.createConfigService(properties);
        String content = configService.getConfig(dataId, group, 5000);
        System.out.println("first receive:" + content);
        configService.addListener(dataId, group, new Listener() {
            @Override
            public Executor getExecutor() {
                System.out.println("-----------");
                return null;
            }

            @Override
            public void receiveConfigInfo(String config) {
                System.out.println("currentTime:" + new Date() + ", receive:" + config);
            }
        });
        // 这里我用了一个 System.in.read() 方法来监听输入的信息，主要是为了防止主线程退出，看不到后续的结果。
        int n = System.in.read();
    }
}
