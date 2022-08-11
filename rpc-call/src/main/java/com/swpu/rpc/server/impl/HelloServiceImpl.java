package com.swpu.rpc.server.impl;

import com.swpu.rpc.api.HelloService;

/**
 * @Author lms
 * @Date 2022/8/11 10:17
 * @Description
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
//        int i = 10 / 0;
        return "你好！" + name;
    }
}
