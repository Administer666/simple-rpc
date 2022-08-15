package com.swpu.rpc.core.discovery.impl;

import com.swpu.rpc.core.common.ServiceInfo;
import com.swpu.rpc.core.discovery.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * @Author lms
 * @Date 2022/5/21 17:10
 * @Description
 */
@Slf4j
public class ZookeeperDiscoveryService implements DiscoveryService {

    private CuratorFramework client;
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;

    public ZookeeperDiscoveryService(String zkAddress) {
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
    public ServiceInfo discovery(String serviceName) throws Exception {
        Collection<ServiceInstance<ServiceInfo>> serviceInstances = this.serviceDiscovery.queryForInstances(serviceName);
        return CollectionUtils.isEmpty(serviceInstances) ? null :
                serviceInstances.stream().findAny().get().getPayload();
    }

    @Override
    public int getNextGlobalId() throws Exception {
        String path = "/guid/temp";
        String s = client.create()
                .withTtl(200L)
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL_WITH_TTL)
                .forPath(path);
        return Integer.parseInt(s.substring(path.length()));
    }
}