package com.swpu.rpc.client.annotation;

import java.lang.annotation.*;

/**
 * @Author lms
 * @Date 2022/8/12 19:21
 * @Description
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcAutowired {

}

