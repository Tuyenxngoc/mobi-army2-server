package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.service.ConnectionBlockerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ConnectionLimitHandler extends ChannelInboundHandlerAdapter {
    private final ConnectionBlockerService connectionBlockerService;

    public ConnectionLimitHandler(ConnectionBlockerService connectionBlockerService) {
        this.connectionBlockerService = connectionBlockerService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

        if (!connectionBlockerService.tryIncrementConnection(ip)) {
            log.warn("Connection from IP {} rejected: too many connections", ip);
            ctx.close();
            return;
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        connectionBlockerService.decrementConnection(ip);

        super.channelInactive(ctx);
    }

}
