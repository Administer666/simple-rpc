package com.swpu.rpc.core.message;

import lombok.Data;

/**
 * @Author lms
 * @Date 2022/5/14 13:17
 * @Description
 */
@Data
public class RpcResponseMessage extends Message {

    // 正常返回值
    private Object returnValue;
    // 异常值
    private Exception exceptionValue;
}
