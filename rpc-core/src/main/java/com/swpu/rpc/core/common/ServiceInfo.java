package com.swpu.rpc.core.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author lms
 * @Date 2022/5/21 16:41
 * @Description
 */
@Data
@Accessors(chain = true)
public class ServiceInfo {

    /**
     * 应用名
     */
    private String appName;

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
}
