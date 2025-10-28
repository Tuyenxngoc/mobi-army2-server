package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.ConnectionBlockerService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ApplicationContext context = ApplicationContext.getInstance();
        SessionHandler sessionHandler = new SessionHandler(context.getBean(ServerManager.class));
        ConnectionBlockerService connectionBlockerService = context.getBean(ConnectionBlockerService.class);

        // Initial pipeline with plain encoder/decoder
        ch.pipeline().addLast("conn-limit", new ConnectionLimitHandler(connectionBlockerService));
        ch.pipeline().addLast("idle", new IdleStateHandler(1, 0, 0, TimeUnit.MINUTES));
        ch.pipeline().addLast("decoder-plain", new PlainMessageDecoder());
        ch.pipeline().addLast("encoder-plain", new PlainMessageEncoder());
        ch.pipeline().addLast("session", sessionHandler);

        // Callback replace secure
        sessionHandler.setOnKeyExchangeComplete(() -> {
            ch.pipeline().replace("decoder-plain", "decoder-secure", new MessageDecoder(sessionHandler));
            ch.pipeline().replace("encoder-plain", "encoder-secure", new MessageEncoder(sessionHandler));
        });
    }
}
