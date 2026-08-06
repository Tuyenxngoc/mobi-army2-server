package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.service.LeaderboardService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class LeaderboardMessageHandler extends BaseMessageHandler {
    private final LeaderboardService leaderboardService;

    public LeaderboardMessageHandler(Session session, MessageSender messageSender, LeaderboardService leaderboardService) {
        super(session, messageSender);
        this.leaderboardService = leaderboardService;
    }

    public void viewLeaderboard(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        byte page = dis.readByte();

        if (type >= LeaderboardService.CATEGORIES.length) {
            return;
        }
        ms = new Message(Cmd.BANGTHANHTICH);
        DataOutputStream ds = ms.writer();
        ds.writeByte(type);
        if (type < 0) {
            ds.writeByte(LeaderboardService.CATEGORIES.length);
            for (String name : LeaderboardService.CATEGORIES) {
                ds.writeUTF(name);
            }
        } else {
            //Kiểm tra page num
            int maxPage = leaderboardService.getTotalPageByType(type);
            if (page > maxPage || page >= 10) {
                page = 0;
            }
            if (page < 0) {
                page = (byte) maxPage;
            }
            //Gửi dữ liệu
            ds.writeByte(page);
            ds.writeUTF(LeaderboardService.LABELS[type]);
            List<UserLeaderboardDTO> bangXH = leaderboardService.getUsers(type, page, 10);
            if (bangXH != null) {
                for (UserLeaderboardDTO pl : bangXH) {
                    ds.writeInt(pl.getUserId());
                    ds.writeUTF(pl.getUsername());
                    ds.writeByte(pl.getActiveCharacter());
                    ds.writeShort(pl.getClanId());
                    ds.writeByte(pl.getLevel());
                    ds.writeByte(pl.getLevelPt());
                    ds.writeByte(pl.getIndex());
                    for (short i : pl.getData()) {
                        ds.writeShort(i);
                    }
                    ds.writeUTF(pl.getDetail());
                }
            }
        }
        ds.flush();
        sendMessage(ms);
    }
}
