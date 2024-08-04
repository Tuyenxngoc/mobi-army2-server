package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiftCodeRewardJson {

    @SerializedName("x")
    private int xu;

    @SerializedName("l")
    private int luong;

    @SerializedName("xp")
    private int exp;

    @SerializedName("i")
    private List<SpecialItemChestJson> items;

    @SerializedName("e")
    private List<EquipmentChestJson> equips;

}
