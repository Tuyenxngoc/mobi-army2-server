package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.model.User;

/**
 * @author tuyen
 */
public interface ISession {

    void sendMessage(IMessage message);

    void sendKeys();

    void close();

    String getIPAddress();

    String getPlatform();

    String getVersion();

    byte getProvider();

    void setVersion(String version);

    void setPlatform(String platform);

    void setProvider(byte provider);

    User getUser();
}
