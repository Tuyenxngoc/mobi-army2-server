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

    @SerializedName("i")
    private short equipIndex;

    @SerializedName("eT")
    private byte equipType;

    @SerializedName("vL")
    private byte vipLevel;

    @SerializedName("pD")
    private LocalDateTime purchaseDate;

    @SerializedName("cId")
    private byte characterId;

    @SerializedName("iU")
    private byte inUse;

    @SerializedName("s")
    private byte[] slots;

    @SerializedName("aP")
    private byte[] additionalPoints;

    @SerializedName("aPct")
    private byte[] additionalPercent;

}