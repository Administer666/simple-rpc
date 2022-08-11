package com.swpu.rpc.client.handler;

import com.swpu.rpc.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import static com.swpu.rpc.client.RpcClient.promiseMap;

/**
 * @Author lms
 * @Date 2022/8/11 11:31
 * @Description
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        Promise promise = promiseMap.remove(msg.getSequenceId());
        if (msg.getReturnValue() != null) {
            promise.setSuccess(msg.getReturnValue());
        } else {
            promise.setFailure(new Exception("远程调用失败，被调用方异常信息: " + msg.getExceptionMessage()));
        }
    }
}
