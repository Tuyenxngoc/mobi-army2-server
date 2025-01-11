package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.ClanItem;

public interface IClanItemDAO {
    ClanItem[] getClanItems(short clanId);

    void updateClanItems(short clanId, ClanItem[] items);
}
