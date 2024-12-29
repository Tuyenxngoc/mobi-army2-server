package com.teamobi.mobiarmy2.dto;

import com.teamobi.mobiarmy2.model.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.SpecialItemChestJson;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class GiftCodeDTO {
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
