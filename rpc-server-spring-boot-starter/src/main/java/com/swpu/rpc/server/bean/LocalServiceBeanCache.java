package com.swpu.rpc.server.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lms
 * @Date 2022/5/28 12:25
 * @Description 将服务实现类的bean缓存到本地，避免每次都通过反射从spring容器中寻找
 */
public class LocalServiceBeanCache {

    private static final Map<String, Object> serviceCacheMap = new ConcurrentHashMap<>();

    public static void put(String serviceName, Object bean) {
        serviceCacheMap.put(serviceName, bean);
    }

    public static Object get(String serviceName) {
        return serviceCacheMap.get(serviceName);
    }

    public static void remove(String serviceName) {
        serviceCacheMap.remove(serviceName);
    }
}
