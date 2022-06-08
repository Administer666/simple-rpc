package com.swpu.rpc.core.message;

import lombok.Data;

/**
 * @Author lms
 * @Date 2022/5/14 13:17
 * @Description
 */

@Data
public class RpcRequestMessage extends Message {

    // 被调用接口的全限定名 + 版本号
    private String serviceName;
    // 方法名
    private String methodName;
    // 参数类型
    private Class[] parameterTypes;
    // 参数值
    private Object[] parameterValues;

}
