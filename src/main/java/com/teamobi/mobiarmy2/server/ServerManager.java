package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.impl.Session;
import com.teamobi.mobiarmy2.service.IGameDataService;
import com.teamobi.mobiarmy2.service.ILeaderboardService;
import com.teamobi.mobiarmy2.ui.controllers.ServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class ServerManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);

    private final IGameDataService gameDataService;
    private final ILeaderboardService leaderboardService;
    private final IServerConfig serverConfig;

    private ServerSocket server;
    private long countClients;
    private boolean isStart;
    private boolean isMaintenanceMode;
    private final ArrayList<ISession> sessions;
    private final List<ServerListener> listeners;

    public ServerManager() {
        ApplicationContext context = ApplicationContext.getInstance();
        this.gameDataService = context.getBean(IGameDataService.class);
        this.leaderboardService = context.getBean(ILeaderboardService.class);
        this.serverConfig = context.getBean(IServerConfig.class);

        this.isMaintenanceMode = false;
        this.sessions = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    private static class SingletonHelper {
        private static final ServerManager INSTANCE = new ServerManager();
    }

    public static ServerManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public boolean isMaintenanceMode() {
        return isMaintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        isMaintenanceMode = maintenanceMode;
    }

    public void init() {
        gameDataService.loadServerData();
        gameDataService.setCache();
        leaderboardService.init();
        RoomManager.getInstance().init();
    }

    public void start() {
        logger.info("Start server!");
        isStart = true;
        try {
            server = new ServerSocket(serverConfig.getPort());
            logger.info("Server start at port: {}", serverConfig.getPort());
            while (isStart) {
                if (sessions.size() < serverConfig.getMaxClients()) {
                    try {
                        Socket socket = server.accept();
                        ISession session = new Session(++countClients, socket);
                        sessions.add(session);
                        logger.info("Accept socket client {} done!", countClients);
                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        logger.warn("Maximum number of players reached. Waiting for a slot to be free.");
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        logger.info("Stop server!");
        isStart = false;
        try {
            while (!sessions.isEmpty()) {
                ISession session = sessions.getFirst();
                session.close();
            }
            if (server != null) {
                server.close();
            }
            HikariCPManager.getInstance().closeDataSource();
            ApplicationContext.getInstance().clearDependencies();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void disconnect(Session session) {
        sessions.remove(session);
        notifyListeners();
    }

    public void sendToServer(IMessage ms) {
        for (ISession session : sessions) {
            session.sendMessage(ms);
        }
    }

    public User getUserByUserId(int userId) {
        return sessions.stream()
                .filter(session -> session != null && session.getUser() != null && session.getUser().getUserId() == userId)
                .map(ISession::getUser)
                .findFirst()
                .orElse(null);
    }

    public List<User> findWaitPlayers(int excludedPlayerId) {
        return sessions.stream()
                .filter(session -> session != null && session.getUser() != null &&
                        session.getUser().getUserId() != excludedPlayerId &&
                        session.getUser().getState() == UserState.WAITING)
                .map(ISession::getUser)
                .limit(10)
                .toList();
    }

    public void addServerListener(ServerListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (ServerListener listener : listeners) {
            listener.onUsersUpdated(sessions);
        }
    }
}
