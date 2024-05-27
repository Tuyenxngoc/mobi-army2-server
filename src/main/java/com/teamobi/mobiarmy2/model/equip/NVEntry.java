package com.teamobi.mobiarmy2.model.equip;

import java.util.ArrayList;
import java.util.List;

public class NVEntry {
    public byte id;
    public String name;
    public int buyXu;
    public int buyLuong;
    public byte ma_sat_gio;
    public byte goc_min;
    public byte so_dan;
    public short sat_thuong;
    public byte sat_thuong_dan;
    public List<EquipmentData> trangbis = new ArrayList<>();
}