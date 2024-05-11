package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.server.ClanManager;

import java.util.List;

public interface IClanDao {

    Short getClanIcon(int clanId);

    Byte getMembersOfClan(short clanId);

    void updateXu(int clanId, int xu);

    void updateLuong(int clanId, int luong);

    void gopClanContribute(String txtContribute, int playerId, int xu, int luong);

    ClanManager.ClanInfo getClanInfo(short clanId);

    List<ClanManager.ClanMemEntry> getClanMember(short clanId, byte page);

}
