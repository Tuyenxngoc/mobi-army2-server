package com.teamobi.mobiarmy2.ui.controllers;

import com.teamobi.mobiarmy2.network.Session;

import java.util.List;

public interface ServerListener {
    void onUsersUpdated(List<Session> users);
}