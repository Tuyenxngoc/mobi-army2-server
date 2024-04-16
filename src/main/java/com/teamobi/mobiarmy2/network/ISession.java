package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.network.Impl.Message;

public interface ISession {

    void sendMessage(Message message);

    void sendKeys();

    void close();
}
