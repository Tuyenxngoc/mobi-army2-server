package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.constant.Cmd;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

@Slf4j
@ChannelHandler.Sharable
public class SessionHandler extends SimpleChannelInboundHandler<Message> {

    @Getter
    private final long sessionId;
    @Getter
    private final byte[] sessionKey;
    @Setter
    private Runnable onKeyExchangeComplete;
    private boolean keySent = false;

    public SessionHandler(long sessionId) {
        this.sessionId = sessionId;
        this.sessionKey = generateSessionKey();
    }

    private byte[] generateSessionKey() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private void triggerKeyExchangeComplete() {
        if (onKeyExchangeComplete != null) {
            onKeyExchangeComplete.run();
        }
        log.info("Session {} key exchange completed", sessionId);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("New client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        try {
            byte cmd = msg.getCommand();

            // Handle GET_KEY command
            if (cmd == Cmd.GET_KEY && !keySent) {
                try {
                    Message ms = new Message(Cmd.GET_KEY);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(sessionKey.length);
                    ds.writeByte(sessionKey[0]);
                    for (int i = 1; i < sessionKey.length; i++) {
                        ds.writeByte(sessionKey[i] ^ sessionKey[i - 1]);
                    }
                    ds.flush();

                    log.info("Sent session key to {}", sessionId);
                    ctx.writeAndFlush(ms).addListener(future -> {
                        if (future.isSuccess()) {
                            triggerKeyExchangeComplete();
                            keySent = true;
                        } else {
                            log.error("Session {} failed to send session key: {}", sessionId, future.cause().getMessage());
                            ctx.close();
                        }
                    });

                } catch (IOException e) {
                    log.error("Session {} error while sending session key: {}", sessionId, e.getMessage());
                    ctx.close();
                }

                return;
            }

            log.info("Session {} received message: {}", sessionId, cmd);

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Session {} error: {}", sessionId, cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Client {} disconnected", ctx.channel().remoteAddress());
    }
}
