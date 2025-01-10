package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    private byte characterId;
    private String name;
    private int priceXu;
    private int priceLuong;
    private byte windResistance;
    private byte minAngle;
    private short damage;
    private byte bulletDamage;
    private byte bulletCount;
}