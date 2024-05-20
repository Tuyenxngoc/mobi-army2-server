package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiftCodeRewardData {
    @SerializedName("xu")
    private int xu;

    @SerializedName("luong")
    private int luong;

    @SerializedName("exp")
    private int exp;

    @SerializedName("item")
    private List<ItemData> items;

}
