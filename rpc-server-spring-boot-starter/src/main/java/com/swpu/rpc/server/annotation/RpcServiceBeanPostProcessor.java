package com.swpu.rpc.server.annotation;

import com.swpu.rpc.core.common.ServiceInfo;
import com.swpu.rpc.core.register.RegisterService;
import com.swpu.rpc.server.config.RpcServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.Inet4Address;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lms
 * @Date 2022/8/12 18:00
 * @Description 所有bean初始化完成后执行：把所有服务注册到注册中心，并且将服务实现类进行缓存
 */
@Slf4j
public class RpcServiceBeanPostProcessor implements BeanPostProcessor {

    // 将服务实现类对象缓存到本地，避免每次都通过反射从spring容器中寻找
    public static final Map<String, Object> serviceCacheMap = new ConcurrentHashMap<>();

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
                Class<?> serviceClass = null;
                if (annotation.serviceClass() != void.class) {
                    serviceClass = annotation.serviceClass();
                } else if (!"".equals(annotation.serviceName())) {
                    serviceClass = Class.forName(annotation.serviceName());
                } else { // 啥都没给就根据其实现的接口类型进行推断
                    Class<?>[] interfaces = beanClass.getInterfaces();
                    for (Class<?> interfaceClass : interfaces) {
                        if (beanClass.getSimpleName().toLowerCase().contains(interfaceClass.getSimpleName().toLowerCase())) {
                            serviceClass = interfaceClass;
                            break;
                        }
                    }
                }
                if (serviceClass == null) {
                    throw new ClassNotFoundException("服务实现类 " + beanClass.getName() + " 未提供接口");
                }
                String serviceName = serviceClass.getName();
                // 将该服务实现类的单例对象放入缓存中，以便后续 RpcRequestHandler反射调方法用
                serviceCacheMap.put(serviceName, bean);
                ServiceInfo serviceInfo = new ServiceInfo()
                        .setAddress(Inet4Address.getLocalHost().getHostAddress())
                        .setPort(properties.getPort())
                        .setWeight(annotation.weight())
                        .setServiceName(serviceName);
                // 将服务信息注册到注册中心
                registerService.register(serviceInfo);
            } catch (Exception e) {
                log.error("服务注册失败：", e);
            }
        }
        return bean;
    }
}
