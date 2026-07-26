package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.network.Session;

public abstract class BaseMessageHandler {
    protected final Session session;
    protected final MessageSender messageSender;

    protected BaseMessageHandler(Session session, MessageSender messageSender) {
        this.session = session;
        this.messageSender = messageSender;
    }

    protected BaseMessageHandler(Session s) {
        this.session = s;
        messageSender = null;//todo app
    }

    protected void sendMessage(Message ms) {
        messageSender.sendTo(session, ms);
    }

    protected User us() {
        return session.getUser();
    }

    protected FightWait fw() {
        return us() != null ? us().getFightWait() : null;
    }

    protected IFightManager fm() {
        return fw() != null ? fw().getFightManager() : null;
    }

}
