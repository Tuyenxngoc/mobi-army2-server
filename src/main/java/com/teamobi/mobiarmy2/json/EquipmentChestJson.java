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

    @SerializedName("ei")
    private short equipIndex;

    @SerializedName("et")
    private byte equipType;

    @SerializedName("vl")
    private byte vipLevel;

    @SerializedName("pd")
    private LocalDateTime purchaseDate;

    @SerializedName("cid")
    private byte characterId;

    @SerializedName("iu")
    private byte inUse;

    @SerializedName("s")
    private byte[] slots;

    @SerializedName("ap")
    private byte[] addPoints;

    @SerializedName("apc")
    private byte[] addPercents;

}