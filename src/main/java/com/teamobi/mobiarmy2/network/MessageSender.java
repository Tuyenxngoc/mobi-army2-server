package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.server.ServerManager;

import java.util.Collection;

/**
 * Điểm gửi Message duy nhất cho mọi đích: một session, một user, nhiều user, hoặc toàn server.
 * Thay thế các đường gửi rải rác trước đây (User.sendMessage/sendMessageToUser/sendServerInfo,
 * ServerManager.sendToServer gọi trực tiếp từ entity).
 */
public class MessageSender {
    private final ServerManager serverManager;

    public MessageSender(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public void sendTo(Session session, Message ms) {
        if (session != null) {
            session.sendMessage(ms);
        }
    }

    public void sendTo(User user, Message ms) {
        if (user != null) {
            sendTo(user.getSession(), ms);
        }
    }

    public void sendTo(Collection<User> users, Message ms) {
        if (users == null) {
            return;
        }
        for (User user : users) {
            sendTo(user, ms);
        }
    }

    public void broadcast(Message ms) {
        serverManager.sendToServer(ms);
    }
}
