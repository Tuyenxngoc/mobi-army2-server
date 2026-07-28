package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.server.SessionRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

@Slf4j
public class MessageSender {
    private final SessionRegistry sessionRegistry;

    public MessageSender(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
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
        for (Session session : sessionRegistry.getSessions()) {
            sendTo(session, ms);
        }
    }

    public void sendServerMessage(User user, String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendTo(user, ms);
        } catch (IOException e) {
            log.error("Failed to send server message to user {}: {}", user != null ? user.getUserId() : -1, e.getMessage(), e);
        }
    }

    public void sendMoneyErrorMessage(User user, String message) {
        try {
            Message ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendTo(user, ms);
        } catch (IOException e) {
            log.error("Failed to send money error message to user {}: {}", user != null ? user.getUserId() : -1, e.getMessage(), e);
        }
    }

    public void sendAdminMessage(User user, String message) {
        sendMessageToUser(true, user, message, user);
    }

    public void sendMessageToUser(boolean isAdminSender, User sender, String message, User recipient) {
        try {
            Message ms = new Message(Cmd.CHAT_TO);
            DataOutputStream ds = ms.writer();
            if (isAdminSender) {
                ds.writeInt(1);
                ds.writeUTF("ADMIN");
            } else {
                ds.writeInt(sender.getUserId());
                ds.writeUTF(sender.getUsername());
            }
            ds.writeUTF(message);
            ds.flush();
            sendTo(recipient, ms);
        } catch (IOException e) {
            log.error("Failed to send chat message to user {}: {}", recipient != null ? recipient.getUserId() : -1, e.getMessage(), e);
        }
    }

    public void sendServerInfo(User user, String message, boolean toServer) {
        if (message == null || message.isEmpty()) {
            return;
        }
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();

            if (toServer) {
                broadcast(ms);
            } else {
                sendTo(user, ms);
            }
        } catch (IOException e) {
            log.error("Failed to send server info: {}", e.getMessage(), e);
        }
    }
}
