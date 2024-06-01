package com.teamobi.mobiarmy2.model.entry;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class GiftCodeEntry {
    private short limit;
    private String code;
    private int[] usedPlayerIds;
    private LocalDateTime expiryDate;
    private String reward;
}
