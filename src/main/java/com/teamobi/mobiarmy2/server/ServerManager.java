package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.Impl.ServerConfig;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.dao.impl.GameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.log.ILogManager;
import com.teamobi.mobiarmy2.log.LoggerUtil;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.network.Impl.Session;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.service.Impl.GameService;
import lombok.Getter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class ServerManager {

    private static volatile ServerManager instance;

    private final IGameService gameService;
    private final IServerConfig config;
    private final ILogManager log;

    private ServerSocket server;
    private long countClients;
    private boolean isStart;
    @Getter
    private Room[] rooms;
    private final ArrayList<ISession> users = new ArrayList<>();

    public ServerManager() {
        IGameDao gameDao = new GameDao();
        this.gameService = new GameService(gameDao);

        this.config = new ServerConfig(CommonConstant.ARMY_2_PROPERTIES);
        this.log = new LoggerUtil(config.isDebug());
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    instance = new ServerManager();
                }
            }
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
        initServerData();
        setCache();
        initRooms();
        initRankings();
    }

    private void initServerData() {
        //Data to set up cache
        gameService.getMapData();
        gameService.getCharacterData();
        gameService.getEquipData();
        gameService.setCaptionLevelData();

        gameService.getItemData();
        gameService.getClanShopData();
        gameService.getSpecialItemData();
        gameService.getFormulaData();
        gameService.getPaymentData();
        gameService.getMissionData();
        gameService.getLvXpData();
        gameService.getFabricateItemData();
    }

    private void setCache() {
        gameService.setCacheMaps();
        gameService.setCacheCharacters();
        gameService.setCacheCaptionLevels();
        gameService.setCachePlayerImages();
        gameService.setCacheMapIcons();
    }

    private void initRooms() {
        rooms = new Room[config.getnRoomAll()];
        config.setRoomTypeStartNum(new int[config.getRoomTypes().length]);
        int k = 0;
        for (int i = 0; i < config.getRoomTypes().length; i++) {
            for (int j = 0; j < config.getnRoom()[i]; j++) {
                if (j == 0) {
                    config.getRoomTypeStartNum()[i] = k;
                }
                rooms[k] = new Room(k, i, config.getNArea(), j);
                k++;
            }
        }
    }

    private void initRankings() {
        LeaderboardManager.getInstance().init();
    }

    public void start() {
        log.logMessage("Start server!");
        try {
            isStart = true;
            server = new ServerSocket(config.getPort());
            log.logMessage("Server start at port: " + config.getPort());
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
        try {
            while (users.size() > 0) {
                ISession session = users.get(0);
                session.close();
            }
            if (server != null) {
                server.close();
            }
            HikariCPManager.getInstance().closeDataSource();
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

    public User getUserByPlayerId(int playerId) {
        synchronized (users) {
            return users.stream()
                    .filter(session -> session != null && session.getUser() != null && session.getUser().getPlayerId() == playerId)
                    .map(ISession::getUser)
                    .findFirst()
                    .orElse(null);
        }
    }

    public List<User> findWaitPlayers(int excludedPlayerId) {
        synchronized (users) {
            return users.stream()
                    .filter(session -> session != null && session.getUser() != null)
                    .map(ISession::getUser)
                    .filter(user -> UserState.WAITING.equals(user.getState()) && user.getPlayerId() != excludedPlayerId)
                    .collect(Collectors.toList());
        }
    }
}
