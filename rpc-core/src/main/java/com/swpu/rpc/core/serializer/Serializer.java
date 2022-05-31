package com.swpu.rpc.core.serializer;

import java.io.IOException;

/**
 * @Author lms
 * @Date 2022/5/17 11:04
 * @Description
 */
public interface Serializer {

    // 反序列化方法，byte[] -> 对象
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;

    //序列化方法，对象 -> byte[]
    <T> byte[] serialize(T obj) throws IOException;
}
