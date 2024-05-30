package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class SpecialItemChestEntry {
    public short quantity;
    public SpecialItemEntry item;
}