package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.model.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.clan.ClanMemEntry;

import java.util.List;

/**
 * @author tuyen
 */
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

    ClanItemJson[] getClanItems(short clanId);

    void updateClanItems(short clanId, ClanItemJson[] items);

    short getCountClan();

    List<ClanEntry> getTopTeams(byte page);
}
