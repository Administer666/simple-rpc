package com.swpu.rpc.server;

import com.swpu.rpc.core.protocol.MessageCodec;
import com.swpu.rpc.core.protocol.ProtocolFrameDecoder;
import com.swpu.rpc.core.register.RegisterService;
import com.swpu.rpc.server.config.RpcServerProperties;
import com.swpu.rpc.server.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.net.Inet4Address;

/**
 * @Author lms
 * @Date 2022/8/12 17:30
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
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        RpcRequestHandler RPC_REQUEST_HANDLER = new RpcRequestHandler();

        try {
            String host = Inet4Address.getLocalHost().getHostAddress();
            Integer port = properties.getPort();
            Channel channel = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            ch.pipeline().addLast(RPC_REQUEST_HANDLER);
                        }
                    })
                    .bind(host, port)
                    .sync()
                    .channel();
            log.debug("netty server has started on {}:{}", host, port);
            channel.closeFuture().addListener(future -> {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
                registerService.destroy();
            });
        } catch (Exception e) {
            log.error("netty server failed to start", e);
        }
    }
}
