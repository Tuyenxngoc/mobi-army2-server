package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.Impl.ServerConfig;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.dao.impl.GameDao;
import com.teamobi.mobiarmy2.log.ILogManager;
import com.teamobi.mobiarmy2.log.LoggerUtil;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Session;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.service.Impl.GameService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerManager {

    private static ServerManager instance;

    private final IGameService gameService;
    private final IServerConfig config;
    private final ILogManager log;

    private ServerSocket server;

    private long countClients;
    private boolean isStart;

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

    public void init() {
        log.logMessage("Load map data");
        setCache(0);

        log.logMessage("Load NV data");
        setCache(1);

        log.logMessage("Load caption level");
        setCache(2);

        log.logMessage("Load player images");
        setCache(3);
        setCache(4);
    }

    private void setCache(int i) {
        switch (i) {
            case 0 -> gameService.setCacheMaps();
            case 1 -> gameService.setCacheCharacters();
            case 2 -> gameService.setCacheCaptionLevels();
            case 3 -> gameService.setCachePlayerImages();
            case 4 -> gameService.setCacheMapIcons();
        }
    }

    public void start() {
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
}