package com.teamobi.mobiarmy2.server;

public class BangXHManager {

    private static BangXHManager instance;
    public boolean isComplete = true;

    public synchronized static BangXHManager getInstance() {
        if (instance == null) {
            instance = new BangXHManager();
        }
        return instance;
    }


}
