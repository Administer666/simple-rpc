package com.swpu.rpc.core.message;

import lombok.Data;

/**
 * @Author lms
 * @Date 2022/8/8 0:08
 * @Description
 */
@Data
public class RpcRequestMessage extends Message {

    // 被调用服务的全限定名
    private String serviceName;

    // 方法名
    private String methodName;

    // 参数类型
    private Class[] parameterTypes;

    // 参数值
    private Object[] parameterValues;
}
