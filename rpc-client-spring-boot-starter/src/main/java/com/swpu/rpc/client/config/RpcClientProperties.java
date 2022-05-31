package com.swpu.rpc.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author lms
 * @Date 2022/5/28 16:21
 * @Description
 */
@Data
@ConfigurationProperties(prefix = "rpc.client")
public class RpcClientProperties {
    /**
     * 序列化方式
     */
    private String serialization;

    /**
     * 注册中心地址
     */
    private String discoveryAddress = "192.168.56.2:2181";
//
//    /**
//     * 请求超时时间
//     */
//    private Integer timeout;
}
