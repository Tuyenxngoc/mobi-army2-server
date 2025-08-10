package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.service.ConnectionBlockerService;
import com.teamobi.mobiarmy2.service.GameDataService;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServerManager {
    private final GameDataService gameDataService;
    private final LeaderboardService leaderboardService;
    private final ConnectionBlockerService connectionBlockerService;
    private final ArrayList<Session> sessions = new ArrayList<>();
    private ServerSocket server;
    private long countClients;
    private boolean isStart;
    @Setter
    @Getter
    private boolean isMaintenanceMode = false;

    public ServerManager(GameDataService gameDataService, LeaderboardService leaderboardService, ConnectionBlockerService connectionBlockerService) {
        this.gameDataService = gameDataService;
        this.leaderboardService = leaderboardService;
        this.connectionBlockerService = connectionBlockerService;
    }

    public void init() {
        gameDataService.loadServerData();
        gameDataService.setCache();
        leaderboardService.init();
        ApplicationContext.getInstance()
                .getBean(RoomManager.class).init();
        ExchangeLimitManager.init();
    }

    public void start() {
        log.info("Start server!");
        isStart = true;
        try {
            ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
            server = new ServerSocket(serverConfig.getPort());
            log.info("Server start at port: {}", serverConfig.getPort());
            while (isStart) {
                if (sessions.size() < serverConfig.getMaxClients()) {
                    try {
                        Socket socket = server.accept();

                        String ipAddress = socket.getInetAddress().getHostAddress();
                        if (connectionBlockerService.isIpBlocked(ipAddress)) {
                            log.warn("IP {} is blocked due to too many connections.", ipAddress);
                            socket.close();
                            continue;
                        }

                        Session session = new Session(++countClients, socket);
                        sessions.add(session);

                        connectionBlockerService.incrementIpConnectionCount(ipAddress);

                        log.info("Accept socket client {} done!", countClients);
                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        log.warn("Maximum number of players reached. Waiting for a slot to be free.");
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        log.info("Stop server!");
        isStart = false;
        try {
            while (!sessions.isEmpty()) {
                Session session = sessions.getFirst();
                session.close();
            }
            if (server != null) {
                server.close();
            }

            ApplicationContext.getInstance()
                    .getBean(HikariCPManager.class)
                    .closeDataSource();

            ApplicationContext.getInstance()
                    .getBean(RedisConnectionManager.class)
                    .close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void disconnect(Session session) {
        String ipAddress = session.getIPAddress();
        sessions.remove(session);
        connectionBlockerService.decrementIpConnectionCount(ipAddress);
    }

    public void sendToServer(Message ms) {
        for (Session session : sessions) {
            session.sendMessage(ms);
        }
    }

    public User getUserByUserId(int userId) {
        return sessions.stream()
                .filter(session -> session != null && session.getUser() != null && session.getUser().getUserId() == userId)
                .map(Session::getUser)
                .findFirst()
                .orElse(null);
    }

    public List<User> findWaitPlayers(int excludedUserId) {
        return sessions.stream()
                .filter(session -> session != null && session.getUser() != null &&
                        session.getUser().getUserId() != excludedUserId &&
                        session.getUser().getState() == UserState.WAITING)
                .map(Session::getUser)
                .limit(10)
                .toList();
    }
}
