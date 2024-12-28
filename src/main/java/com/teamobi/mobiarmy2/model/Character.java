package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    private byte id;
    private String name;
    private int priceXu;
    private int priceLuong;
    private byte windResistance;
    private byte minAngle;
    private short damage;
    private byte bulletDamage;
    private byte bulletCount;
    private Map<Byte, List<Equipment>> equips = new HashMap<>();
}