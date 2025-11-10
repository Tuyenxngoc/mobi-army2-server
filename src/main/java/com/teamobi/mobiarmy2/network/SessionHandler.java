package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.server.ServerManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Set;

@Slf4j
public class SessionHandler extends SimpleChannelInboundHandler<Message> {
    private static final Set<Byte> KEEP_ALIVE_CMDS = Set.of((byte) 20, (byte) 16);//todo: add other keep-alive commands
    private static final int TIMEOUT_DURATION = 180_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Getter
    private final byte[] encryptionKey;
    private boolean keySent = false;

    @Setter
    private Runnable onKeyExchangeComplete;

    private Session session;
    private final ServerManager serverManager;
    private long lastKeepAliveTime = System.currentTimeMillis();

    public SessionHandler(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.encryptionKey = generateSessionKey();
    }

    private byte[] generateSessionKey() {
        byte[] key = new byte[16];
        RANDOM.nextBytes(key);
        return key;
    }

    private void triggerKeyExchangeComplete() {
        if (onKeyExchangeComplete != null) {
            onKeyExchangeComplete.run();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        long sessionId = System.nanoTime();
        this.session = new Session(sessionId, ctx.channel());

        serverManager.addSession(session);

        log.info("New client connected: {}", sessionId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        byte cmd = msg.getCommand();

        log.debug("Client {} sends ms {}", session.getSessionId(), Cmd.getCmdNameByValue(cmd));

        if (KEEP_ALIVE_CMDS.contains(cmd)) {
            lastKeepAliveTime = System.currentTimeMillis();
        }

        if (cmd == Cmd.GET_KEY && !keySent) {
            handleGetKey(ctx);
            return;
        }

        session.enqueueMessage(msg);
    }

    private void handleGetKey(ChannelHandlerContext ctx) {
        try {
            Message ms = new Message(Cmd.GET_KEY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(encryptionKey.length);
            ds.writeByte(encryptionKey[0]);
            for (int i = 1; i < encryptionKey.length; i++) {
                ds.writeByte(encryptionKey[i] ^ encryptionKey[i - 1]);
            }
            ds.flush();

            ctx.writeAndFlush(ms).addListener(future -> {
                if (future.isSuccess()) {
                    triggerKeyExchangeComplete();
                    keySent = true;
                } else {
                    ctx.close();
                }
            });

        } catch (IOException e) {
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            long now = System.currentTimeMillis();
            long idleMillis = now - lastKeepAliveTime;

            if (idleMillis > TIMEOUT_DURATION) {
                log.debug("Session {} timed out due to inactivity", session.getSessionId());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Session {} error: {}", session.getSessionId(), cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (session == null) {
            return;
        }
        log.info("Client {} disconnected", session.getSessionId());

        session.cleanup();
        serverManager.removeSession(session.getSessionId());
    }
}
