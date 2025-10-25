package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.entity.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class SessionHandler extends SimpleChannelInboundHandler<Message> {
    private static final List<Byte> WHITE_LIST_CMD = List.of((byte) -27, (byte) 1, (byte) 58, (byte) 114, (byte) 121, (byte) 127);
    @Getter
    private final long sessionId;
    @Getter
    private final byte[] sessionKey;
    @Setter
    private Runnable onKeyExchangeComplete;
    private boolean keySent = false;
    private Channel channel;
    private final MessageRouter messageRouter;
    @Getter
    @Setter
    private User user;

    public SessionHandler(long sessionId) {
        this.sessionId = sessionId;
        this.sessionKey = generateSessionKey();


        this.messageRouter = null;
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

    public void sendMessage(Message msg) {
        if (msg == null) {
            log.warn("Cannot send null message for session {}", sessionId);
            return;
        }

        if (channel == null || !channel.isActive()) {
            log.warn("Cannot send message, channel is closed or null for session {}", sessionId);
            return;
        }

        channel.writeAndFlush(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        log.info("New client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
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

        if ((user == null || !user.isLogged()) && !WHITE_LIST_CMD.contains(cmd)) {
            log.warn("Session {} received unauthorized command {} before login. Ignoring.", sessionId, cmd);
            return;
        }

        if (messageRouter != null) {
            messageRouter.onMessage(msg);
        } else {
            log.warn("No message router defined for session {}", sessionId);
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

        // Cleanup user session
        if (user != null && user.isLogged()) {
            //todo: implement user logout logic
        }
    }
}
