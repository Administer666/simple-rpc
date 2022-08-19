package com.swpu.rpc.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author lms
 * @Date 2022/8/12 19:22
 * @Description
 */
@ConfigurationProperties(prefix = "rpc.client")
@Data
public class RpcClientProperties {

    /**
     * 序列化方式
     */
    private String serialization = "protostuff";

    /**
     * 注册中心地址
     */
    private String discoveryAddress = "192.168.56.2:2181";


    /**
     * 超时时间，单位 ms
     */
    private Long timeout = 5000L;
}
