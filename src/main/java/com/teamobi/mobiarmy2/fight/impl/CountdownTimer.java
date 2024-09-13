package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.fight.ICountdownTimer;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer implements ICountdownTimer {
    private Timer timer;
    private int remainingTime;
    private final int countdownTime;
    private final Runnable onTimeUpCallback;

    public CountdownTimer(int countdownTime, Runnable onTimeUpCallback) {
        this.countdownTime = countdownTime;
        this.remainingTime = countdownTime;
        this.onTimeUpCallback = onTimeUpCallback;
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
        if (onTimeUpCallback != null) {
            onTimeUpCallback.run();
        }
    }
}