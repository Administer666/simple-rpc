package com.swpu.rpc.client.core;

import com.swpu.rpc.core.protocol.MessageCodec;
import com.swpu.rpc.core.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author lms
 * @Date 2022/5/28 16:08
 * @Description
 */
@Slf4j
public class RpcClient {

    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public RpcClient(String address, Integer port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        RpcResponseMessageHandler RPC_RESPONSE_HANDLER = new RpcResponseMessageHandler();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        try {
            channel = new Bootstrap()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            ch.pipeline().addLast(RPC_RESPONSE_HANDLER);
                        }
                    })
                    .connect(address, port)
                    .sync()
                    .channel();
            channel.closeFuture().addListener(future -> group.shutdownGracefully());
        } catch (InterruptedException e) {
            log.error("netty客户端连接失败，请检查服务端是否已开启，{}", e);
        }
    }
}
