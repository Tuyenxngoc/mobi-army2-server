package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.server.ServerManager;

public class MobiArmy2 {

    public static void main(String[] args) {
        ServerManager serverManager = ServerManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop));
        serverManager.init();
        serverManager.start();
    }

}
