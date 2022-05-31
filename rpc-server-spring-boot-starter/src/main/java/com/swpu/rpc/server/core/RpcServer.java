package com.swpu.rpc.server.core;

import com.swpu.rpc.core.protocol.MessageCodec;
import com.swpu.rpc.core.protocol.ProtocolFrameDecoder;
import com.swpu.rpc.core.register.RegisterService;
import com.swpu.rpc.server.config.RpcServerProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author lms
 * @Date 2022/5/27 12:09
 * @Description
 */
@Slf4j
public class RpcServer implements ApplicationRunner {

    private RegisterService registerService;

    private RpcServerProperties properties;


    public RpcServer(RegisterService registerService, RpcServerProperties properties) {
        this.registerService = registerService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        RpcRequestMessageHandler RPC_REQUEST_HANDLER = new RpcRequestMessageHandler();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        Channel channel;
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            channel = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            ch.pipeline().addLast(RPC_REQUEST_HANDLER);
                        }
                    })
                    .bind(hostAddress, properties.getPort())
                    .sync()
                    .channel();
            log.debug("netty server has started on {}:{}", hostAddress, properties.getPort());
            channel.closeFuture().addListener(future -> {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
                registerService.destroy();
            });
        } catch (InterruptedException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
