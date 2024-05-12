package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.json.ClanItemData;
import com.teamobi.mobiarmy2.server.ClanManager;

import java.util.List;

public interface IClanDao {

    Short getClanIcon(int clanId);

    Byte getMembersOfClan(short clanId);

    int getXu(short clanId);

    int getLuong(short clanId);

    int getExp(short clanId);

    void updateXu(int clanId, int xu);

    void updateLuong(int clanId, int luong);

    void gopClanContribute(String txtContribute, int playerId, int xu, int luong);

    ClanManager.ClanInfo getClanInfo(short clanId);

    List<ClanManager.ClanMemEntry> getClanMember(short clanId, byte page);

    ClanItemData[] getClanItems(short clanId);

    void updateClanItems(short clanId, ClanItemData[] items);

}
