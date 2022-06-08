package com.swpu.rpc.core.balance;

import com.swpu.rpc.core.common.ServiceInfo;

import java.util.List;

/**
 * @Author lms
 * @Date 2022/6/1 8:45
 * @Description 负载均衡接口
 */
public interface LoadBalance {

    /**
     * 从多个服务中选出一个
     * @param serviceInfos
     * @return
     */
    ServiceInfo chooseOne(List<ServiceInfo> serviceInfos);
}
