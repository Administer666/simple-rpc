package com.swpu.rpc.core.balance;

import com.swpu.rpc.core.common.ServiceInfo;

import java.util.List;

/**
 * @Author lms
 * @Date 2022/8/15 8:45
 * @Description 负载均衡接口
 */
public interface LoadBalance {

    ServiceInfo chooseOne(List<ServiceInfo> serviceInfos);
}
