package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.json.ClanItemData;
import com.teamobi.mobiarmy2.model.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.clan.ClanMemEntry;

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

    ClanInfo getClanInfo(short clanId);

    List<ClanMemEntry> getClanMember(short clanId, byte page);

    ClanItemData[] getClanItems(short clanId);

    void updateClanItems(short clanId, ClanItemData[] items);

    short getCountClan();

    List<ClanEntry> getTopTeams(byte page);
}
