package com.teamobi.mobiarmy2.dto;

import com.teamobi.mobiarmy2.model.EquipmentChest;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class UserDTO {
    private int userId;
    private Short clanId;
    private int xu;
    private int luong;
    private int cup;
    private boolean isChestLocked;
    private boolean isInvitationLocked;
    private byte activeCharacterId;
    private int pointEvent;
    private byte materialsPurchased;
    private short equipmentPurchased;
    private LocalDateTime xpX2Time;
    private LocalDateTime lastOnline;
    private int topEarningsXu;
    private byte[] items;
    private int[] mission;
    private byte[] missionLevel;
    private List<Integer> friends;
    private List<SpecialItemChest> specialItemChest = new ArrayList<>();
    private List<EquipmentChest> equipmentChest = new ArrayList<>();
}
