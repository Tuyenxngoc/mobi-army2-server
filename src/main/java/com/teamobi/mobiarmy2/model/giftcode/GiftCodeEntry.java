package com.teamobi.mobiarmy2.model.giftcode;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GiftCodeEntry {

    private short limit;

    private String code;

    private int[] usedPlayerIds;

    private LocalDateTime expiryDate;

    private String reward;

}
