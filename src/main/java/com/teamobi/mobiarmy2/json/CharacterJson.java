package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class CharacterJson {

    @SerializedName("xp")
    private int xp;

    @SerializedName("level")
    private int level;

    @SerializedName("point")
    private int point;

    @SerializedName("pointAdd")
    private short[] pointAdd;

    @SerializedName("data")
    private int[] data;

}