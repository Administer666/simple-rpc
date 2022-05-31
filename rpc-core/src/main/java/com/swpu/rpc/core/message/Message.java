package com.swpu.rpc.core.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author lms
 * @Date 2022/5/27 21:22
 * @Description
 */
@Data
public class Message<T> implements Serializable {

    private byte messageType;
    private byte serialization;
    private long sequenceId;

}
