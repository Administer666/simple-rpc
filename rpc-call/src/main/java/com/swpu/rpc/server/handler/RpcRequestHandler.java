package com.swpu.rpc.server.handler;

import com.swpu.rpc.message.RpcRequestMessage;
import com.swpu.rpc.message.RpcResponseMessage;
import com.swpu.rpc.protocol.ProtocolConstants;
import com.swpu.rpc.server.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * @Author lms
 * @Date 2022/8/11 10:15
 * @Description
 */
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage message = new RpcResponseMessage();
        message.setSequenceId(msg.getSequenceId());
        message.setMessageType(ProtocolConstants.RPCRESPONSE);

        try {
            Object service = ServicesFactory.getService(Class.forName(msg.getServiceName()));
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object res = method.invoke(service, msg.getParameterValues());
            message.setReturnValue(res);
        } catch (Exception e) {
            e.printStackTrace();
            message.setExceptionMessage(e.getCause().toString());
        }
        ctx.writeAndFlush(message);
    }
}