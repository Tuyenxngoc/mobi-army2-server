package com.teamobi.mobiarmy2.model.entry.equip;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
@Getter
@Setter
public class CharacterEntry {
    public byte id;
    public String name;
    public int buyXu;
    public int buyLuong;
    public byte windResistance;
    public byte minAngle;
    public byte bulletCount;
    public short damage;
    public byte bulletDamage;
    public Map<Byte, List<EquipmentEntry>> equips = new HashMap<>();
}