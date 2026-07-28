package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giữ toàn bộ session đang kết nối và ánh xạ userId -> sessionId.
 * Tách khỏi {@link ServerManager} để các thành phần chỉ cần tra cứu session
 * (MessageSender, handler, FightWait) không phải phụ thuộc vào cả ServerManager.
 */
@Slf4j
public class SessionRegistry {
    private final ConcurrentHashMap<Long, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Long> userToSession = new ConcurrentHashMap<>();

    private final ServerConfig serverConfig;

    public SessionRegistry(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void addSession(Session session) {
        if (sessions.size() >= serverConfig.getMaxClients()) {
            log.warn("Max clients reached. Rejecting session: {}", session.getSessionId());
            session.closeChannel();
            return;
        }
        sessions.put(session.getSessionId(), session);
    }

    public void registerUser(User user) {
        if (user != null && user.getSession() != null) {
            userToSession.put(user.getUserId(), user.getSession().getSessionId());
        }
    }

    public int getUserCount() {
        return userToSession.size();
    }

    public void removeSession(Long sessionId) {
        Session removed = sessions.remove(sessionId);
        if (removed != null && removed.getUser() != null) {
            userToSession.remove(removed.getUser().getUserId());
        }
    }

    public Collection<Session> getSessions() {
        return sessions.values();
    }

    public User getUserByUserId(int userId) {
        Long sessionId = userToSession.get(userId);
        if (sessionId == null) {
            return null;
        }
        Session session = sessions.get(sessionId);
        return session != null ? session.getUser() : null;
    }

    public List<User> findWaitPlayers(int excludedUserId) {
        List<User> result = new ArrayList<>(10);
        for (var entry : userToSession.entrySet()) {
            if (result.size() >= 10) break;
            int userId = entry.getKey();
            if (userId == excludedUserId) continue;

            Long sessionId = entry.getValue();
            if (sessionId == null) continue;

            Session session = sessions.get(sessionId);
            if (session == null) continue;

            User user = session.getUser();
            if (user == null || !user.isLogged()) continue;

            if (user.getState() == UserState.WAITING) {
                result.add(user);
            }
        }
        return result;
    }

    public void closeAllSessions() {
        for (Session session : sessions.values()) {
            session.closeChannel();
        }
    }
}
