package com.swpu.rpc.core;

import com.google.common.collect.Lists;
import com.swpu.rpc.core.balance.RandomLoadBalance;
import com.swpu.rpc.core.common.ServiceInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author lms
 * @Date 2022/6/2 14:26
 * @Description
 */
public class LoadBalanceTest {

    @Test
    public void test1() throws InterruptedException {
//        RoundRobinLoadBalance balance = new RoundRobinLoadBalance();
        RandomLoadBalance balance = new RandomLoadBalance();
//        ConsistentHashLoadBalance balance = new ConsistentHashLoadBalance();
        ServiceInfo serviceInfo1 = new ServiceInfo();
        serviceInfo1.setServiceName("service1");
        serviceInfo1.setWeight(1);
        ServiceInfo serviceInfo2 = new ServiceInfo();
        serviceInfo2.setServiceName("service2");
        serviceInfo2.setWeight(2);
        ServiceInfo serviceInfo3 = new ServiceInfo();
        serviceInfo3.setServiceName("service3");
        serviceInfo3.setWeight(2);
        List<ServiceInfo> list = Lists.newArrayList(serviceInfo1, serviceInfo2, serviceInfo3);
        Map<ServiceInfo, AtomicInteger> map = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    map.computeIfAbsent(balance.chooseOne(list), k -> new AtomicInteger())
                            .getAndIncrement();
                    if (finalI == 3 && j == 40000) { // 模拟程序运行时有个节点宕机了
                        list.remove(list.size() - 1);
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        map.forEach((k, v) -> System.out.println(k.getServiceName() + ": " + (double) v.get() / 100000));
    }
}
