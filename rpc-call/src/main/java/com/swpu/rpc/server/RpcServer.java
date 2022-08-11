package com.swpu.rpc.server;

import com.swpu.rpc.config.Config;
import com.swpu.rpc.server.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import com.swpu.rpc.protocol.MessageCodec;
import com.swpu.rpc.protocol.ProtocolFrameDecoder;

/**
 * @Author lms
 * @Date 2022/8/11 10:01
 * @Description
 */
public class RpcServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        RpcRequestHandler RPC_REQUEST_HANDLER = new RpcRequestHandler();

        try {
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
                    .bind(Config.getServerPort())
                    .sync()
                    .channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
