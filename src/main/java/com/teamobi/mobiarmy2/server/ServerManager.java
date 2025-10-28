package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.NettyServerInitializer;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.service.GameDataService;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServerManager {
    private final GameDataService gameDataService;
    private final LeaderboardService leaderboardService;
    private final ConcurrentHashMap<Long, Session> sessions = new ConcurrentHashMap<>();
    @Setter
    @Getter
    private boolean isMaintenanceMode = false;

    public ServerManager(GameDataService gameDataService, LeaderboardService leaderboardService) {
        this.gameDataService = gameDataService;
        this.leaderboardService = leaderboardService;
    }

    public void init() {
        gameDataService.loadServerData();
        gameDataService.setCache();
        leaderboardService.init();
        ApplicationContext.getInstance().getBean(RoomManager.class).init();
        ExchangeLimitManager.init();
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // nhận kết nối
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // xử lý IO

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            int port = ApplicationContext.getInstance().getBean(ServerConfig.class).getPort();
            ChannelFuture f = bootstrap.bind(port).sync();
            log.info("Netty Server started on port {}", port);

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Server thread interrupted", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {

        ApplicationContext.getInstance().getBean(HikariCPManager.class).closeDataSource();
    }

    public void addSession(Session session) {
        sessions.put(session.getSessionId(), session);
    }

    public void removeSession(Long sessionId) {
        sessions.remove(sessionId);
    }

    public void sendToServer(Message ms) {
        for (Session session : sessions.values()) {
            session.sendMessage(ms);
        }
    }

    public User getUserByUserId(int userId) {
        return null;
    }

    public List<User> findWaitPlayers(int excludedUserId) {
        return null;
    }
}
