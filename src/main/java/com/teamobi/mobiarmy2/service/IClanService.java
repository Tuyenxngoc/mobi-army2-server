package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.model.ClanItem;

import java.util.List;

public interface IClanService {
    int getClanLevel(short clanId);

    int getClanXu(short clanId);

    int getClanLuong(short clanId);

    byte[] getClanIcon(short clanId);

    byte getTotalPage(short clanId);

    byte getTotalPagesClan();

    ClanInfoDTO getClanInfo(short clanId);

    List<ClanMemDTO> getMemberClan(short clanId, byte page);

    List<ClanDTO> getTopTeams(byte page);

    boolean[] getClanItems(short clanId);

    void updateItemClan(short clanId, int playerId, ClanItem clanItemShop, boolean isBuyXu);

    void contributeClan(short clanId, int playerId, int quantity, boolean isXu);

    void updateXp(short clanId, int playerId, int xpUp);

    void updateCup(short clanId, int playerId, int cupUp);
}
