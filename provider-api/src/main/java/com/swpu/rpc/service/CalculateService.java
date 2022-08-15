package com.swpu.rpc.service;

import java.math.BigDecimal;

/**
 * @Author lms
 * @Date 2022/8/11 10:03
 * @Description
 */
public interface CalculateService {

    BigDecimal add(BigDecimal a, BigDecimal b);

    BigDecimal sub(BigDecimal a, BigDecimal b);

    BigDecimal multi(BigDecimal a, BigDecimal b);

    BigDecimal divide(BigDecimal a, BigDecimal b);

}
