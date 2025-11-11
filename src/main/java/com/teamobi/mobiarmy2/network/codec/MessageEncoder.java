package com.teamobi.mobiarmy2.network.codec;

import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.SessionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Message> {
    private final SessionHandler sessionHandler;
    private byte curW;

    public MessageEncoder(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    private byte writeKey(byte b) {
        byte[] sessionKey = sessionHandler.getEncryptionKey();
        byte result = (byte) ((sessionKey[curW++] & 0xFF) ^ (b & 0xFF));
        if (curW >= sessionKey.length) {
            curW = 0;
        }
        return result;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] data = msg.getData();
        byte command = msg.getCommand();
        int size = (data != null) ? data.length : 0;

        // Các lệnh đặc biệt: gửi raw data, không mã hóa
        boolean isRawCmd = command == -120 || command == 90 || command == -104 || command == -108;

        // Ghi CMD
        out.writeByte(writeKey(command));

        if (isRawCmd) {
            out.writeInt(size);
            if (data != null && size > 0) {
                out.writeBytes(data);
            }
        } else {
            // Ghi độ dài
            out.writeByte(writeKey((byte) (size >> 8)));
            out.writeByte(writeKey((byte) (size & 0xFF)));

            if (data != null && size > 0) {
                // Mã hóa nội dung
                for (int i = 0; i < size; i++) {
                    out.writeByte(writeKey(data[i]));
                }
            }
        }
    }
}
