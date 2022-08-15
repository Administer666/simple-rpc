package com.swpu.rpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author lms
 * @Date 2022/8/12 17:41
 * @Description
 */
@ConfigurationProperties(prefix = "rpc.server")
@Data
public class RpcServerProperties {

    /**
     * rpc服务的访问端口
     */
    private Integer port = 9090;

    /**
     * 注册中心地址
     */
    private String registryAddress="192.168.56.2:2181";

}
