package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.ICountdownTimer;
import com.teamobi.mobiarmy2.fight.IFightManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author tuyen
 */
public class CountdownTimer implements ICountdownTimer {

    private final IFightManager fightManager;
    private Timer timer;
    private int remainingTime;
    private final int countdownTime;

    public CountdownTimer(IFightManager fightManager, int countdownTime) {
        this.fightManager = fightManager;
        this.countdownTime = countdownTime;
        this.remainingTime = countdownTime;
    }

    @Override
    public void start() {
        if (timer != null) {
            timer.cancel(); // Hủy bỏ bộ đếm trước đó nếu nó đang chạy
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingTime > 0) {
                    remainingTime--;
                    System.out.println("Thời gian còn lại: " + remainingTime + " giây");
                } else {
                    timer.cancel(); // Dừng bộ đếm khi thời gian hết
                    timeUp();
                }
            }
        }, 0, 1000); // Cập nhật mỗi giây
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void reset() {
        stop();
        remainingTime = countdownTime;
        start();
    }

    private void timeUp() {
        System.out.println("Hết thời gian!");
        fightManager.onTimeUp();
    }

}
