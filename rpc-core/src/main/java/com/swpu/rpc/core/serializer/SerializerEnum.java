package com.swpu.rpc.core.serializer;

/**
 * @Author lms
 * @Date 2022/5/17 12:58
 * @Description
 */
public enum SerializerEnum {
    HESSIAN,
    JAVA;

    public static SerializerEnum getSerializerEnumByName(String name) {
        for (SerializerEnum serializerEnum : SerializerEnum.values()) {
            if (serializerEnum.name().equalsIgnoreCase(name)) {
                return serializerEnum;
            }
        }
        return HESSIAN;
    }

    public static SerializerEnum getSerializerEnumById(int id) {
        for (SerializerEnum serializerEnum : SerializerEnum.values()) {
            if (serializerEnum.ordinal() == id) {
                return serializerEnum;
            }
        }
        return HESSIAN;
    }
}
