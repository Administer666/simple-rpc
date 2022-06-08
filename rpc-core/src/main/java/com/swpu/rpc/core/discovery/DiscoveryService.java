package com.swpu.rpc.core.discovery;

import com.swpu.rpc.core.balance.LoadBalance;
import com.swpu.rpc.core.common.ServiceInfo;

/**
 * @Author lms
 * @Date 2022/5/21 16:40
 * @Description
 */
public interface DiscoveryService {

    ServiceInfo discovery(String serviceName, LoadBalance loadBalance) throws Exception;

    /**
     * 全局唯一id生成器，不应该写在这里，为了方便
     *
     * @return
     * @throws Exception
     */
    long getNextGeneralId() throws Exception;
}
