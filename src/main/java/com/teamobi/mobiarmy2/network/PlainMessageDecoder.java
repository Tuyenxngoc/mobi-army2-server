package com.teamobi.mobiarmy2.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class PlainMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 3) {
            return;
        }

        in.markReaderIndex();

        byte cmd = in.readByte();
        int size = in.readUnsignedShort();

        if (in.readableBytes() < size) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[size];
        in.readBytes(data);

        out.add(new Message(cmd, data));
    }
}
