package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.network.ISession;

import java.util.List;

public interface ServerListener {

    void onUsersUpdated(List<ISession> users);

}
