package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.network.NettyServerInitializer;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.service.GameDataService;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerManager {
    @Setter
    @Getter
    private boolean isMaintenanceMode = false;
    private boolean isRunning = false;

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final GameDataService gameDataService;
    private final LeaderboardService leaderboardService;
    private final RoomManager roomManager;
    private final ExchangeLimitManager exchangeLimitManager;
    private final ServerConfig serverConfig;
    private final SessionRegistry sessionRegistry;

    public ServerManager(ServerConfig serverConfig,
                         GameDataService gameDataService,
                         LeaderboardService leaderboardService,
                         RoomManager roomManager,
                         ExchangeLimitManager exchangeLimitManager,
                         SessionRegistry sessionRegistry) {
        this.serverConfig = serverConfig;
        this.gameDataService = gameDataService;
        this.leaderboardService = leaderboardService;
        this.roomManager = roomManager;
        this.exchangeLimitManager = exchangeLimitManager;
        this.sessionRegistry = sessionRegistry;
    }

    public void init() {
        gameDataService.loadServerData();
        gameDataService.setCache();
        leaderboardService.init();

        roomManager.init();

        if (serverConfig.isTet()) {
            exchangeLimitManager.init();
        }
    }

    public void start() {
        if (isRunning) {
            log.warn("Server is already running.");
            return;
        }
        isRunning = true;

        log.info("Starting server...");
        bossGroup = new NioEventLoopGroup(1); // nhận kết nối
        workerGroup = new NioEventLoopGroup(); // xử lý IO

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //todo app: còn thiếu connectionBlockerService + factory tạo Session/handler
                    .childHandler(new NettyServerInitializer(null, sessionRegistry))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            int port = serverConfig.getPort();
            serverChannel = bootstrap.bind(port).sync().channel();
            log.info("Netty Server started on port {}", port);

            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Server thread interrupted", e);
        }
    }

    public void stop() {
        if (!isRunning) {
            log.warn("Server is not running.");
            return;
        }
        isRunning = false;

        log.info("Stopping server...");

        // Đóng tất cả session
        sessionRegistry.closeAllSessions();

        //Tắt executor dùng Virtual Thread
        Session.shutdownExecutor();

        // Đóng channel chính
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close().syncUninterruptibly();
        }

        // Đóng EventLoopGroups
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        log.info("Server stopped successfully.");
    }
}
