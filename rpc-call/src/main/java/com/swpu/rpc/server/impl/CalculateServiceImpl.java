package com.swpu.rpc.server.impl;

import com.swpu.rpc.api.CalculateService;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        return a.multiply(b);
    }

    @Override
    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b, 2, RoundingMode.HALF_UP);
    }
}
