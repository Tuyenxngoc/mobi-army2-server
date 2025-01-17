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

    void setPlatform(String platform);

    String getVersion();

    void setVersion(String version);

    byte getProvider();

    void setProvider(byte provider);

    User getUser();
}
