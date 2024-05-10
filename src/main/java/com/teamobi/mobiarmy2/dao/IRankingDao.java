package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.server.BangXHManager;

import java.util.List;

public interface IRankingDao {

    List<BangXHManager.BangXHEntry> getTopDanhDu();

    List<BangXHManager.BangXHEntry> getTopCaoThu();

    List<BangXHManager.BangXHEntry> getTopDaiGiaXu();

    List<BangXHManager.BangXHEntry> getTopDaiGiaLuong();

    List<BangXHManager.BangXHEntry> getTopDanhDuTuan();

    List<BangXHManager.BangXHEntry> getTopDaiGiaTuan();
}
