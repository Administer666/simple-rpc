package com.swpu.rpc.api;

import java.math.BigDecimal;

/**
 * @Author lms
 * @Date 2022/5/30 20:58
 * @Description
 */
public interface CalculateService {

    BigDecimal add(BigDecimal a, BigDecimal b);

    BigDecimal sub(BigDecimal a, BigDecimal b);

    BigDecimal multi(BigDecimal a, BigDecimal b);

    BigDecimal divide(BigDecimal a, BigDecimal b);

}
