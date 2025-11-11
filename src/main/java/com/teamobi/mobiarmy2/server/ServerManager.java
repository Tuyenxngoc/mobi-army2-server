package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServerManager {
    private final ConcurrentHashMap<Long, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Long> userToSession = new ConcurrentHashMap<>();
    @Setter
    @Getter
    private boolean isMaintenanceMode = false;
    private boolean isRunning = false;

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final ServerConfig serverConfig;

    public ServerManager(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void init() {
        ApplicationContext ctx = ApplicationContext.getInstance();

        GameDataService gameDataService = ctx.getBean(GameDataService.class);
        LeaderboardService leaderboardService = ctx.getBean(LeaderboardService.class);

        gameDataService.loadServerData();
        gameDataService.setCache();
        leaderboardService.init();

        RoomManager roomManager = ctx.getBean(RoomManager.class);
        roomManager.init();

        if (serverConfig.isTet()) {
            ExchangeLimitManager exchangeLimitManager = ctx.getBean(ExchangeLimitManager.class);
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
                    .childHandler(new NettyServerInitializer())
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
        for (Session session : sessions.values()) {
            session.closeChannel();
        }

        // Đợi tất cả worker threads hoàn thành
        for (Session session : sessions.values()) {
            session.awaitTermination(5000);
        }

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

    public void addSession(Session session) {
        sessions.put(session.getSessionId(), session);
    }

    public void registerUser(User user) {
        if (user != null && user.getSession() != null) {
            userToSession.put(user.getUserId(), user.getSession().getSessionId());
        }
    }

    public void removeSession(Long sessionId) {
        Session removed = sessions.remove(sessionId);
        if (removed != null && removed.getUser() != null) {
            userToSession.remove(removed.getUser().getUserId());
        }
    }

    public void sendToServer(Message ms) {
        for (Session session : sessions.values()) {
            session.sendMessage(ms);
        }
    }

    public User getUserByUserId(int userId) {
        Long sessionId = userToSession.get(userId);
        if (sessionId == null) {
            return null;
        }
        Session session = sessions.get(sessionId);
        return session != null ? session.getUser() : null;
    }

    public List<User> findWaitPlayers(int excludedUserId) {
        List<User> result = new ArrayList<>(10);
        for (var entry : userToSession.entrySet()) {
            if (result.size() >= 10) break;
            int userId = entry.getKey();
            if (userId == excludedUserId) continue;

            Long sessionId = entry.getValue();
            if (sessionId == null) continue;

            Session session = sessions.get(sessionId);
            if (session == null) continue;

            User user = session.getUser();
            if (user == null || !user.isLogged()) continue;

            if (user.getState() == UserState.WAITING) {
                result.add(user);
            }
        }
        return result;
    }
}
