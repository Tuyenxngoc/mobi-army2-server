package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;

import java.io.IOException;

public class GiftBoxMessageHandler extends BaseMessageHandler {
    public GiftBoxMessageHandler(Session session) {
        super(session);
    }

    public void openLuckyGift(Message ms) throws IOException {
        byte index = ms.reader().readByte();
        us().getGiftBoxService().openGiftBoxAfterFight(index);
    }
}
