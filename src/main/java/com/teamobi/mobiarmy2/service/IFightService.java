package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.model.User;

public interface IFightService {

    void sendMessageToUser(User user, String message);

}
