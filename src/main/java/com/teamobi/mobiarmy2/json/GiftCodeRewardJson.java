package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GiftCodeRewardJson {
    @SerializedName("xu")
    private Integer xu;

    @SerializedName("luong")
    private Integer luong;

    @SerializedName("exp")
    private Integer exp;

    @SerializedName("item")
    private List<SpecialItemChestJson> items;

}
