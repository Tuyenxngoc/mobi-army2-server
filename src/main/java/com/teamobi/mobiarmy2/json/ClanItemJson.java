package com.teamobi.mobiarmy2.json;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanItemJson {

    @SerializedName("id")
    private byte id;

    @SerializedName("time")
    private LocalDateTime time;

}
