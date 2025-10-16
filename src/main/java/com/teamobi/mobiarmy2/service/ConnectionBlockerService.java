package com.teamobi.mobiarmy2.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConnectionBlockerService {
    private static final int MAX_CONNECTIONS_PER_IP = 3;
    private final Map<String, AtomicInteger> connectionMap = new ConcurrentHashMap<>();

    /**
     * Kiểm tra IP có bị chặn không
     */
    public boolean isIpBlocked(String ipAddress) {
        AtomicInteger count = connectionMap.get(ipAddress);
        return count != null && count.get() >= MAX_CONNECTIONS_PER_IP;
    }

    /**
     * Tăng số kết nối hiện tại. Trả về true nếu kết nối được phép, false nếu chặn
     */
    public boolean tryIncrementConnection(String ipAddress) {
        return connectionMap.compute(ipAddress, (ip, count) -> {
            if (count == null) count = new AtomicInteger(0);

            if (count.get() >= MAX_CONNECTIONS_PER_IP) {
                // Không tăng nữa, return nguyên count
                return count;
            } else {
                count.incrementAndGet();
                return count;
            }
        }).get() <= MAX_CONNECTIONS_PER_IP;
    }

    /**
     * Giảm số kết nối khi kết thúc
     */
    public void decrementConnection(String ipAddress) {
        connectionMap.computeIfPresent(ipAddress, (ip, count) -> {
            int remaining = count.decrementAndGet();
            if (remaining <= 0) return null; // Xóa IP khỏi map khi không còn kết nối
            return count;
        });
    }
}
