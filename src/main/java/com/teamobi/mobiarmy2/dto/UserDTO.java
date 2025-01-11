package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class UserDTO {
    private int userId;
    private LocalDateTime x2XpTime;
    private LocalDateTime lastOnline;
    private int xu;
    private int luong;
    private int cup;
    private int pointEvent;
    private byte materialsPurchased;
    private boolean isChestLocked;
    private boolean isInvitationLocked;
}
