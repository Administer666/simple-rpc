package com.swpu.rpc.server.handler;

import com.swpu.rpc.core.message.RpcRequestMessage;
import com.swpu.rpc.core.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static com.swpu.rpc.core.protocol.ProtocolConstants.RPCRESPONSE;
import static com.swpu.rpc.server.annotation.RpcServiceBeanPostProcessor.serviceCacheMap;

/**
 * @Author lms
 * @Date 2022/8/12 17:32
 * @Description
 */
@ChannelHandler.Sharable
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage request) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(request.getSequenceId());
        response.setSerializerId(request.getSerializerId());
        response.setMessageType(RPCRESPONSE);

        try {
            Object service = serviceCacheMap.get(request.getServiceName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object res = method.invoke(service, request.getParameterValues());
            response.setReturnValue(res);
        } catch (Exception e) {
            log.error("远程调用出错: ", e);
            response.setExceptionMessage(e.getCause().toString());
        }
        ctx.writeAndFlush(response);
    }
}