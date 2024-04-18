package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataItem {

    @SerializedName("id")
    private int id;

    @SerializedName("numb")
    private int numb;
}