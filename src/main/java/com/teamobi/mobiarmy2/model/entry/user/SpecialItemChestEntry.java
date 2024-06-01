package com.teamobi.mobiarmy2.model.entry.user;

import com.teamobi.mobiarmy2.model.entry.item.SpecialItemEntry;
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