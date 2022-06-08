package com.swpu.rpc.core.balance;

import com.swpu.rpc.core.common.ServiceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lms
 * @Date 2022/6/1 8:51
 * @Description 按权重轮询
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private int count = 0;

    @Override
    public synchronized ServiceInfo chooseOne(List<ServiceInfo> serviceInfos) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < serviceInfos.size(); i++) {
            for (int j = 0; j < serviceInfos.get(i).getWeight(); j++) {
                list.add(i);
            }
        }
        int index = count % list.size();
        count++;
        return serviceInfos.get(list.get(index));
    }
    /*private int count = 0;
    private ServiceInfo lastServiceInfo = new ServiceInfo();

    @Override
    public synchronized ServiceInfo chooseOne(List<ServiceInfo> serviceInfos) {
        if (serviceInfos.size() <= 0) return null;

        int index = serviceInfos.indexOf(lastServiceInfo);

        if (index < 0) {
            index = 0;
            count = 0;
            lastServiceInfo = serviceInfos.get(0);
        }
        if (++count >= lastServiceInfo.getWeight()) {
            count = 0;
            index = (index + 1) % serviceInfos.size();
            lastServiceInfo = serviceInfos.get(index);
        }

        return lastServiceInfo;
    }*/
}
