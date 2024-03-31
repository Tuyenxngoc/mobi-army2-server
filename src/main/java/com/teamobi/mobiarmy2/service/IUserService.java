package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.IOException;

public interface IUserService {

    void sendKeys() throws IOException;

    void SendServerMessage(String message);

    void giaHanDo(Message ms);

    void nhiemVuView(Message ms);
}
