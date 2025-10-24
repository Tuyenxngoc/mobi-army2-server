package com.teamobi.mobiarmy2.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlainMessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        try {
            out.writeByte(msg.getCommand());
            byte[] data = msg.getData();
            if (data != null) {
                out.writeShort(data.length);
                out.writeBytes(data);
            } else {
                out.writeShort(0);
            }
        } catch (Exception e) {
            log.error("Lỗi trong quá trình mã hóa PlainMessage", e);
            ctx.close();
        }
    }
}
