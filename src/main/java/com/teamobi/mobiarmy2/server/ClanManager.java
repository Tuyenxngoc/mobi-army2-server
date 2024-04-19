package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.impl.ClanDao;

/**
 * @author tuyen
 */
public class ClanManager {

    private static ClanManager instance;

    private final ClanDao clanDao;

    public ClanManager() {
        clanDao = new ClanDao();
    }

    public static synchronized ClanManager getInstance() {
        if (instance == null) {
            instance = new ClanManager();
        }
        return instance;
    }

    public ClanDao getClanDao() {
        return clanDao;
    }
}
