package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;

public abstract class BaseMessageHandler {
    protected final Session session;

    protected BaseMessageHandler(Session session) {
        this.session = session;
    }

    protected void sendMessage(Message ms) {
        session.sendMessage(ms);
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
