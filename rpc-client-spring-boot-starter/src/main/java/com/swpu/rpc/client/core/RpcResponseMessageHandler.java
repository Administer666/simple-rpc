package com.swpu.rpc.client.core;

import com.swpu.rpc.core.message.RpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author lms
 * @Date 2022/5/28 16:11
 * @Description
 */
@Slf4j
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage response) throws Exception {
        log.debug("接收到一条服务端响应信息：{}", response);

        Promise promise = PromiseMap.PROMISES.remove(response.getSequenceId());
        promise.setSuccess(response.getReturnValue());
    }
}
