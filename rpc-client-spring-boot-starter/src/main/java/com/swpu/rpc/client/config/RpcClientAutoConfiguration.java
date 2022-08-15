package com.swpu.rpc.client.config;


import com.swpu.rpc.client.annotation.RpcAutowiredBeanPostProcessor;
import com.swpu.rpc.core.discovery.DiscoveryService;
import com.swpu.rpc.core.discovery.impl.ZookeeperDiscoveryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lms
 * @Date 2022/5/28 16:21
 * @Description
 */

@Configuration
@EnableConfigurationProperties(RpcClientProperties.class)
public class RpcClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DiscoveryService discoveryService(RpcClientProperties properties) {
        return new ZookeeperDiscoveryService(properties.getDiscoveryAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcAutowiredBeanPostProcessor rpcAutowiredBeanPostProcessor(DiscoveryService discoveryService,
                                                                       RpcClientProperties properties) {
        return new RpcAutowiredBeanPostProcessor(discoveryService, properties);
    }

}
