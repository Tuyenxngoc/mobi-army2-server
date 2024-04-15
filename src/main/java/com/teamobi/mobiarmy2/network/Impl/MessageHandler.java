package com.teamobi.mobiarmy2.network.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.server.ServerManager;

public class MessageHandler implements IMessageHandler {

    private final Session session;

    public MessageHandler(Session session) {
        this.session = session;
    }

    @Override
    public void onMessage(Message ms) {
        try {
            switch (ms.getCommand()) {
                case Cmd.GET_KEY -> session.sendKeys();

                default -> ServerManager.getInstance().logger().logWarning("Command " + ms.getCommand() + " is not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
