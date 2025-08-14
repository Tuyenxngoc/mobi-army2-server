package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.common.constant.UserState;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.dao.UserDAO;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

public class FightWaitMessageHandler extends BaseMessageHandler {
    private final UserDAO userDAO;

    public FightWaitMessageHandler(Session session, UserDAO userDAO) {
        super(session);
        this.userDAO = userDAO;
    }

    private static String getFormattedRankDisplay(int rank) {
        if (rank < 10_000) {
            return String.format("Top %s", rank);
        } else if (rank < 100_000) {
            return String.format("Top %s+", Utils.getStringNumber(rank));
        } else {
            return "Top 100k+";
        }
    }

    public void handleChatMessage(Message ms) throws IOException {
        String message = ms.reader().readUTF().trim();
        if (message.isEmpty() || message.length() > 100) {
            return;
        }
        user.getFightWait().chatMessage(user.getUserId(), message);
    }

    public void handleKickPlayer(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        user.getFightWait().kickPlayer(user.getUserId(), userId);
    }

    public void handleLeaveBoard() {
        if (user.getState() == UserState.WAITING) {
            return;
        }
        user.getFightWait().leaveTeam(user.getUserId());
    }

    public void setReady(Message ms) throws IOException {
        boolean ready = ms.reader().readBoolean();
        user.getFightWait().setReady(ready, user.getUserId());
    }

    public void handleSetPasswordFightWait(Message ms) throws IOException {
        String password = ms.reader().readUTF().trim();
        if (password.isEmpty() || password.length() > 10 || !Utils.isAlphanumeric(password)) {
            return;
        }
        user.getFightWait().setPassRoom(password, user.getUserId());
    }

    public void handleSetMoneyFightWait(Message ms) throws IOException {
        int xu = ms.reader().readInt();
        if (xu < 0) {
            return;
        }
        user.getFightWait().setMoney(xu, user.getUserId());
    }

    public void handleStartGame() {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().startGame(user.getUserId());
    }

    public void handleSetFightWaitName(Message ms) throws IOException {
        String name = ms.reader().readUTF().trim();
        if (name.isEmpty() || name.length() > 20 || !Utils.isAlphanumeric(name)) {
            return;
        }
        user.getFightWait().setRoomName(user.getUserId(), name);
    }

    public void handleSetMaxPlayerFightWait(Message ms) throws IOException {
        byte maxPlayers = ms.reader().readByte();
        user.getFightWait().setMaxPlayers(user.getUserId(), maxPlayers);
    }

    public void handleChoseItemFight(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte[] items = new byte[8];

        for (int i = 0; i < items.length; i++) {
            byte index = dis.readByte();
            if (user.getItemFightQuantity(index) > 0) {
                items[i] = index;
            } else {
                items[i] = -1;
            }
        }
        user.getFightWait().setItems(user.getUserId(), items);
    }

    public void handleChangeTeam(Message ms) {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().changeTeam(user);
    }

    public void handleSelectMap(Message ms) throws IOException {
        byte mapId = ms.reader().readByte();
        user.getFightWait().setMap(user.getUserId(), mapId);
    }

    public void handleFindPlayerWait(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        boolean find = dis.readBoolean();
        if (find) {
            user.getFightWait().findPlayer(user.getUserId());
        } else {
            int userId = dis.readInt();
            user.getFightWait().inviteToRoom(userId);
        }
    }

    public void handleGetFlayerDetail(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        User us = null;
        if (userId == user.getUserId()) {
            us = user;
        } else if (user.isNotWaiting()) {
            us = user.getFightWait().getUserByUserId(userId);
        }
        ms = new Message(Cmd.PLAYER_DETAIL);
        DataOutputStream ds = ms.writer();
        if (us == null) {
            ds.writeInt(-1);
        } else {
            String rankDisplayText = GameString.NO_RANKING;
            if (us.getRank() == 0) {
                Optional<Integer> userRankOptional = userDAO.getUserRankByCup(us.getCup());
                if (userRankOptional.isPresent()) {
                    us.setRank(userRankOptional.get());

                    rankDisplayText = getFormattedRankDisplay(us.getRank());
                }
            } else {
                rankDisplayText = getFormattedRankDisplay(us.getRank());
            }

            ds.writeInt(us.getUserId());
            ds.writeUTF(us.getUsername());
            ds.writeInt(us.getXu());
            ds.writeByte(us.getCurrentLevel());
            ds.writeByte(us.getCurrentLevelPercent());
            ds.writeInt(us.getLuong());
            ds.writeInt(us.getCurrentXp());
            ds.writeInt(us.getCurrentRequiredXp());
            ds.writeInt(us.getCup());
            ds.writeUTF(rankDisplayText);
        }
        ds.flush();
        sendMessage(ms);
    }
}
