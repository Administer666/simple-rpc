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
            Set<String> keys = properties.stringPropertyNames();
            for (String key : keys) {
                if (key.endsWith("Service")) {
                    Class<?> serviceClass = Class.forName(key);
                    Class<?> serviceImplClass = Class.forName(properties.getProperty(key));
                    beanMap.put(serviceClass, serviceImplClass.newInstance());
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <T> T getService(Class<T> serviceClass) {
        return (T) beanMap.get(serviceClass);
    }
}
