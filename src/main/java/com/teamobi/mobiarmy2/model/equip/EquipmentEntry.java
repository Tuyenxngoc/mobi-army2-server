package com.teamobi.mobiarmy2.model.equip;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentEntry {
    public int indexEquip;
    public int indexSale;
    public byte characterId;
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
    public boolean isSet;
    public short[] arraySet;
}