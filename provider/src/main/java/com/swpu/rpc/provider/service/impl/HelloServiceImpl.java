package com.swpu.rpc.provider.service.impl;

import com.swpu.rpc.server.annotation.RpcService;
import com.swpu.rpc.service.HelloService;

/**
 * @Author lms
 * @Date 2022/5/30 21:58
 * @Description
 */
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "你好，" + name;
    }
}
