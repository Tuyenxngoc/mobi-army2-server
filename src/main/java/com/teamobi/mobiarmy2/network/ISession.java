package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.IOException;

public interface ISession {

    void sendMessage(Message message);

    long getSessionId();

    void close();

    void sendKeys() throws IOException;

    void setPlatform(String platform);

    void setProvider(byte provider);

    void setVersion(String version);
}
