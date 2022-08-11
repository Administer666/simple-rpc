package com.swpu.rpc.message;

import lombok.Data;

/**
 * @Author lms
 * @Date 2022/8/8 0:12
 * @Description
 */
@Data
public class RpcResponseMessage extends Message {

    // 正常返回值
    private Object returnValue;

    // 这里只传异常简短信息，全部异常堆栈信息太长了
    private String exceptionMessage;
}
