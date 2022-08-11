package com.swpu.rpc.protocol;

import com.swpu.rpc.config.Config;
import com.swpu.rpc.message.Message;
import com.swpu.rpc.message.RpcRequestMessage;
import com.swpu.rpc.message.RpcResponseMessage;
import com.swpu.rpc.serializer.Serializer;
import com.swpu.rpc.serializer.SerializerEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import static com.swpu.rpc.protocol.ProtocolConstants.*;

/**
 * @Author lms
 * @Date 2022/8/8 0:15
 * @Description 自定义协议编解码器, 必须和LengthFieldBasedFrameDecoder一同使用，保证到达此处的消息都是完整的
 * <p>
 * +--------------------------------------------------------------+
 * | 魔数 4byte | 协议版本号 2byte | 序列化算法 1byte | 消息类型 1byte  |
 * +--------------------------------------------------------------+
 * |       消息序号 4byte          |          数据长度 4byte         |
 * +--------------------------------------------------------------+
 * |                   数据内容 （长度不定）                          |
 * +--------------------------------------------------------------+
 */
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 4字节魔数
        out.writeInt(MAGIC);
        // 2字节版本号
        out.writeShort(VERSION);
        // 1字节的序列化算法
        SerializerEnum serializerEnum = SerializerEnum.getSerializerEnumByName(Config.getSerializer());
        out.writeByte(serializerEnum.ordinal());
        // 1字节的报文类型
        out.writeByte(msg.getMessageType());
        // 4字节消息序号
        out.writeInt(msg.getSequenceId());

        byte[] bytes = serializerEnum.getSerializer().serialize(msg);
        // 4字节消息长度
        out.writeInt(bytes.length);
        // 不定长消息正文
        out.writeBytes(bytes);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magic = in.readInt();
        if (magic != MAGIC) {
            throw new IllegalArgumentException("非法魔数： " + magic);
        }
        short version = in.readShort();
        byte serialization = in.readByte();
        byte messageType = in.readByte();
        long sequenceId = in.readInt();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length); // 消息正文读到byte数组里

        Serializer serializer = SerializerEnum.getSerializerById(serialization);
        Message message;
        if (RPCREQUEST == messageType) {
            message = serializer.deserialize(RpcRequestMessage.class, bytes);
        } else {
            message = serializer.deserialize(RpcResponseMessage.class, bytes);
        }
        out.add(message);
    }
}
