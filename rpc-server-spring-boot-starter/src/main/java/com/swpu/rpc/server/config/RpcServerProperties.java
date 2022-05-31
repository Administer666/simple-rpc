package com.swpu.rpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author lms
 * @Date 2022/5/21 17:53
 * @Description
 */
@ConfigurationProperties(prefix = "rpc.server")
@Data
public class RpcServerProperties {
    /**
     * rpc服务的访问端口
     */
    private Integer port = 8080;

    /**
     * rpc服务名称
     */
    private String appName;

    /**
     * 注册中心地址
     */
    private String registryAddress = "192.168.56.2:2181";

}
