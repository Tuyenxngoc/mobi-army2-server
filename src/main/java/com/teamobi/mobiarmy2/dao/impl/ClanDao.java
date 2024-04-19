package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;

/**
 * @author tuyen
 */
public class ClanDao implements IClanDao {

    @Override
    public Short getClanIcon(int clanId) {
        return null;
    }

    @Override
    public void gopXu(int clanId, int xu) {
        String sql = "UPDATE clan SET xu = xu + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, xu);
    }

    @Override
    public void gopLuong(int clanId, int luong) {
        String sql = "UPDATE clan SET luong = luong + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, luong);
    }

}
