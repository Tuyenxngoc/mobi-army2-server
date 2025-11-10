package com.teamobi.mobiarmy2.server;

import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExchangeLimitManager {
    private static final int NUM_DAILY_RESETS = 3;
    private static final int[] MAX_GOLD_PER_VIP = {10, 5, 2};
    private static final int[] MAX_SILVER_PER_VIP = {15, 10, 5};

    private final AtomicInteger[] goldCounters = initCounters(MAX_GOLD_PER_VIP.length);
    private final AtomicInteger[] silverCounters = initCounters(MAX_SILVER_PER_VIP.length);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void init() {
        scheduleInitialResets();
        scheduleMidnightTask();
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

    private void scheduleInitialResets() {
        long remainingSeconds = calculateDelayUntilMidnight();
        generateAndScheduleResets(0, remainingSeconds);
    }

    private void scheduleMidnightTask() {
        long delayUntilMidnight = calculateDelayUntilMidnight();
        scheduler.scheduleAtFixedRate(
                this::scheduleDailyResets,
                delayUntilMidnight,
                86400,
                TimeUnit.SECONDS
        );
        log.info("Scheduling daily reset task with delay of {} seconds until midnight.", delayUntilMidnight);
    }

    private long calculateDelayUntilMidnight() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return ChronoUnit.SECONDS.between(now, nextMidnight);
    }

    private void scheduleDailyResets() {
        generateAndScheduleResets(0, 86400);
    }

    private void generateAndScheduleResets(long minDelay, long maxDelay) {
        Set<Long> delays = new HashSet<>();
        Random random = new Random();
        ZoneId zone = ZoneId.systemDefault();

        while (delays.size() < NUM_DAILY_RESETS) {
            long delay = minDelay + (long) (random.nextDouble() * (maxDelay - minDelay));
            delays.add(delay);
        }

        Set<String> resetTimes = new TreeSet<>();
        ZonedDateTime now = ZonedDateTime.now(zone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Long delay : delays) {
            ZonedDateTime resetTime = now.plusSeconds(delay);
            String formattedTime = resetTime.format(formatter);
            resetTimes.add(formattedTime);

            scheduler.schedule(
                    this::resetCounters,
                    delay,
                    TimeUnit.SECONDS
            );
        }

        log.info("Transaction counters will reset at the following times: {}", resetTimes);
    }
}