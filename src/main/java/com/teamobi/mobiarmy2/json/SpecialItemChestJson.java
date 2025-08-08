package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialItemChestJson {

    @SerializedName("i")
    private byte id;

    @SerializedName("q")
    private short quantity;

}