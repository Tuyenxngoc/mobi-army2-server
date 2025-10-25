package com.teamobi.mobiarmy2.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {
    private final SessionHandler sessionHandler;
    private byte curR;

    public MessageDecoder(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    private byte readKey(byte b) {
        byte[] sessionKey = sessionHandler.getSessionKey();
        byte result = (byte) ((sessionKey[curR++] & 0xFF) ^ (b & 0xFF));
        if (curR >= sessionKey.length) {
            curR = 0;
        }
        return result;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Header tối thiểu = 3 bytes (cmd + size)
        if (in.readableBytes() < 3) {
            return;
        }

        in.markReaderIndex();
        byte startCurR = curR;

        // Đọc CMD
        byte cmd = readKey(in.readByte());

        // Độ dài (2 bytes)
        byte b1 = in.readByte();
        byte b2 = in.readByte();
        int size = ((readKey(b1) & 0xFF) << 8) | (readKey(b2) & 0xFF);

        // Chưa đủ dữ liệu cho body → chờ thêm
        if (in.readableBytes() < size) {
            in.resetReaderIndex();
            curR = startCurR;
            return;
        }

        // Đọc và giải mã body
        byte[] data = new byte[size];
        in.readBytes(data);
        for (int i = 0; i < data.length; i++) {
            data[i] = readKey(data[i]);
        }

        // Thêm vào pipeline
        out.add(new Message(cmd, data));
    }

}
