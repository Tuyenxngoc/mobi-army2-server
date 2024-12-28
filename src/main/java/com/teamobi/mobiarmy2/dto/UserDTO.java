package com.teamobi.mobiarmy2.dto;

import com.teamobi.mobiarmy2.model.EquipmentChest;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
@Getter
@Setter
public class UserDTO {
    private String userId;
    private int playerId;
    private String username;
    private Short clanId;
    private int xu;
    private int luong;
    private int cup;
    private boolean isLock;
    private boolean isActive;
    private boolean isChestLocked;
    private boolean isInvitationLocked;
    private byte activeCharacterId;
    private int pointEvent;
    private byte materialsPurchased;
    private short equipmentPurchased;
    private LocalDateTime xpX2Time;
    private LocalDateTime lastOnline;
    private long[] playerCharacterIds;
    private boolean[] ownedCharacters;
    private int[] levels;
    private byte[] levelPercents;
    private int[] xps;
    private int[] points;
    private short[][] addedPoints;
    private byte[] items;
    private int[][] equipData;
    private int[] mission;
    private byte[] missionLevel;
    private EquipmentChest[][] characterEquips;
    private List<Integer> friends;
    private List<SpecialItemChest> specialItemChest;
    private Map<Integer, EquipmentChest> equipmentChest;
    private int topEarningsXu;

    public EquipmentChest getEquipmentByKey(int key) {
        return equipmentChest.get(key);
    }

    public void initialize(int totalCharacter) {
        this.playerCharacterIds = new long[totalCharacter];
        this.ownedCharacters = new boolean[totalCharacter];
        this.levels = new int[totalCharacter];
        this.levelPercents = new byte[totalCharacter];
        this.xps = new int[totalCharacter];
        this.points = new int[totalCharacter];
        this.addedPoints = new short[totalCharacter][5];
        this.equipData = new int[totalCharacter][6];
        this.characterEquips = new EquipmentChest[totalCharacter][6];
        this.specialItemChest = new ArrayList<>();
        this.equipmentChest = new HashMap<>();
    }
}
