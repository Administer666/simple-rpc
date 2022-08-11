package com.swpu.rpc.serializer;

import java.io.IOException;

/**
 * @Author lms
 * @Date 2022/8/11 23:27
 * @Description
 */
public interface Serializer {

    //序列化方法，对象 -> byte[]
    <T> byte[] serialize(T obj) throws IOException;

    // 反序列化方法，byte[] -> 对象
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;

}
