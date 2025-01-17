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

    @SerializedName("i")
    private byte id;

    @SerializedName("t")
    private LocalDateTime time;

}
