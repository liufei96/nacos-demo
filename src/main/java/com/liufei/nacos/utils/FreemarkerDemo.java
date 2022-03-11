package com.liufei.nacos.utils;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerDemo {

    private static final ClassPathResource classPathResource = new ClassPathResource("templates");
    private static final String CLASS_PATH = "src/main/java/com/liufei/nacos/templates";


    public static void main(String[] args) {
        // step1 创建freeMarker配置实例
        Configuration configuration = new Configuration();
        Writer out = null;
        try {
            // step2 获取模版路径
            configuration.setDirectoryForTemplateLoading(classPathResource.getFile());
            // step3 创建数据模型
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("classPath", "com.liufei.nacos.templates");
            dataMap.put("className", "User");
            dataMap.put("Id", "Id");
            dataMap.put("userName", "userName");
            dataMap.put("password", "password");
            // step4 加载模版文件
            Template template = configuration.getTemplate("classTemplate.ftl");
            // step5 生成数据
            File docFile = new File(CLASS_PATH + "\\" + "User.java");
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
            // step6 输出文件
            template.process(dataMap, out);
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^User.java 文件创建成功 !");

            Object user = createUser("com.liufei.nacos.templates.User");
            System.out.println(JSONObject.toJSONString(user));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out) {
                    out.flush();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }


    public static Object createUser(String classPath) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(classPath);
            Object obj = aClass.newInstance();
            Method setName = aClass.getMethod("setUserName", String.class);
            setName.invoke(obj, "liufei");
            return obj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return new Object();
    }
}
