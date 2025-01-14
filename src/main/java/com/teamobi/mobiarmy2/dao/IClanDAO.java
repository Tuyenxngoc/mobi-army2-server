package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.json.ClanItemJson;

import java.util.List;

/**
 * @author tuyen
 */
public interface IClanDAO {

    Short getClanIcon(int clanId);

    Byte getMembersOfClan(short clanId);

    int getXu(short clanId);

    int getLuong(short clanId);

    int getXp(short clanId);

    int getLevel(short clanId);

    int getCup(short clanId);

    void updateXu(int clanId, int xu);

    void updateLuong(int clanId, int luong);

    void gopClanContribute(String txtContribute, int userId, int xu, int luong);

    ClanInfoDTO getClanInfo(short clanId);

    List<ClanMemDTO> getClanMember(short clanId, byte page);

    ClanItemJson[] getClanItems(short clanId);

    void updateClanItems(short clanId, ClanItemJson[] items);

    short getCountClan();

    List<ClanDTO> getTopTeams(byte page);

    void updateXp(short clanId, int userId, int xp, int level);

    void updateCup(short clanId, int userId, int cup);

    void updateClanMemberPoints(int userId, int point);
}
