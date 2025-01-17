package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;

import java.util.List;

/**
 * @author tuyen
 */
public interface IClanDAO {

    Short getClanIcon(short clanId);

    int getXu(short clanId);

    int getLuong(short clanId);

    int getXp(short clanId);

    int getLevel(short clanId);

    int getCup(short clanId);

    void updateXu(short clanId, int xu);

    void updateLuong(short clanId, int luong);

    void gopClanContribute(String txtContribute, int userId, int xu, int luong);

    ClanInfoDTO getClanInfo(short clanId);

    short getCountClan();

    List<ClanDTO> getTopTeams(byte page);

    void updateXp(short clanId, int userId, int xp, int level);

    void updateCup(short clanId, int userId, int cup);

    void updateClanMemberPoints(int userId, int point);
}
