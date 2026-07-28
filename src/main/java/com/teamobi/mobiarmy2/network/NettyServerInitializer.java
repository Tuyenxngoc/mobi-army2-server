package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.network.codec.MessageDecoder;
import com.teamobi.mobiarmy2.network.codec.MessageEncoder;
import com.teamobi.mobiarmy2.network.codec.PlainMessageDecoder;
import com.teamobi.mobiarmy2.network.codec.PlainMessageEncoder;
import com.teamobi.mobiarmy2.server.SessionRegistry;
import com.teamobi.mobiarmy2.service.ConnectionBlockerService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private final ConnectionBlockerService connectionBlockerService;
    private final SessionRegistry sessionRegistry;

    public NettyServerInitializer(ConnectionBlockerService connectionBlockerService, SessionRegistry sessionRegistry) {
        this.connectionBlockerService = connectionBlockerService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        SessionHandler sessionHandler = new SessionHandler(sessionRegistry);

        // Initial pipeline with plain encoder/decoder
        ch.pipeline()
                .addLast("conn-limit", new ConnectionLimitHandler(connectionBlockerService))
                .addLast("idle", new IdleStateHandler(1, 0, 0, TimeUnit.MINUTES))
                .addLast("decoder-plain", new PlainMessageDecoder())
                .addLast("encoder-plain", new PlainMessageEncoder())
                .addLast("session", sessionHandler);

        // Callback replace secure
        sessionHandler.setOnKeyExchangeComplete(() -> {
            ch.pipeline().replace("decoder-plain", "decoder-secure", new MessageDecoder(sessionHandler));
            ch.pipeline().replace("encoder-plain", "encoder-secure", new MessageEncoder(sessionHandler));
        });
    }
}
