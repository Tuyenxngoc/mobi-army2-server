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
public class ClanItemShop {
    private byte id;
    private byte level;
    private String name;
    private byte time;
    private byte onSale;
    private int xu;
    private int luong;
}