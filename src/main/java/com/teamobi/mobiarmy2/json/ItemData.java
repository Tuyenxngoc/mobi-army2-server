package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ItemData {

    @SerializedName("id")
    private int id;

    @SerializedName("numb")
    private int numb;
}