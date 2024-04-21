package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.Impl.ServerConfig;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.dao.impl.GameDao;
import com.teamobi.mobiarmy2.log.ILogManager;
import com.teamobi.mobiarmy2.log.LoggerUtil;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.network.Impl.Session;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.service.Impl.GameService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author tuyen
 */
public class ServerManager {

    private static ServerManager instance;

    private final IGameService gameService;
    private final IServerConfig config;
    private final ILogManager log;

    private ServerSocket server;
    private long countClients;
    private boolean isStart;
    private Room[] rooms;
    private final ArrayList<ISession> users = new ArrayList<>();

    public ServerManager() {
        IGameDao gameDao = new GameDao();
        this.gameService = new GameService(gameDao);

        this.config = new ServerConfig("army2.properties");
        this.log = new LoggerUtil(config.isDebug());
    }

    public static synchronized ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public ILogManager logger() {
        return log;
    }

    public IServerConfig config() {
        return config;
    }

    public void init() {
        isStart = false;
        initServerData();
        setCache();
        initRooms();
    }

    private void initRooms() {
        rooms = new Room[10];
    }

    private void initServerData() {
        gameService.getItemData();
        gameService.getClanShopData();
        gameService.getSpecialItemData();
        gameService.getFormulaData();
        gameService.getPaymentData();
        gameService.getMissionData();

        gameService.setDefaultNvData();
    }

    private void setCache() {
        gameService.setCacheMaps();
        gameService.setCacheCharacters();
        gameService.setCacheCaptionLevels();
        gameService.setCachePlayerImages();
        gameService.setCacheMapIcons();
    }

    public void start() {
        log.logMessage("Start server!");
        try {
            isStart = true;
            server = new ServerSocket(config.getPort());
            log.logMessage("The server starts at port: " + config.getPort());
            while (isStart) {
                try {
                    Socket socket = server.accept();
                    ISession session = new Session(++countClients, socket);
                    users.add(session);
                    log.logMessage("Accept socket client " + countClients + " done!");
                } catch (Exception e) {
                    //Empty catch block
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        log.logMessage("Stop server");
        isStart = false;
        countClients = 0;
        try {
            users.clear();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(Session session) {
        users.remove(session);
        countClients--;
    }

    public void sendToServer(Message ms) {
        synchronized (users) {
            for (ISession session : users) {
                session.sendMessage(ms);
            }
        }
    }

    public User getUser(int userId) {
        synchronized (users) {
            for (ISession session : users) {
                if (session != null && session.getUser() != null) {
                    User user = session.getUser();
                    if (user.getId() == userId) {
                        return user;
                    }
                }
            }
        }
        return null;
    }
}
