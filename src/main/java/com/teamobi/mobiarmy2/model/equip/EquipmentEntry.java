package com.teamobi.mobiarmy2.model.equip;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentEntry {
    private int saleIndex;
    private byte characterId;
    private byte equipType;
    private short equipIndex;
    private String name;
    private int priceXu;
    private int priceLuong;
    private byte expirationDays;
    private byte bulletId;
    private short frameCount;
    private byte levelRequirement;
    private short[] bigImageCutX;
    private short[] bigImageCutY;
    private byte[] bigImageSizeX;
    private byte[] bigImageSizeY;
    private byte[] bigImageAlignX;
    private byte[] bigImageAlignY;
    private byte[] addPoints;
    private byte[] addPercents;
    private boolean onSale;
    private boolean isDisguise;
    private short[] disguiseEquippedIndexes;
}