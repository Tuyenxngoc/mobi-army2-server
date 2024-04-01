package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.IOException;

public interface IUserService {

    void sendKeys() throws IOException;

    void sendServerMessage(String message);

    void giaHanDo(Message ms);

    void nhiemVuView(Message ms);

    void login(Message ms);

    void sendLoginSuccess();

    void gopClan(Message ms);

    void getVersionCode(Message ms);

    void getProvider(Message ms);

    void hopTrangBi(Message ms);

    void buyItem(Message ms);
}
