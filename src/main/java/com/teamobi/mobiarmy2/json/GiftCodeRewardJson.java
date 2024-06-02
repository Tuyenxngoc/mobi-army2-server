package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiftCodeRewardJson {

    @SerializedName("xu")
    private int xu;

    @SerializedName("luong")
    private int luong;

    @SerializedName("exp")
    private int exp;

    @SerializedName("item")
    private List<SpecialItemChestJson> items;

    @SerializedName("equip")
    private List<EquipmentChestJson> equips;

}
