package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.impl.ClanDao;
import com.teamobi.mobiarmy2.util.Until;

/**
 * @author tuyen
 */
public class ClanManager {

    private static ClanManager instance;

    private final ClanDao clanDao;

    public ClanManager() {
        clanDao = new ClanDao();
    }

    public static ClanManager getInstance() {
        if (instance == null) {
            instance = new ClanManager();
        }
        return instance;
    }

    public ClanDao getClanDao() {
        return clanDao;
    }

    public void contributeClan(short clanId, int userId, int quantity, boolean isXu) {
        String txtContribute;
        if (isXu) {
            clanDao.gopXu(clanId, quantity);
            txtContribute = Until.getStringNumber(quantity) + " xu";
        } else {
            clanDao.gopLuong(clanId, quantity);
            txtContribute = Until.getStringNumber(quantity) + " lượng";
        }
        clanDao.gopClanContribute(txtContribute, userId);
    }
}
