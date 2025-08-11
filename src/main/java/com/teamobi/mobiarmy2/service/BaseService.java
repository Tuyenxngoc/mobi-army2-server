package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import com.teamobi.mobiarmy2.server.ServerManager;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class BaseService {
    protected final Session session;

    protected BaseService(Session session) {
        this.session = session;
    }

    protected void sendMessage(Message ms) {
        session.sendMessage(ms);
    }

    public void sendServerMessage(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMoneyErrorMessage(String message) {
        try {
            Message ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMessageLoginFail(String message) {
        try {
            Message ms = new Message(Cmd.LOGIN_FAIL);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMessageToUser(String message) {
        sendMessageToUser(true, session.getUser(), message);
    }

    public void sendMessageToUser(boolean isAdminSender, User recipient, String message) {
        try {
            Message ms = new Message(Cmd.CHAT_TO);
            DataOutputStream ds = ms.writer();
            if (isAdminSender) {
                ds.writeInt(1);
                ds.writeUTF("ADMIN");
            } else {
                User user = session.getUser();
                ds.writeInt(user.getUserId());
                ds.writeUTF(user.getUsername());
            }
            ds.writeUTF(message);
            ds.flush();
            recipient.sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendServerInfo(String message, boolean toServer) {
        if (message == null || message.isEmpty()) {
            return;
        }
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();

            if (toServer) {
                ApplicationContext.getInstance()
                        .getBean(ServerManager.class).sendToServer(ms);
            } else {
                sendMessage(ms);
            }
        } catch (IOException ignored) {
        }
    }

}
