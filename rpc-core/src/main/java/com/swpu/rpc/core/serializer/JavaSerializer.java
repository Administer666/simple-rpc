package com.swpu.rpc.core.serializer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author lms
 * @Date 2022/5/27 17:38
 * @Description
 */
@Slf4j
public class JavaSerializer implements Serializer {

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Java反序列化失败", e);
            throw new RuntimeException("Java反序列化失败", e);
        }
    }

    @Override
    public <T> byte[] serialize(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Java序列化失败", e);
            throw new RuntimeException("Java序列化失败", e);
        }
    }
}
