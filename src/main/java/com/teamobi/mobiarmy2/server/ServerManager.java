package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.Impl.ServerConfig;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.dao.impl.GameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.log.ILogManager;
import com.teamobi.mobiarmy2.log.LoggerUtil;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.network.Impl.Session;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.service.Impl.GameService;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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

    private void initServerData() {
        gameService.getItemData();
        gameService.getClanShopData();
        gameService.getSpecialItemData();
        gameService.getFormulaData();
        gameService.getPaymentData();
        gameService.getMissionData();
        gameService.getLvXpData();

        //Data to set up cache
        gameService.getMapData();
        gameService.getCharacterData();
        gameService.getEquipData();
        gameService.setCaptionLevelData();

        gameService.setDefaultNvData();
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
                rooms[k] = new Room(k, i, config.getN_area(), j);
                k++;
            }
        }
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

    //Todo optimize this method
    public static short[] data(int iddb, byte nv) {
        short[] data = new short[5];
        try {
            Connection connection = HikariCPManager.getInstance().getConnection();

            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet red = statement.executeQuery("SELECT `ruongTrangBi`,`NV" + nv + "` FROM armymem WHERE id='" + iddb + "' LIMIT 1");

            red.first();
            JSONObject jobj = (JSONObject) JSONValue.parse(red.getString("NV" + nv));
            JSONArray trangBi = (JSONArray) JSONValue.parse(red.getString("ruongTrangBi"));
            red.close();
            JSONArray Jarr = (JSONArray) jobj.get("data");
            short indexS = ((Long) Jarr.get(5)).shortValue();
            if (indexS >= 0 && indexS < trangBi.size()) {
                JSONObject jobj1 = (JSONObject) trangBi.get(indexS);
                short nvId = Short.parseShort(jobj1.get("nvId").toString());
                short equipId = Short.parseShort(jobj1.get("id").toString());
                short equipType = Short.parseShort(jobj1.get("equipType").toString());
                NVData.EquipmentEntry eq = NVData.getEquipEntryById(nvId, equipType, equipId);
                data[0] = eq.arraySet[0];
                data[1] = eq.arraySet[1];
                data[2] = eq.arraySet[2];
                data[3] = eq.arraySet[3];
                data[4] = eq.arraySet[4];
            } else {
                for (byte a = 0; a < 5; a++) {
                    indexS = ((Long) Jarr.get(a)).shortValue();
                    if (indexS >= 0 && indexS < trangBi.size()) {
                        JSONObject jobj1 = (JSONObject) trangBi.get(indexS);
                        data[a] = Short.parseShort(jobj1.get("id").toString());
                    } else if (User.nvEquipDefault[nv - 1][a] != null && a != 5) {
                        data[a] = User.nvEquipDefault[nv - 1][a].id;
                    } else {
                        data[a] = -1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
