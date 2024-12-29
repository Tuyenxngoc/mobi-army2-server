package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentChestJson {
    private int key;
    private short equipIndex;
    private byte equipType;
    private byte vipLevel;
    private LocalDateTime purchaseDate;
    private byte characterId;
    private byte inUse;
    private byte[] slots;
    private byte[] addPoints;
    private byte[] addPercents;
}