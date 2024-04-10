package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IFightService;

import java.io.DataOutputStream;
import java.io.IOException;

public class FightService implements IFightService {

    @Override
    public void sendMessageToUser(User user, String message) {
        dasd(user, message);
    }

    static void dasd(User user, String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
