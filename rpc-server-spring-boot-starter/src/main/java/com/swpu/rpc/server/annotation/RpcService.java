package com.swpu.rpc.server.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author lms
 * @Date 2022/5/21 17:48
 * @Description
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {

    // 要被实现的服务的类的全限定名
    String serviceName() default "";

    // 要被实现的服务的类对象
    Class<?> serviceClass() default void.class;

    // 版本号
    String version() default "1.0";

    // 负载均衡权重
    int weight() default 1;
}
