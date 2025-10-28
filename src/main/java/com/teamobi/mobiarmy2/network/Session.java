package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.entity.User;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Session {
    @Getter
    private final long sessionId;
    private final Channel channel;

    @Getter
    private final String ipAddress;

    @Setter
    private String platform;

    @Setter
    @Getter
    private String version;

    @Setter
    @Getter
    private byte provider = -1;

    @Setter
    @Getter
    private String agent;

    @Getter
    @Setter
    private User user;

    private final MessageRouter messageRouter;

    public Session(long sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.ipAddress = channel.remoteAddress().toString();
        this.messageRouter = new MessageRouter(null);
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

    public void handleMessage(Message msg) {
        messageRouter.onMessage(msg);
    }

    public void close() {

    }

    public void initMessageHandlers() {

    }
}
