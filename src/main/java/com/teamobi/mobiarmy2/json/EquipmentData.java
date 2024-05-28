package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentData {
    @SerializedName("nvId")
    private byte nvId;

    @SerializedName("isUse")
    private boolean isUse;

    @SerializedName("vipLevel")
    private byte vipLevel;

    @SerializedName("invAdd")
    private List<Short> invAdd;

    @SerializedName("equipType")
    private byte equipType;

    @SerializedName("dayBuy")
    private String dayBuy;

    @SerializedName("id")
    private byte id;

    @SerializedName("slot")
    private List<Integer> slot;

    @SerializedName("percenAdd")
    private List<Short> percenAdd;
}