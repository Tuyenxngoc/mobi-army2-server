package com.teamobi.mobiarmy2.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        long sessionId = System.nanoTime();
        SessionHandler sessionHandler = new SessionHandler(sessionId);

        // Initial pipeline with plain encoder/decoder
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
