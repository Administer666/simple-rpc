package com.swpu.rpc.core.register.impl;

import com.swpu.rpc.core.common.ServiceInfo;
import com.swpu.rpc.core.register.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;

/**
 * @Author lms
 * @Date 2022/8/12 9:55
 * @Description
 */
@Slf4j
public class ZookeeperRegisterService implements RegisterService {

    private CuratorFramework client;
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;

    public ZookeeperRegisterService(String zkAddress) {
        try {
            // 最多尝试连接3次，每次间隔1s
            client = CuratorFrameworkFactory
                    .builder()
                    .connectString(zkAddress)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .namespace("simple_rpc")
                    .build();
            client.start();
            serviceDiscovery = ServiceDiscoveryBuilder
                    .builder(ServiceInfo.class)
                    .client(client)
                    .basePath("/service_list")
                    .serializer(new JsonInstanceSerializer<>(ServiceInfo.class))
                    .build();
            serviceDiscovery.start();
        } catch (Exception e) {
            log.error("注册中心启动失败 :", e);
        }
    }


    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();
        this.serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();
        this.serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public void destroy() throws IOException {
        this.serviceDiscovery.close();
    }
}
