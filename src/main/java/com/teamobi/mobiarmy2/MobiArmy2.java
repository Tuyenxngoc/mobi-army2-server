package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.ui.ServerUI;

/**
 * @author tuyen
 */
public class MobiArmy2 {

    public static void main(String[] args) {
        ServerManager serverManager = ServerManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop, "serverShutdownHook"));

        new Thread(() -> {
            serverManager.init();
            serverManager.start();
        }, "Main").start();

        new Thread(() -> ServerUI.launchUI(args), "Server UI").start();
    }
}
