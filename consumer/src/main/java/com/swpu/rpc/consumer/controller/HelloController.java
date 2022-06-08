package com.swpu.rpc.consumer.controller;

import com.swpu.rpc.client.annotation.RpcAutowired;
import com.swpu.rpc.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author lms
 * @Date 2022/5/30 21:59
 * @Description
 */
@RestController
public class HelloController {

    @RpcAutowired
    private HelloService helloService;

    @RpcAutowired(version = "2.0")
    private HelloService helloService2;

    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return helloService.sayHello(name);
    }

    @GetMapping("/hello2")
    public String hello2(@RequestParam("name") String name) {
        return helloService2.sayHello(name);
    }
}