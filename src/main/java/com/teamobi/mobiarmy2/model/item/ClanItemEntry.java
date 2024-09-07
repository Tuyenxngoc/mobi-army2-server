package com.teamobi.mobiarmy2.model.item;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanItemEntry {
    private byte id;
    private byte level;
    private String name;
    private byte time;
    private byte onSale;
    private int xu;
    private int luong;
}