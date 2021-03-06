package com.swpu.rpc.client.proxy;

import com.swpu.rpc.client.config.RpcClientProperties;
import com.swpu.rpc.client.core.PromiseMap;
import com.swpu.rpc.client.core.RpcClientFactory;
import com.swpu.rpc.core.balance.LoadBalance;
import com.swpu.rpc.core.common.ServiceInfo;
import com.swpu.rpc.core.common.ServiceUtil;
import com.swpu.rpc.core.discovery.DiscoveryService;
import com.swpu.rpc.core.message.RpcRequestMessage;
import com.swpu.rpc.core.serializer.SerializerEnum;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.Proxy;

import static com.swpu.rpc.core.protocol.ProtocolConstants.RPCREQUEST;

/**
 * @Author lms
 * @Date 2022/5/28 16:47
 * @Description
 */
public class RpcProxyFactory {

    public static <T> T getProxy(Class<?> clazz, String version, DiscoveryService discoveryService, RpcClientProperties properties, LoadBalance loadBalance) {
        T proxyInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            String serviceName = ServiceUtil.join(clazz.getName(), version);
            ServiceInfo serviceInfo = discoveryService.discovery(serviceName, loadBalance);
            if (serviceInfo == null) {
                throw new RuntimeException("无法在注册中心找到服务：" + serviceInfo.getServiceName());
            }
            long sequenceId = discoveryService.getNextGeneralId();

            RpcRequestMessage message = new RpcRequestMessage();
            message.setMessageType(RPCREQUEST);
            message.setSerialization((byte) SerializerEnum.getSerializerEnumByName(properties.getSerialization()).ordinal());
            message.setSequenceId(sequenceId);
            message.setServiceName(serviceName);
            message.setMethodName(method.getName());
            message.setParameterTypes(method.getParameterTypes());
            message.setParameterValues(args);

            Channel channel = RpcClientFactory.getChannel(serviceInfo.getAddress(), serviceInfo.getPort());
            DefaultPromise promise = new DefaultPromise(channel.eventLoop());
            PromiseMap.PROMISES.put(sequenceId, promise);
            channel.writeAndFlush(message);
            promise.await();
            if (promise.isSuccess()) {
                return promise.getNow();
            }
            return null;
        });
        return proxyInstance;
    }
}
