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

    @SerializedName("equipIndex")
    private short equipIndex;

    @SerializedName("equipType")
    private byte equipType;

    @SerializedName("vipLevel")
    private byte vipLevel;

    @SerializedName("purchaseDate")
    private LocalDateTime purchaseDate;

    @SerializedName("characterId")
    private byte characterId;

    @SerializedName("inUse")
    private byte inUse;

    @SerializedName("slots")
    private byte[] slots;

    @SerializedName("additionalPoints")
    private byte[] additionalPoints;

    @SerializedName("additionalPercent")
    private byte[] additionalPercent;

}