package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.server.ServerManager;

public class CountDownManager implements Runnable {

    private final FightManager fightManager;
    public byte second;
    private byte countTime;
    private boolean startCount;
    private Thread countThread;

    public CountDownManager(FightManager fightManager, byte countTime) {
        this.fightManager = fightManager;
        this.startCount = false;
        this.second = 0;
        this.countTime = countTime;
        this.countThread = null;
    }

    public void setCountTime(byte count) {
        countTime = count;
    }

    public void resetCount() {
        stopCount();
        second = countTime;
        startCount = true;

        ServerManager.getInstance().logger().logMessage("SecondCount= " + second);
        countThread = new Thread(this);
        countThread.start();
    }

    public void stopCount() {
        startCount = false;
        if (countThread != null && countThread.isAlive()) {
            countThread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            while (startCount) {
                Thread.sleep(1000L);
                second--;
                if (second == -10) {
                    fightManager.countOut();
                    startCount = false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ServerManager.getInstance().logger().logMessage("Stop count!");
        }
    }
}
