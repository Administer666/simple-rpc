package com.swpu.rpc.core.serializer;

import com.swpu.rpc.core.serializer.impl.HessianSerializer;
import com.swpu.rpc.core.serializer.impl.JavaSerializer;
import com.swpu.rpc.core.serializer.impl.ProtostuffSerializer;

/**
 * @Author lms
 * @Date 2022/8/11 23:47
 * @Description
 */
public enum SerializerEnum {
    PROTOSTUFF(new ProtostuffSerializer()),
    HESSIAN(new HessianSerializer()),
    JAVA(new JavaSerializer());

    Serializer serializer;

    SerializerEnum(Serializer serializer) {
        this.serializer = serializer;
    }

    public static SerializerEnum getSerializerEnumByName(String name) {
        for (SerializerEnum serializerEnum : SerializerEnum.values()) {
            if (serializerEnum.name().equalsIgnoreCase(name)) {
                return serializerEnum;
            }
        }
        return PROTOSTUFF;
    }

    public static Serializer getSerializerById(int id) {
        for (SerializerEnum serializerEnum : SerializerEnum.values()) {
            if (serializerEnum.ordinal() == id) {
                return serializerEnum.serializer;
            }
        }
        return PROTOSTUFF.serializer;
    }
}
