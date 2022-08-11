package com.swpu.rpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lms
 * @Date 2022/8/11 10:37
 * @Description
 */
public class ServicesFactory {

    static Properties properties = new Properties();
    static Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    static {
        try (InputStream in = ServicesFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(in);
            Set<String> serviceNames = properties.stringPropertyNames();
            for (String serviceName : serviceNames) {
                Class<?> serviceClass = Class.forName(serviceName);
                Class<?> serviceImplClass = Class.forName(properties.getProperty(serviceName));
                beanMap.put(serviceClass, serviceImplClass.newInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <T> T getService(Class<T> serviceClass) {
        return (T) beanMap.get(serviceClass);
    }

    public static void main(String[] args) {
        for (Map.Entry<Class<?>, Object> entry : beanMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
