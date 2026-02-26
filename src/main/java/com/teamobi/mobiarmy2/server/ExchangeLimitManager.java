package com.teamobi.mobiarmy2.server;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExchangeLimitManager {
    private static final int[] MAX_GOLD_PER_VIP = {10, 5, 2};
    private static final int[] MAX_SILVER_PER_VIP = {15, 10, 5};
    private static final int CYCLE_HOURS = 3;
    private static final int RESET_MINUTE = 15;

    private final AtomicInteger[] goldCounters = initCounters(MAX_GOLD_PER_VIP.length);
    private final AtomicInteger[] silverCounters = initCounters(MAX_SILVER_PER_VIP.length);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void init() {
        scheduleNextReset();
    }

    private AtomicInteger[] initCounters(int size) {
        AtomicInteger[] counters = new AtomicInteger[size];
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new AtomicInteger();
        }
        return counters;
    }

    public boolean isGoldLimitReached(int vipLevel) {
        return goldCounters[vipLevel].get() >= MAX_GOLD_PER_VIP[vipLevel];
    }

    public void incrementGoldCount(int vipLevel) {
        goldCounters[vipLevel].getAndIncrement();
    }

    public boolean isSilverLimitReached(int vipLevel) {
        return silverCounters[vipLevel].get() >= MAX_SILVER_PER_VIP[vipLevel];
    }

    public void incrementSilverCount(int vipLevel) {
        silverCounters[vipLevel].getAndIncrement();
    }

    public void resetCounters() {
        resetCounterArray(goldCounters);
        resetCounterArray(silverCounters);
        log.info("Transaction counters have been reset.");
    }

    private void resetCounterArray(AtomicInteger[] counters) {
        for (AtomicInteger counter : counters) {
            counter.set(0);
        }
    }

    private void scheduleNextReset() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextReset = calculateNextResetTime(now);
        long delay = ChronoUnit.MILLIS.between(now, nextReset);

        if (delay < 0) {
            delay = 0;
        }

        scheduler.schedule(() -> {
            try {
                resetCounters();
            } catch (Exception e) {
                log.error("Error in reset task", e);
            } finally {
                scheduleNextReset();
            }
        }, delay, TimeUnit.MILLISECONDS);

        Duration duration = Duration.ofMillis(delay);
        log.info("Next reset scheduled at: {} (delay: {} minutes)",
                nextReset.format(DateTimeFormatter.ofPattern("HH:mm:ss")), duration.toMinutes());
    }

    private ZonedDateTime calculateNextResetTime(ZonedDateTime now) {
        // Find the nearest previous cycle start hour (0, 3, 6, ...)
        int currentHour = now.getHour();
        int baseHour = (currentHour / CYCLE_HOURS) * CYCLE_HOURS;

        ZonedDateTime candidate = now.withHour(baseHour)
                .withMinute(RESET_MINUTE)
                .withSecond(0)
                .withNano(0);

        // If 'now' has already passed the candidate time, move to the next cycle
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusHours(CYCLE_HOURS);
        }

        return candidate;
    }
}