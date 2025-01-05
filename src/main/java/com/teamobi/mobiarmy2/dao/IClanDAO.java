package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.model.ClanItem;

import java.util.List;

/**
 * @author tuyen
 */
public interface IClanDAO {

    Short getClanIcon(int clanId);

    int getXu(short clanId);

    int getLuong(short clanId);

    int getXp(short clanId);

    int getLevel(short clanId);

    int getCup(short clanId);

    void updateXu(int clanId, int xu);

    void updateLuong(int clanId, int luong);

    void gopClanContribute(String txtContribute, int playerId, int xu, int luong);

    ClanInfoDTO getClanInfo(short clanId);

    List<ClanMemDTO> getClanMember(short clanId, byte page);

    ClanItem[] getClanItems(short clanId);

    void updateClanItems(short clanId, ClanItem[] items);

    short getCountClan();

    List<ClanDTO> getTopTeams(byte page);

    void updateXp(short clanId, int playerId, int xp, int level);

    void updateCup(short clanId, int playerId, int cup);

    void updateClanMemberPoints(int playerId, int point);
}
