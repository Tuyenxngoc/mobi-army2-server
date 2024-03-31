package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IUserService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserService implements IUserService {

    private final User user;

    public UserService(User user) {
        this.user = user;
    }

    @Override
    public void sendKeys() throws IOException {
        user.getSession().sendKeys();
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

    @Override
    public void giaHanDo(Message ms) {
        if (!user.isWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int idKey = dis.readInt();
            if (action == 0) {

            } else if (action == 1) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nhiemVuView(Message ms) {
        if (!user.isWaiting()) {
            return;
        }
    }

}
