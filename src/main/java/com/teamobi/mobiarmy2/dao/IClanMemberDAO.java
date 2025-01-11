package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.ClanMemDTO;

import java.util.List;

public interface IClanMemberDAO {
    Byte count(short clanId);

    List<ClanMemDTO> getClanMember(short clanId, byte page);
}
