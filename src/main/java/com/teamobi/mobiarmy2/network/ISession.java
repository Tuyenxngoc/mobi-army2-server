package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;

public interface ISession {

    void sendMessage(Message message);

    void sendKeys();

    void close();

    void setVersion(String version);

    void setPlatform(String platform);

    void setProvider(byte provider);

    User getUser();
}
