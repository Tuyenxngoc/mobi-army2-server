package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.CharacterManager;

import java.io.DataOutputStream;
import java.io.IOException;

public class CharacterMessageHandler extends BaseMessageHandler {
    public CharacterMessageHandler(Session session) {
        super(session);
    }

    public void handleChoseCharacter(Message ms) throws IOException {
        byte characterId = ms.reader().readByte();
        if (characterId >= CharacterManager.CHARACTERS.size() || characterId < 0 || !us().getOwnedCharacters()[characterId]) {
            return;
        }
        us().setActiveCharacterId(characterId);

        ms = new Message(Cmd.CHOOSE_GUN);
        DataOutputStream ds = ms.writer();
        ds.writeInt(us().getUserId());
        ds.writeByte(characterId);
        ds.flush();
        sendMessage(ms);

        sendCharacterInfo();
        sendEquipInfo();
    }

    private void sendEquipInfo() throws IOException {
        Message ms = new Message(Cmd.CURR_EQUIP_DBKEY);
        DataOutputStream ds = ms.writer();
        for (int i = 0; i < 5; i++) {
            ds.writeInt(us().getEquipData()[us().getActiveCharacterId()][i]);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleAddPoints(Message ms) throws IOException {
        short[] points = new short[5];
        int totalPoints = 0;
        for (int i = 0; i < points.length; i++) {
            points[i] = ms.reader().readShort();
            if (points[i] < 0) {
                return;
            }
            totalPoints += points[i];
        }
        if (totalPoints <= us().getCurrentPoint()) {
            us().updatePoints(points, totalPoints);
        }

        sendCharacterInfo();
    }
}
