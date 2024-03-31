package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.army2.server.ServerManager;

public class MobiArmy2 {

    public static void main(String[] args) {
        System.out.println("Start server!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Server!");
            ServerManager.stop();
        }));
        ServerManager.init();
        ServerManager.start();
    }

}
