package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {
    private short equipmentId;
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