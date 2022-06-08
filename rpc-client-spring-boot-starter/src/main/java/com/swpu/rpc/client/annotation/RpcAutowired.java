package com.swpu.rpc.client.annotation;

import java.lang.annotation.*;

/**
 * @Author lms
 * @Date 2022/5/28 15:56
 * @Description
 */

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcAutowired {

    // 版本号
    String version() default "";

    // 负载均衡策略
    String loadbalance() default "roundrobin";
}
