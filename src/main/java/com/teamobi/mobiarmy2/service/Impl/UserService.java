package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.army2.server.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.service.IUserService;

import java.io.DataOutputStream;
import java.io.IOException;

public class UserService implements IUserService {

    private final User user;

    public UserService(User user) {
        this.user = user;
    }

    @Override
    public void SendServerMessage(String message) {
        try {
            Message ms = new Message(45);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
