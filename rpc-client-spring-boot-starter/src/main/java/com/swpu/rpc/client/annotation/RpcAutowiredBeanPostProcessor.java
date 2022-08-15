package com.swpu.rpc.client.annotation;

import com.swpu.rpc.client.config.RpcClientProperties;
import com.swpu.rpc.client.proxy.RpcProxyFactory;
import com.swpu.rpc.core.discovery.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * @Author lms
 * @Date 2022/8/12 19:35
 * @Description 将类中标注 @RpcAutowired注解的字段注入代理后的bean
 */
@Slf4j
public class RpcAutowiredBeanPostProcessor implements BeanPostProcessor {

    private DiscoveryService discoveryService;

    private RpcClientProperties properties;

    public RpcAutowiredBeanPostProcessor(DiscoveryService discoveryService,
                                         RpcClientProperties properties) {
        this.discoveryService = discoveryService;
        this.properties = properties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            RpcAutowired annotation = field.getAnnotation(RpcAutowired.class);
            if (annotation != null) {
                try {
                    field.setAccessible(true);
                    field.set(bean, RpcProxyFactory.getProxy(field.getType(), discoveryService, properties));
                } catch (IllegalAccessException e) {
                    log.error("属性赋值失败", e);
                }
            }
        }
        return bean;
    }
}
