package com.swpu.rpc.core.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author lms
 * @Date 2022/8/12 9:54
 * @Description
 */
@Data
@Accessors(chain = true)
public class ServiceInfo {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 地址
     */
    private String address;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 负载均衡权重
     */
    private Integer weight;
}
