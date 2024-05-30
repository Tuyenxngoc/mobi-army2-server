package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class SpecialItemChestJson {

    @SerializedName("id")
    private short id;

    @SerializedName("quantity")
    private short quantity;

}