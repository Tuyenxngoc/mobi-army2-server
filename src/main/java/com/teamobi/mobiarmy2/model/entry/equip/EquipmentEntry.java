package com.teamobi.mobiarmy2.model.entry.equip;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentEntry {
    public int indexSale;
    public Byte characterId;
    public byte equipType;
    public short index;
    public String name;
    public int priceXu;
    public int priceLuong;
    public byte expirationDays;
    public byte bulletId;
    public short frameCount;
    public byte levelRequirement;
    public short[] bigImageCutX;
    public short[] bigImageCutY;
    public byte[] bigImageSizeX;
    public byte[] bigImageSizeY;
    public byte[] bigImageAlignX;
    public byte[] bigImageAlignY;
    public byte[] additionalPoints;
    public byte[] additionalPercent;
    public boolean onSale;
    public boolean isDisguise;
    public short[] disguiseEquippedIndexes;
}