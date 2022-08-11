package com.swpu.rpc.client;

import com.swpu.rpc.api.CalculateService;
import com.swpu.rpc.api.HelloService;
import com.swpu.rpc.client.handler.RpcResponseHandler;
import com.swpu.rpc.config.Config;
import com.swpu.rpc.message.RpcRequestMessage;
import com.swpu.rpc.protocol.MessageCodec;
import com.swpu.rpc.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.swpu.rpc.protocol.ProtocolConstants.RPCREQUEST;

/**
 * @Author lms
 * @Date 2022/8/11 11:25
 * @Description
 */
public class RpcClient {

    public static final Map<Integer, Promise> promiseMap = new ConcurrentHashMap<>();
    private static final AtomicInteger autoIncrementId = new AtomicInteger();
    private volatile static Channel channel;


    public static void main(String[] args) {
        HelloService helloService = getProxyService(HelloService.class);
        System.out.println(helloService.sayHello("张三"));

        CalculateService calculateService = getProxyService(CalculateService.class);
        BigDecimal res = calculateService.add(new BigDecimal("1.2"), new BigDecimal("2.6"));
        System.out.println(res.doubleValue());
    }

    private static <T> T getProxyService(Class<T> clazz) {
        T proxyInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            RpcRequestMessage message = new RpcRequestMessage();
            message.setSequenceId(autoIncrementId.incrementAndGet());
            message.setMessageType(RPCREQUEST);
            message.setServiceName(clazz.getName());
            message.setMethodName(method.getName());
            message.setParameterTypes(method.getParameterTypes());
            message.setParameterValues(args);

            Channel channel = getChannel();
            DefaultPromise promise = new DefaultPromise(channel.eventLoop());
            promiseMap.put(message.getSequenceId(), promise);
            channel.writeAndFlush(message);

            promise.await();
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                throw promise.cause();
            }
        });
        return proxyInstance;
    }

    public static Channel getChannel() {
        if (channel == null) {
            synchronized (RpcClient.class) {
                if (channel == null) {
                    initChannel();
                }
            }
        }
        return channel;
    }

    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        RpcResponseHandler RPC_RESPONSE_HANDLER = new RpcResponseHandler();
        try {
            channel = new Bootstrap()
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
                    .connect(Config.getServerHost(), Config.getServerPort())
                    .sync()
                    .channel();
            channel.closeFuture().addListener(future -> group.shutdownGracefully());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
