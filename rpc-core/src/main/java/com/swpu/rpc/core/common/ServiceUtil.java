package com.swpu.rpc.core.common;

/**
 * @Author lms
 * @Date 2022/6/8 12:48
 * @Description
 */
public class ServiceUtil {

    public static String serviceKey(String serviceName, String version) {
        return String.join("-", serviceName, version);
    }
}
