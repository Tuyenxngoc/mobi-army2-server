package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserEquipmentDTO {
    private int userEquipmentId;
    private int userId;
    private short equipmentId;
    private byte vipLevel;
    private LocalDateTime purchaseDate;
    private boolean inUse;
    private byte[] slots;
    private byte[] addPoints;
    private byte[] addPercents;
}
