package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentChestJson {

    @SerializedName("k")
    private int key;

    @SerializedName("i")
    private short equipmentId;

    @SerializedName("v")
    private byte vipLevel;

    @SerializedName("d")
    private LocalDateTime purchaseDate;

    @SerializedName("u")
    private byte inUse;

    @SerializedName("s")
    private byte[] slots;

    @SerializedName("p")
    private byte[] addPoints;

    @SerializedName("c")
    private byte[] addPercents;

}