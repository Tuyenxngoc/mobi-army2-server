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
public class DataCharacter {

    @SerializedName("xp")
    private int xp;

    @SerializedName("level")
    private int level;

    @SerializedName("point")
    private int point;

    @SerializedName("pointAdd")
    private List<Integer> pointAdd;

    @SerializedName("data")
    private List<Integer> data;

}