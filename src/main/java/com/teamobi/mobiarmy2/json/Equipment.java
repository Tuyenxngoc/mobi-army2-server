package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Equipment {
    @SerializedName("nvId")
    private int nvId;

    @SerializedName("isUse")
    private boolean isUse;

    @SerializedName("vipLevel")
    private byte vipLevel;

    @SerializedName("invAdd")
    private List<Short> invAdd;

    @SerializedName("equipType")
    private int equipType;

    @SerializedName("dayBuy")
    private String dayBuy;

    @SerializedName("id")
    private int id;

    @SerializedName("slot")
    private List<Integer> slot;

    @SerializedName("percenAdd")
    private List<Short> percenAdd;
}