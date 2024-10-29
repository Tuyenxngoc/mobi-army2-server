package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.server.ServerManager;

/**
 * @author tuyen
 */
public class MobiArmy2 {

    public static void main(String[] args) {
        ServerManager serverManager = ServerManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop, "serverShutdownHook"));
        serverManager.init();
        serverManager.start();
    }

}
