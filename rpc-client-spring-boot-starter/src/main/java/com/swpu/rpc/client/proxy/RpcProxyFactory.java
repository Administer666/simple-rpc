package com.swpu.rpc.client.proxy;

import com.swpu.rpc.client.RpcClient;
import com.swpu.rpc.client.config.RpcClientProperties;
import com.swpu.rpc.core.balance.LoadBalance;
import com.swpu.rpc.core.common.ServiceInfo;
import com.swpu.rpc.core.discovery.DiscoveryService;
import com.swpu.rpc.core.exception.ResourceNotFoundException;
import com.swpu.rpc.core.exception.RpcException;
import com.swpu.rpc.core.message.RpcRequestMessage;
import com.swpu.rpc.core.serializer.SerializerEnum;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.Proxy;

import static com.swpu.rpc.client.RpcClient.PROMISES;
import static com.swpu.rpc.core.protocol.ProtocolConstants.RPCREQUEST;

/**
 * @Author lms
 * @Date 2022/8/12 21:51
 * @Description
 */
public class RpcProxyFactory {

    public static <T> T getProxy(Class<?> clazz,
                                 DiscoveryService discoveryService,
                                 RpcClientProperties properties,
                                 LoadBalance loadBalance) {
        T proxyInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            ServiceInfo serviceInfo = discoveryService.discovery(clazz.getName(), loadBalance);
            if (serviceInfo == null) {
                throw new ResourceNotFoundException("无法在注册中心找到服务：" + serviceInfo.getServiceName());
            }
            RpcRequestMessage request = new RpcRequestMessage();
            int sequenceId = discoveryService.getNextGlobalId();
            request.setSequenceId(sequenceId);
            request.setMessageType(RPCREQUEST);
            request.setSerializerId((byte) SerializerEnum.getSerializerEnumByName(properties.getSerialization()).ordinal());
            request.setServiceName(clazz.getName());
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameterValues(args);
            Channel channel = RpcClient.getChannel(serviceInfo.getAddress(), serviceInfo.getPort());
            DefaultPromise promise = new DefaultPromise(channel.eventLoop());
            PROMISES.put(sequenceId, promise);
            channel.writeAndFlush(request);
            boolean await = promise.await(properties.getTimeout());
            if (await) {
                if (promise.isSuccess()) {
                    return promise.getNow();
                } else {
                    throw promise.cause();
                }
            } else {
                throw new RpcException("远程调用失败，请求超时 timeout: " + properties.getTimeout() + "ms");
            }
        });
        return proxyInstance;
    }
}
