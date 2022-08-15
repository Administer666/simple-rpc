package com.swpu.rpc.provider.service.impl;

import com.swpu.rpc.server.annotation.RpcService;
import com.swpu.rpc.service.CalculateService;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Author lms
 * @Date 2022/5/31 16:36
 * @Description
 */
@RpcService
public class CalculateServiceImpl implements CalculateService {
    @Override
    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    @Override
    public BigDecimal sub(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    @Override
    public BigDecimal multi(BigDecimal a, BigDecimal b) {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a.multiply(b);
    }

    @Override
    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        int x = 10 / 0;
        return a.divide(b, 2, RoundingMode.HALF_UP);
    }
}
