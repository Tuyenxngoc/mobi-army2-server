package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentChestJson {

    @SerializedName("i")
    private byte equipIndex;

    @SerializedName("eT")
    private byte equipType;

    @SerializedName("vL")
    private byte vipLevel;

    @SerializedName("pD")
    private Date purchaseDate;

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