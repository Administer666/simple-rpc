package com.swpu.rpc.client;

import com.swpu.rpc.client.handler.RpcResponseHandler;
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
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lms
 * @Date 2022/8/12 22:54
 * @Description 维护着与多个服务端的channel
 */
@Slf4j
public class RpcClient {

    // key 代表sequenceId，value代表两个线程共用的信箱，netty线程负责放，主线程负责取
    public static final Map<Integer, Promise> PROMISES = new ConcurrentHashMap<>();

    private static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    public static Channel getChannel(String address, Integer port) {
        return channelMap.computeIfAbsent(address + ":" + port, k -> createChannel(address, port));
    }

    private static Channel createChannel(String address, Integer port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        RpcResponseHandler RPC_RESPONSE_HANDLER = new RpcResponseHandler();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        Channel channel = null;
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
            log.debug("netty client has connected to the server {}:{}", address, port);
            channel.closeFuture().addListener(future -> {
                channelMap.remove(address + ":" + port);
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("netty client connection failed", e);
        }
        return channel;
    }
}
