package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.network.Impl.Session;

public class User {

    private final Session session;
    private UserState state;

    public User(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }

    public UserState getState() {
        return state;
    }

    public boolean isWaiting() {
        return state.equals(UserState.WAITING);
    }
}
