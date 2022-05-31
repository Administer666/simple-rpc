package com.swpu.rpc.core.protocol;

import com.swpu.rpc.core.message.Message;
import com.swpu.rpc.core.message.RpcRequestMessage;
import com.swpu.rpc.core.message.RpcResponseMessage;
import com.swpu.rpc.core.serializer.Serializer;
import com.swpu.rpc.core.serializer.SerializerEnum;
import com.swpu.rpc.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import static com.swpu.rpc.core.protocol.ProtocolConstants.*;

/*
 *
 * @Author lms
 * @Date 2022/5/14 16:31
 * @Description 自定义协议编解码器, 必须和LengthFieldBasedFrameDecoder一同使用，保证到达此处的消息都是完整的
 *
 *  +--------------------------------------------------------------+
 *  | 魔数 1byte | 协议版本号 1byte | 序列化算法 1byte | 消息类型 1byte  |
 *  +--------------------------------------------------------------+
 *  |              消息序号 8byte               |   数据长度 4byte    |
 *  +--------------------------------------------------------------+
 *  |                   数据内容 （长度不定）                          |
 *  +--------------------------------------------------------------+
 */
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1字节魔数
        out.writeByte(MAGIC);
        // 1字节版本号
        out.writeByte(VERSION);
        // 1字节的序列化算法
        out.writeByte(msg.getSerialization());
        // 1字节的报文类型
        out.writeByte(msg.getMessageType());
        // 8字节消息序号
        out.writeLong(msg.getSequenceId());
        Serializer serializer = SerializerFactory.getSerializer(SerializerEnum.getSerializerEnumById(msg.getSerialization()));
        byte[] bytes = serializer.serialize(msg);
        // 4字节消息长度
        out.writeInt(bytes.length);
        // 不定长消息正文
        out.writeBytes(bytes);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte magic = in.readByte();
        if (magic != MAGIC) {
            throw new IllegalArgumentException("非法魔数： " + magic);
        }
        byte version = in.readByte();
        byte serialization = in.readByte();
        byte messageType = in.readByte();
        long sequenceId = in.readLong();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length); // 消息正文读到byte数组里

        Serializer serializer = SerializerFactory.getSerializer(SerializerEnum.getSerializerEnumById(serialization));
        Message message;
        if (messageType == RPCREQUEST) {
            message = serializer.deserialize(RpcRequestMessage.class, bytes);
        } else {
            message = serializer.deserialize(RpcResponseMessage.class, bytes);
        }
        out.add(message);
    }
}
