package com.swpu.rpc.client.core;

import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lms
 * @Date 2022/5/29 17:12
 * @Description
 */
public class PromiseMap {
    // key 代表sequenceId，value代表两个线程共用的信箱，netty线程负责放，主线程负责取
    public static final Map<Long, Promise> PROMISES = new ConcurrentHashMap<>();
}
