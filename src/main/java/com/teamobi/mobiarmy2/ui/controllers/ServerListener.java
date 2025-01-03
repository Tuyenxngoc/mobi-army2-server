package com.teamobi.mobiarmy2.ui.controllers;

import com.teamobi.mobiarmy2.network.ISession;

import java.util.List;

public interface ServerListener {

    void onUsersUpdated(List<ISession> users);

}
