package com.teamobi.mobiarmy2.model.entry.item;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FightItemEntry {
    private String name;
    private short buyXu;
    private short buyLuong;
    private byte carriedItemCount;
}