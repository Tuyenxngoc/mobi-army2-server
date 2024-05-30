package com.teamobi.mobiarmy2.model.item;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FightItemEntry {
    private String name;
    private short buyXu;
    private short buyLuong;
    private byte carriedItemCount;
}