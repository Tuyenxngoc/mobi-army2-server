package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.server.ServerManager;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CountDownManager {

    private final FightManager fightManager;
    public byte second;
    private byte countTime;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> countTask;

    public CountDownManager(FightManager fightManager, byte countTime) {
        this.fightManager = fightManager;
        this.second = 0;
        this.countTime = countTime;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void setCountTime(byte count) {
        countTime = count;
    }

    public void resetCount() {
        stopCount();
        second = countTime;

        ServerManager.getInstance().logger().logMessage("Second count= " + second);
        countTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                second--;
                if (second == -10) {
                    fightManager.countOut();
                    stopCount();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ServerManager.getInstance().logger().logMessage("Stop count!");
                Thread.currentThread().interrupt();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stopCount() {
        if (countTask != null && !countTask.isDone()) {
            countTask.cancel(true);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
