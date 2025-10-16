package com.teamobi.mobiarmy2.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginRateLimiterService {
    private static final int LOGIN_BLOCK_DURATION_SECONDS = 20;
    private final Map<String, Long> blockedUsers = new ConcurrentHashMap<>();

    /**
     * Trả về số giây còn lại của block, 0 nếu không bị block
     */
    public long getRemainingLoginTime(String username) {
        Long expireTime = blockedUsers.get(username);
        if (expireTime == null) return 0;

        long now = System.currentTimeMillis();
        long remainingMs = expireTime - now;

        if (remainingMs <= 0) {
            // Hết hạn → xóa khỏi map
            blockedUsers.remove(username);
            return 0;
        }

        return remainingMs / 1000; // trả về giây
    }

    /**
     * Lưu thời gian block cho user
     */
    public void saveLogoutTime(String username) {
        long expireTime = System.currentTimeMillis() + LOGIN_BLOCK_DURATION_SECONDS * 1000L;
        blockedUsers.put(username, expireTime);
    }
}
