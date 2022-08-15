package com.swpu.rpc.core.register;

import com.swpu.rpc.core.common.ServiceInfo;

import java.io.IOException;

/**
 * @Author lms
 * @Date 2022/8/12 9:53
 * @Description 注册中心可选择zk、nacos、eureka
 */
public interface RegisterService {

    void register(ServiceInfo serviceInfo) throws Exception;

    void unRegister(ServiceInfo serviceInfo) throws Exception;

    void destroy() throws IOException;
}
