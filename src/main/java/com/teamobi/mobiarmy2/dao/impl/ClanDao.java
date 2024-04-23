package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.util.Until;

import java.util.Date;

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

    @Override
    public void gopClanContribute(String txtContribute, int userId) {
        String sql = "UPDATE `clanmem` " +
                "SET `n_contribute` = `n_contribute` + 1, " +
                "`contribute_time` = ?, " +
                "`contribute_text` = ? " +
                "WHERE `user` = ?";
        HikariCPManager.getInstance().update(sql, Until.toDateString(new Date()), txtContribute, userId);
    }

}
