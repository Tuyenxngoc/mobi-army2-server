package com.teamobi.mobiarmy2.network;

import java.io.IOException;

public interface ISession {

    boolean isSendKeyComplete();

    void sendMessage(Message message);

    void close();

    void sendKeys() throws IOException;

}
