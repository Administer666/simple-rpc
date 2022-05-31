package com.swpu.rpc.core.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @Author lms
 * @Date 2022/5/15 9:17
 * @Description
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        super(1024, 12, 4, 0, 0);
    }
}
