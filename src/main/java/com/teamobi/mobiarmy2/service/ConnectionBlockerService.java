package com.teamobi.mobiarmy2.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionBlockerService {
    private static final int MAX_CONNECTIONS_PER_IP = 3;
    private final Map<String, AtomicInteger> connectionMap = new ConcurrentHashMap<>();

    /**
     * Tăng số kết nối hiện tại. Trả về true nếu kết nối được phép, false nếu chặn
     */
    public boolean tryIncrementConnection(String ipAddress) {
        AtomicInteger count = connectionMap.computeIfAbsent(ipAddress, ip -> new AtomicInteger(0));
        int newCount = count.incrementAndGet();
        if (newCount > MAX_CONNECTIONS_PER_IP) {
            // Vượt quá → giảm lại và chặn
            count.decrementAndGet();
            return false;
        }
        return true;
    }

    /**
     * Giảm số kết nối khi kết thúc
     */
    public void decrementConnection(String ipAddress) {
        AtomicInteger count = connectionMap.get(ipAddress);
        if (count == null) return;

        int remaining = count.decrementAndGet();
        if (remaining <= 0) {
            connectionMap.remove(ipAddress);
        }
    }
}
