package com.swpu.rpc.core.serializer;

/**
 * @Author lms
 * @Date 2022/5/27 19:43
 * @Description 设计模式——简单工厂
 */
public class SerializerFactory {
    public static Serializer getSerializer(SerializerEnum serializerEnum) {
        switch (serializerEnum) {
            case JAVA:
                return new JavaSerializer();
            case HESSIAN:
                return new HessianSerializer();
            default:
                throw new IllegalArgumentException("serializer type is illegal");
        }
    }
}
