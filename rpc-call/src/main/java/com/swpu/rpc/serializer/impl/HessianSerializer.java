package com.swpu.rpc.serializer.impl;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import com.swpu.rpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


/**
 * @Author lms
 * @Date 2022/8/11 23:30
 * @Description
 */
@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException();
        }
        T result;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            HessianSerializerInput hsi = new HessianSerializerInput(bis);
            result = (T) hsi.readObject(clazz);
        } catch (Exception e) {
            log.error("Hessian反序列化失败", e);
            throw new SerializationException(e);
        }
        return result;
    }

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        byte[] results;
        HessianSerializerOutput hso;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            hso = new HessianSerializerOutput(bos);
            hso.writeObject(obj);
            hso.flush();
            results = bos.toByteArray();
        } catch (Exception e) {
            log.error("Hessian序列化失败", e);
            throw new SerializationException(e);
        }
        return results;
    }
}
