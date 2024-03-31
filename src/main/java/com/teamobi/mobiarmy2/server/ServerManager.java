package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.Impl.ServerConfig;
import com.teamobi.mobiarmy2.log.ILogManager;
import com.teamobi.mobiarmy2.log.LoggerUtil;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.network.Impl.MessageHandler;
import com.teamobi.mobiarmy2.network.Impl.Session;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerManager {

    private static ServerManager instance;

    private final IServerConfig config;
    private final ILogManager log;

    private ServerSocket server;

    private long countClients;
    private boolean isStart;

    private final ArrayList<User> users = new ArrayList<>();

    public ServerManager() {
        this.config = new ServerConfig("army2.properties");
        this.log = new LoggerUtil(config.isDebug());
    }

    public ILogManager logger() {
        return log;
    }

    public static synchronized ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public void init() {
    }

    public void start() {
        try {
            isStart = true;
            server = new ServerSocket(config.getPort());
            log.logMessage("The server starts at port: " + config.getPort());
            while (isStart) {
                try {
                    Socket socket = server.accept();
                    Session session = new Session(socket);
                    User user = new User(session);
                    IUserService userService = new UserService(user);
                    IMessageHandler messageHandler = new MessageHandler(userService);
                    session.setHandler(messageHandler);
                    users.add(user);
                    countClients++;
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
            users.clear();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
