package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.impl.ServerConfig;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.dao.impl.GameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Room;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.impl.Session;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.service.impl.GameService;
import lombok.Getter;
import lombok.Setter;
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

    //tmp variable
    public static byte maxPlayers = 8;

    private final IGameService gameService;
    @Getter
    private final IServerConfig config;

    private ServerSocket server;
    private long countClients;
    private boolean isStart;
    @Getter
    @Setter
    private boolean isMaintenanceMode = false;
    @Getter
    private Room[] rooms;
    private final ArrayList<ISession> users = new ArrayList<>();
    private final List<ServerListener> listeners = new ArrayList<>();

    public ServerManager() {
        IGameDao gameDao = new GameDao();
        this.gameService = new GameService(gameDao);

        this.config = new ServerConfig();
    }

    private static class SingletonHelper {
        private static final ServerManager INSTANCE = new ServerManager();
    }

    public static ServerManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public void init() {
        initServerData();
        setCache();
        initRooms();
        initRankings();
    }

    private void initServerData() {
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
        byte[] roomQuantities = config.getRoomQuantity();
        int totalRooms = 0;

        for (int quantity : roomQuantities) {
            totalRooms += quantity;
        }

        rooms = new Room[totalRooms];
        byte index = 0;

        for (byte type = 0; type < roomQuantities.length; type++) {
            int minXu = config.getRoomMinXu()[type];
            int maxXu = config.getRoomMaxXu()[type];
            byte minMap = config.getRoomMinMap()[type];
            byte maxMap = config.getRoomMaxMap()[type];
            byte numArea = config.getNumArea();
            byte maxPlayerFight = config.getMaxPlayerFight();
            byte numPlayerInitRoom = config.getNumPlayerInitRoom();
            byte roomIconType = config.getRoomIconType();

            for (byte roomCount = 0; roomCount < roomQuantities[type]; roomCount++) {
                byte[] mapCanSelected = null;
                boolean isContinuous = false;
                if (type == 5) {
                    mapCanSelected = config.getBossRoomMapLimit()[roomCount];
                    if (roomCount == 9) {
                        isContinuous = true;
                    }
                }

                rooms[index] = new Room(index, type, minXu, maxXu, minMap, maxMap, mapCanSelected, isContinuous, numArea, maxPlayerFight, numPlayerInitRoom, roomIconType);
                index++;
            }
        }
    }

    private void initRankings() {
        LeaderboardManager.getInstance().init();
    }

    public void start() {
        logger.info("Start server!");
        isStart = true;
        try {
            server = new ServerSocket(config.getPort());
            logger.info("Server start at port: {}", config.getPort());
            while (isStart) {
                if (users.size() < config.getMaxClients()) {
                    try {
                        Socket socket = server.accept();
                        ISession session = new Session(++countClients, socket);
                        users.add(session);
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
            while (!users.isEmpty()) {
                ISession session = users.getFirst();
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

    public synchronized void disconnect(Session session) {
        users.remove(session);
        notifyListeners();
    }

    public void sendToServer(IMessage ms) {
        for (ISession session : users) {
            session.sendMessage(ms);
        }
    }

    public User getUserByPlayerId(int playerId) {
        return users.stream()
                .filter(session -> session != null && session.getUser() != null && session.getUser().getPlayerId() == playerId)
                .map(ISession::getUser)
                .findFirst()
                .orElse(null);
    }

    public List<User> findWaitPlayers(int excludedPlayerId) {
        return users.stream()
                .filter(session -> session != null && session.getUser() != null &&
                        session.getUser().getPlayerId() != excludedPlayerId &&
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
            listener.onUsersUpdated(users);
        }
    }
}
