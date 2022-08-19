package com.swpu.rpc.core.balance;

import com.swpu.rpc.core.common.ServiceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author lms
 * @Date 2022/8/15 8:51
 * @Description 按权重轮询
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private AtomicInteger count = new AtomicInteger();

    @Override
    public ServiceInfo chooseOne(List<ServiceInfo> serviceInfos) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < serviceInfos.size(); i++) {
            for (int j = 0; j < serviceInfos.get(i).getWeight(); j++) {
                list.add(i);
            }
        }
        int index = count.get() % list.size();
        count.incrementAndGet();
        return serviceInfos.get(list.get(index));
    }
}
