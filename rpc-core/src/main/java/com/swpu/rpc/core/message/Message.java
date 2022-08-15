package com.swpu.rpc.core.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author lms
 * @Date 2022/8/7 23:56
 * @Description
 */
@Data
@Accessors(chain = true)
public class Message implements Serializable {

    // 消息类型，1请求 2响应
    private byte messageType;

    // 序列化算法
    private byte serializerId;

    // 消息序号
    private int sequenceId;
}
