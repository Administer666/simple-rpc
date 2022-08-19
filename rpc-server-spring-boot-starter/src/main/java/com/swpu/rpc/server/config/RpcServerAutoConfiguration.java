package com.swpu.rpc.server.config;

import com.swpu.rpc.core.register.RegisterService;
import com.swpu.rpc.core.register.impl.ZookeeperRegisterService;
import com.swpu.rpc.server.RpcServer;
import com.swpu.rpc.server.annotation.RpcServiceBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lms
 * @Date 2022/8/12 17:43
 * @Description
 */
@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RegisterService registerService(RpcServerProperties properties) {
        return new ZookeeperRegisterService(properties.getRegistryAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcServer rpcServer(RegisterService registerService,
                               RpcServerProperties properties) {
        return new RpcServer(registerService, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcServiceBeanPostProcessor rpcServiceBeanPostProcessor(RegisterService registerService,
                                                                   RpcServerProperties properties) {
        return new RpcServiceBeanPostProcessor(registerService, properties);
    }
}
