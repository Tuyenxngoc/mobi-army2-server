package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class GiftCodeEntry {
    private long id;
    private boolean isUsed;
    private short limit;
    private LocalDateTime expiryDate;
    private int xu;
    private int luong;
    private int exp;
    private SpecialItemChestJson[] items;
    private EquipmentChestJson[] equips;
}
