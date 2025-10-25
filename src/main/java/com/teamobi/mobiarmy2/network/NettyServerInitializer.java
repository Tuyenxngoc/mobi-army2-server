package com.teamobi.mobiarmy2.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        long sessionId = System.nanoTime();
        SessionHandler sessionHandler = new SessionHandler(sessionId);

        // Initial pipeline with plain encoder/decoder
        ch.pipeline().addLast("decoder-plain", new PlainMessageDecoder());
        ch.pipeline().addLast("session", sessionHandler);
        ch.pipeline().addLast("encoder-plain", new PlainMessageEncoder());

        // Callback replace secure
        sessionHandler.setOnKeyExchangeComplete(() -> {
            ch.pipeline().replace("decoder-plain", "decoder-secure", new MessageDecoder(sessionHandler));
            ch.pipeline().replace("encoder-plain", "encoder-secure", new MessageEncoder(sessionHandler));
        });
    }
}
