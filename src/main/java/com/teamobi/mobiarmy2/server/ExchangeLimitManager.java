package com.teamobi.mobiarmy2.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ExchangeLimitManager {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeLimitManager.class);

    private static final int NUM_DAILY_RESETS = 3;
    private static final int[] MAX_GOLD_PER_VIP = {10, 5, 2};
    private static final int[] MAX_SILVER_PER_VIP = {15, 10, 5};

    private static final AtomicInteger[] goldCounters = initCounters(MAX_GOLD_PER_VIP.length);
    private static final AtomicInteger[] silverCounters = initCounters(MAX_SILVER_PER_VIP.length);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void init() {
        scheduleInitialResets();
        scheduleMidnightTask();
    }

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
        logger.info("Transaction counters have been reset.");
    }

    private static void resetCounterArray(AtomicInteger[] counters) {
        for (AtomicInteger counter : counters) {
            counter.set(0);
        }
    }

    private static void scheduleInitialResets() {
        long remainingSeconds = calculateDelayUntilMidnight();
        generateAndScheduleResets(0, remainingSeconds);
    }

    private static void scheduleMidnightTask() {
        long delayUntilMidnight = calculateDelayUntilMidnight();
        scheduler.scheduleAtFixedRate(
                ExchangeLimitManager::scheduleDailyResets,
                delayUntilMidnight,
                86400,
                TimeUnit.SECONDS
        );
        logger.info("Scheduling daily reset task with delay of {} seconds until midnight.", delayUntilMidnight);
    }

    private static long calculateDelayUntilMidnight() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return ChronoUnit.SECONDS.between(now, nextMidnight);
    }

    private static void scheduleDailyResets() {
        generateAndScheduleResets(0, 86400);
    }

    private static void generateAndScheduleResets(long minDelay, long maxDelay) {
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
                    ExchangeLimitManager::resetCounters,
                    delay,
                    TimeUnit.SECONDS
            );
        }

        logger.info("Transaction counters will reset at the following times: {}", resetTimes);
    }
}