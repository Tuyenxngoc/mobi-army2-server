package com.teamobi.mobiarmy2.app;

import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.ui.ServerUI;

public class MobiArmy2 {
    public static void main(String[] args) {
        BeanRegistry.registerBeans();
        ServerManager serverManager = ApplicationContext.getInstance().getBean(ServerManager.class);
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop, "ServerShutdownHook"));

        new Thread(() -> {
            serverManager.init();
            serverManager.start();
        }, "Main").start();

        new Thread(() -> ServerUI.launchUI(args), "ServerUI").start();
    }
}
