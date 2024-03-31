package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.network.Impl.Message;

public interface IMessageHandler {

    void onMessage(Message message);

}
