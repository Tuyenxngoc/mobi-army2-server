package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.dao.UserDAO;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.util.Utils;

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

    public void openLuckyGift(Message ms) throws IOException {
        byte index = ms.reader().readByte();
        fw().openGiftBoxAfterFight(us().getUserId(), index);
    }

    public void handleChatMessage(Message ms) throws IOException {
        String message = ms.reader().readUTF().trim();
        if (message.isEmpty() || message.length() > 100) {
            return;
        }
        fw().chatMessage(us().getUserId(), message);
    }

    public void handleKickPlayer(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        fw().kickPlayer(us().getUserId(), userId);
    }

    public void handleLeaveBoard() {
        if (us().getState() == UserState.WAITING) {
            return;
        }
        fw().leaveTeam(us().getUserId());
    }

    public void setReady(Message ms) throws IOException {
        boolean ready = ms.reader().readBoolean();
        fw().setReady(ready, us().getUserId());
    }

    public void handleSetPasswordFightWait(Message ms) throws IOException {
        String password = ms.reader().readUTF().trim();
        if (password.isEmpty() || password.length() > 10 || !Utils.isAlphanumeric(password)) {
            return;
        }
        fw().setPassRoom(password, us().getUserId());
    }

    public void handleSetMoneyFightWait(Message ms) throws IOException {
        int xu = ms.reader().readInt();
        if (xu < 0) {
            return;
        }
        fw().setMoney(xu, us().getUserId());
    }

    public void handleStartGame() {
        if (us().getState() != UserState.WAIT_FIGHT) {
            return;
        }
        fw().startGame(us().getUserId());
    }

    public void handleSetFightWaitName(Message ms) throws IOException {
        String name = ms.reader().readUTF().trim();
        if (name.isEmpty() || name.length() > 20 || !Utils.isAlphanumeric(name)) {
            return;
        }
        fw().setRoomName(us().getUserId(), name);
    }

    public void handleSetMaxPlayerFightWait(Message ms) throws IOException {
        byte maxPlayers = ms.reader().readByte();
        fw().setMaxPlayers(us().getUserId(), maxPlayers);
    }

    public void handleChoseItemFight(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte[] items = new byte[8];

        for (int i = 0; i < items.length; i++) {
            byte index = dis.readByte();
            if (us().getItemFightQuantity(index) > 0) {
                items[i] = index;
            } else {
                items[i] = -1;
            }
        }
        fw().setItems(us().getUserId(), items);
    }

    public void handleChangeTeam() {
        if (us().getState() != UserState.WAIT_FIGHT) {
            return;
        }
        fw().changeTeam(us());
    }

    public void handleSelectMap(Message ms) throws IOException {
        byte mapId = ms.reader().readByte();
        fw().setMap(us().getUserId(), mapId);
    }

    public void handleFindPlayerWait(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        boolean find = dis.readBoolean();
        if (find) {
            fw().findPlayer(us().getUserId());
        } else {
            int userId = dis.readInt();
            fw().inviteToRoom(userId);
        }
    }

    public void handleGetFlayerDetail(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        User us = null;
        if (userId == us().getUserId()) {
            us = us();
        } else if (us().isNotWaiting()) {
            us = fw().getUserByUserId(userId);
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
