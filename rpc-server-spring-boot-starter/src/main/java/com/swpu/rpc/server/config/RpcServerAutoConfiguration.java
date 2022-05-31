package com.swpu.rpc.server.config;

import com.swpu.rpc.core.register.RegisterService;
import com.swpu.rpc.core.register.ZookeeperRegisterService;
import com.swpu.rpc.server.annotation.RpcServiceBeanPostProcessor;
import com.swpu.rpc.server.core.RpcServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lms
 * @Date 2022/5/26 22:57
 * @Description
 */
@EnableConfigurationProperties(RpcServerProperties.class)
@Configuration
public class RpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RegisterService registerService(RpcServerProperties properties) {
        return new ZookeeperRegisterService(properties.getRegistryAddress());
    }

    @Bean
    @ConditionalOnMissingBean(RpcServer.class)
    public RpcServer rpcServer(RegisterService registerService, RpcServerProperties properties) {
        return new RpcServer(registerService, properties);
    }

    @Bean
    @ConditionalOnMissingBean(RpcServiceBeanPostProcessor.class)
    public RpcServiceBeanPostProcessor rpcServiceBeanPostProcessor(RegisterService registerService, RpcServerProperties properties) {
        return new RpcServiceBeanPostProcessor(registerService, properties);
    }
}
