package com.swpu.rpc.core.serializer;

/**
 * @Author lms
 * @Date 2022/5/27 19:43
 * @Description 设计模式——简单工厂
 */
public class SerializerFactory {

    public static JavaSerializer JAVASERIALIZER = new JavaSerializer();
    public static HessianSerializer HESSIANSERIALIZER = new HessianSerializer();
    public static ProtostuffSerializer PROTOSTUFFSERIALIZER = new ProtostuffSerializer();

    public static Serializer getSerializer(SerializerEnum serializerEnum) {
        switch (serializerEnum) {
            case JAVA:
                return JAVASERIALIZER;
            case HESSIAN:
                return HESSIANSERIALIZER;
            case PROTOSTUFF:
                return PROTOSTUFFSERIALIZER;
            default:
                throw new IllegalArgumentException("serializer type is illegal");
        }
    }
}
