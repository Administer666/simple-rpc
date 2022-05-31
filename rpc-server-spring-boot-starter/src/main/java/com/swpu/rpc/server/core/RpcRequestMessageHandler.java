package com.swpu.rpc.server.core;

import com.swpu.rpc.core.message.RpcRequestMessage;
import com.swpu.rpc.core.message.RpcResponseMessage;
import com.swpu.rpc.server.bean.LocalServiceBeanCache;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static com.swpu.rpc.core.protocol.ProtocolConstants.RPCRESPONSE;

/**
 * @Author lms
 * @Date 2022/5/27 12:14
 * @Description
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage request) throws Exception {
        log.debug("接收到一条客户端请求信息：{}", request);

        RpcResponseMessage response = new RpcResponseMessage();
        response.setMessageType(RPCRESPONSE);
        response.setSerialization(request.getSerialization());
        response.setSequenceId(request.getSequenceId());

        String interfaceName = request.getInterfaceName();
        Object bean = LocalServiceBeanCache.get(interfaceName);
        Method method = bean.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
        Object returnValue = method.invoke(bean, request.getParameterValues());
        response.setReturnValue(returnValue);
        ctx.writeAndFlush(response);
    }
}
