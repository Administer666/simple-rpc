package com.swpu.rpc.consumer.controller;

import com.swpu.rpc.client.annotation.RpcAutowired;
import com.swpu.rpc.service.CalculateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @Author lms
 * @Date 2022/5/31 17:02
 * @Description
 */
@RestController
public class CalculateController {

    @RpcAutowired
    private CalculateService calculateService;

    @GetMapping("/add/{a}/{b}")
    public String add(@PathVariable BigDecimal a, @PathVariable BigDecimal b) {
        String s = a + " + " + b + " = " + calculateService.add(a, b);
        return s;
    }

    @GetMapping("/sub/{a}/{b}")
    public String sub(@PathVariable BigDecimal a, @PathVariable BigDecimal b) {
        String s = a + " - " + b + " = " + calculateService.sub(a, b);
        return s;
    }

    @GetMapping("/multi/{a}/{b}")
    public String multi(@PathVariable BigDecimal a, @PathVariable BigDecimal b) {
        String s = a + " * " + b + " = " + calculateService.multi(a, b);
        return s;
    }

    @GetMapping("/divide/{a}/{b}")
    public String divide(@PathVariable BigDecimal a, @PathVariable BigDecimal b) {
        String s = a + " / " + b + " = " + calculateService.divide(a, b);
        return s;
    }
}