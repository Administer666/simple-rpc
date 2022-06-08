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
        Class<?> beanClass = bean.getClass();
        RpcService annotation = beanClass.getAnnotation(RpcService.class);
        if (annotation != null) {
            try {
                Class<?> interfaceClass = null;
                if (annotation.interfaceClass() != void.class) {
                    interfaceClass = annotation.interfaceClass();
                } else if (!"".equals(annotation.interfaceName())) {
                    interfaceClass = Class.forName(annotation.interfaceName());
                } else { // 啥都没给就根据其实现的接口类型进行推断
                    Class<?>[] interfaces = beanClass.getInterfaces();
                    for (Class<?> itfs : interfaces) {
                        if (beanClass.getSimpleName().toLowerCase().contains(itfs.getSimpleName().toLowerCase())) {
                            interfaceClass = itfs;
                            break;
                        }
                    }
                }
                if (interfaceClass == null) {
                    throw new ClassNotFoundException("服务实现类 " + beanClass.getName() + " 未提供接口");
                }
                LocalServiceBeanCache.put(interfaceClass.getName(), bean);
                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo
                        .setAddress(InetAddress.getLocalHost().getHostAddress())
                        .setPort(properties.getPort())
                        .setAppName(properties.getAppName())
                        .setServiceName(interfaceClass.getName())
                        .setWeight(annotation.weight());
                registerService.register(serviceInfo);
            } catch (Exception e) {
                log.error("服务注册失败：{}", e);
            }
        }
        return bean;
    }
}
