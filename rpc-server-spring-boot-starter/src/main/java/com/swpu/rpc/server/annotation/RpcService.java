package com.swpu.rpc.server.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @Author lms
 * @Date 2022/5/21 17:48
 * @Description
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface RpcService {

//    /**
//     * 标明是哪个服务接口的实现类
//     *
//     * @return
//     */
//    Class<?> interfaceType() default Object.class;

}
