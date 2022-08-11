package com.swpu.rpc.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @Author lms
 * @Date 2022/8/8 0:36
 * @Description
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        super(2048, 12, 4, 0, 0);
    }
}
