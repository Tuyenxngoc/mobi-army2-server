package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.IOException;

public interface ISession {

    void setHandler(IMessageHandler messageHandler);

    boolean isSendKeyComplete();

    void sendMessage(Message message);

    void close();

    void sendKeys() throws IOException;

}
