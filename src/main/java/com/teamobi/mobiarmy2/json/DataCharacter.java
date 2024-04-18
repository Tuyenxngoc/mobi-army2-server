package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataCharacter {
    @SerializedName("data")
    private List<Integer> data;

    @SerializedName("xp")
    private int xp;

    @SerializedName("pointAdd")
    private List<Integer> pointAdd;

    @SerializedName("lever")
    private int lever;

    @SerializedName("point")
    private int point;
}