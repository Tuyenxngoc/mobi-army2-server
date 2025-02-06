package com.teamobi.mobiarmy2.server;

import java.util.concurrent.atomic.AtomicInteger;

public class ExchangeLimitManager {
    // Gold limits for VIP 1-3
    private static final int[] MAX_GOLD_PER_VIP = {100, 50, 20};
    // Silver limits for VIP 1-3
    private static final int[] MAX_SILVER_PER_VIP = {150, 100, 50};

    private static final AtomicInteger[] goldCounters = initCounters(MAX_GOLD_PER_VIP.length);
    private static final AtomicInteger[] silverCounters = initCounters(MAX_SILVER_PER_VIP.length);

    private static AtomicInteger[] initCounters(int size) {
        AtomicInteger[] counters = new AtomicInteger[size];
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new AtomicInteger();
        }
        return counters;
    }

    public static boolean isGoldLimitReached(int vipLevel) {
        return goldCounters[vipLevel].get() >= MAX_GOLD_PER_VIP[vipLevel];
    }

    public static void incrementGoldCount(int vipLevel) {
        goldCounters[vipLevel].getAndIncrement();
    }

    public static boolean isSilverLimitReached(int vipLevel) {
        return silverCounters[vipLevel].get() >= MAX_SILVER_PER_VIP[vipLevel];
    }

    public static void incrementSilverCount(int vipLevel) {
        silverCounters[vipLevel].getAndIncrement();
    }

    public static void resetCounters() {
        resetCounterArray(goldCounters);
        resetCounterArray(silverCounters);
    }

    private static void resetCounterArray(AtomicInteger[] counters) {
        for (AtomicInteger counter : counters) {
            counter.set(0);
        }
    }
}