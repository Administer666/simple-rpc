package com.swpu.rpc.server.annotation;

import com.swpu.rpc.core.common.ServiceInfo;
import com.swpu.rpc.core.register.RegisterService;
import com.swpu.rpc.server.bean.LocalServiceBeanCache;
import com.swpu.rpc.server.config.RpcServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;

/**
 * @Author lms
 * @Date 2022/5/27 12:50
 * @Description 所有bean初始化完成后执行：把所有服务注册到注册中心，并且将服务实现类进行缓存
 */
@Slf4j
public class RpcServiceBeanPostProcessor implements BeanPostProcessor {

    private RegisterService registerService;

    private RpcServerProperties properties;

    public RpcServiceBeanPostProcessor(RegisterService registerService, RpcServerProperties properties) {
        this.registerService = registerService;
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            try {
                Class<?>[] interfaces = bean.getClass().getInterfaces();
                for (Class<?> inface : interfaces) {
                    LocalServiceBeanCache.put(inface.getName(), bean);
                    ServiceInfo serviceInfo = new ServiceInfo();
                    serviceInfo
                            .setAddress(InetAddress.getLocalHost().getHostAddress())
                            .setPort(properties.getPort())
                            .setAppName(properties.getAppName())
                            .setServiceName(inface.getName());
                    registerService.register(serviceInfo);
                }
            } catch (Exception e) {
                log.error("服务注册出错，{}", e);
            }
        }
        return bean;
    }
}
