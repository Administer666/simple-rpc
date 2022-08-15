package com.swpu.rpc.core.discovery;

import com.swpu.rpc.core.common.ServiceInfo;

/**
 * @Author lms
 * @Date 2022/5/21 16:40
 * @Description
 */
public interface DiscoveryService {

    ServiceInfo discovery(String serviceName) throws Exception;

    /**
     * 生成全局唯一id，用作消息序列号，不应该写在这里，为了方便
     *
     * @return 全局唯一id
     * @throws Exception
     */
    int getNextGlobalId() throws Exception;
}
