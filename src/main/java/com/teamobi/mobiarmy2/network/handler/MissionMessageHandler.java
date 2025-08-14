package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.entity.Mission;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.MissionManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class MissionMessageHandler extends BaseMessageHandler {
    public MissionMessageHandler(Session session) {
        super(session);
    }

    public void handleGetMissions(Message ms) {
        User user = session.getUser();
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte action = ms.reader().readByte();
            if (action == 0) {
                sendMissionInfo();
            } else {
                byte missionId = ms.reader().readByte();
                missionComplete(missionId);
            }
        } catch (IOException ignored) {
        }
    }

    private void missionComplete(byte missionId) throws IOException {
        User user = session.getUser();
        String message;
        Mission mission = MissionManager.getMissionById(missionId);
        if (mission == null) {
            message = GameString.MISSION_NOT_FOUND;
        } else {
            byte missionType = mission.getType();
            byte missionLevel = user.getMissionLevel()[missionType];
            byte requiredLevel = mission.getLevel();

            if (user.getMission()[missionType] < mission.getRequirement()) {
                message = GameString.MISSION_NOT_COMPLETED;
            } else if (missionLevel == requiredLevel) {
                user.getMissionLevel()[mission.getType()]++;
                if (mission.getRewardXu() > 0) {
                    user.updateXu(mission.getRewardXu());
                }
                if (mission.getRewardLuong() > 0) {
                    user.updateLuong(mission.getRewardLuong());
                }
                if (mission.getRewardXp() > 0) {
                    user.updateXp(mission.getRewardXp());
                }
                if (mission.getRewardCup() > 0) {
                    user.updateCup(mission.getRewardCup());
                }
                sendMissionInfo();
                message = GameString.createMissionCompleteMessage(mission.getReward());
            } else if (missionLevel < requiredLevel) {
                message = GameString.MISSION_NOT_COMPLETED;
            } else {
                message = GameString.MISSION_COMPLETED;
            }
        }
        sendMoneyErrorMessage(message);
    }

    private void sendMissionInfo() throws IOException {
        User user = session.getUser();
        Message ms = new Message(Cmd.MISSISON);
        DataOutputStream ds = ms.writer();
        int i = 0;
        for (List<Byte> missionIds : MissionManager.MISSIONS_BY_TYPE.values()) {
            int index = user.getMissionLevel()[i] - 1;
            if (index >= missionIds.size()) {
                index = missionIds.size() - 1;
            }
            Mission mission = MissionManager.getMissionById(missionIds.get(index));
            ds.writeByte(mission.getId());
            ds.writeByte(mission.getLevel());
            ds.writeUTF(mission.getName());
            ds.writeUTF(mission.getReward());
            ds.writeInt(mission.getRequirement());
            ds.writeInt(Math.min(user.getMission()[i], mission.getRequirement()));
            ds.writeBoolean(user.getMission()[i] >= mission.getRequirement());
            i++;
        }
        ds.flush();
        sendMessage(ms);
    }
}
