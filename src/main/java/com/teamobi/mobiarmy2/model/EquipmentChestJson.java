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

    @SerializedName("i")
    private short equipmentId;

    @SerializedName("v")
    private byte vipLevel;

    @SerializedName("d")
    private LocalDateTime purchaseDate;

    @SerializedName("u")
    private byte inUse;
    private byte[] slots;

    @SerializedName("p")
    private byte[] addPoints;

    @SerializedName("c")
    private byte[] addPercents;
}