//package com.liufei.nacos.nacosdemo.utils;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.parser.Feature;
//import com.bigcustomer.configs.BaseConfig;
//import com.bigcustomer.configs.KeyConfig;
//import com.bigcustomer.utils.ExternalHelpUtile;
//import com.bigcustomer.utils.SinUtil;
//import huashitech.kissucomponent.redis.RedisUtil;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.util.EntityUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//
///**
// * @author :CX
// * @Date :Create in 2018/8/17 10:02
// * @Effect :
// */
//@Component
//public class HttpsUtils {
//    private static Logger logger = LoggerFactory.getLogger(HttpsUtils.class);
//    static CloseableHttpClient httpClient;
//    static RequestConfig requestConfig;
//    static CloseableHttpResponse httpResponse;
//
//    private static BaseConfig baseConfig;
//    private static ExternalHelpUtile utile;
//    private static KeyConfig config;
//    private static SinUtil sinUtil;
//    private static RedisUtil redisUtil;
//    private static String encoding = "utf-8";
//
//    @Autowired
//    public void init(KeyConfig config, BaseConfig baseConfig, RedisUtil redisUtil,
//                     ExternalHelpUtile utile, SinUtil sinUtil,
//                     CloseableHttpClient httpClient
//                   , RequestConfig requestConfig) {
//        HttpsUtils.config = config;
//        HttpsUtils.baseConfig = baseConfig;
//        HttpsUtils.redisUtil = redisUtil;
//        HttpsUtils.utile = utile;
//        HttpsUtils.sinUtil = sinUtil;
//        HttpsUtils.httpClient = httpClient;
//        HttpsUtils.requestConfig = requestConfig;
//
//    }
//
//   //https 封装方法
//    private static Map<String , Object> baseSendHttpsPost(Map<String, Object> par, String url, String key) throws ClientProtocolException, IOException {
//        //请求map
//        LinkedHashMap<String, Object> requestMap = new LinkedHashMap<>();
//        requestMap.put("mac", null);// 签名会去掉mac
//        requestMap.put("agentcode", config.getAgentcode());
//        requestMap.put("msgbody", par);
//
//        // 签名并对mac 重新赋值
//        String mac = sinUtil.createMac(requestMap, key);
//        requestMap.put("mac", mac);
//        String parStr = JSON.toJSONString(requestMap);
//        logger.info("参数 ：" + parStr);
//        try {
//
//            //创建post方式请求对象
//            HttpPost httpPost = new HttpPost(url);
//
//            //装填参数
//            StringEntity stringEntity = null;
//            if (null != par) {
//                stringEntity = new StringEntity(parStr,encoding);
//                httpPost.setEntity(stringEntity);
//            }
//
//            logger.info("创建请求httpsPost-URL={},params={}", url, parStr);
//            //设置header信息
//            //指定报文头【Content-type】、【User-Agent】
//            httpPost.setHeader("Content-Type", "application/json;charset="+encoding);
////            httpPost.setHeader("Content-Length", params.length() + "");
//
//            //执行请求操作，并拿到结果（同步阻塞）
//            CloseableHttpResponse response = httpClient.execute(httpPost);
//            //获取结果实体
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                //按指定编码转换结果实体为String类型
//                String body = EntityUtils.toString(entity, encoding);
//                logger.info(url + "接口返回报文是：/n" + body);
//                return JSON.parseObject(body, LinkedHashMap.class , Feature.OrderedField);
//            }
//            EntityUtils.consume(entity);
//            if(response != null ){
//                //释放链接
//                response.close();
//            }
//            return null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            try {
//                if (null != httpResponse) {
//                    httpResponse.close();
//                }
//                logger.info("请求流关闭完成");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//}