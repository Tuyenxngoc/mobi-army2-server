package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class GiftCodeDTO {
    private long giftCodeId;
    private short limit;
    private LocalDateTime expiryDate;
    private int xu;
    private int luong;
    private int exp;
}
