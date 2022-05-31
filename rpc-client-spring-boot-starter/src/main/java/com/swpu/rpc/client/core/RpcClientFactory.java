package com.swpu.rpc.client.core;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lms
 * @Date 2022/5/29 11:17
 * @Description
 */
public class RpcClientFactory {

    private static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    public static Channel getChannel(String address, Integer port) {
        String key = address + ":" + port;
        return channelMap.computeIfAbsent(key, k -> new RpcClient(address, port).getChannel());
    }

}
