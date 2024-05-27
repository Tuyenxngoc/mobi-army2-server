package com.teamobi.mobiarmy2.model.giftcode;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;

@Getter
@Setter
public class GetGiftCode {
    private short limit;
    private String code;
    private int[] usedPlayerIds;
    private LocalDateTime expiryDate;
    private String reward;

    public void addUsedPlayerId(int playerId) {
        if (usedPlayerIds == null) {
            usedPlayerIds = new int[]{playerId};
        } else {
            int[] newUsedPlayerIds = Arrays.copyOf(usedPlayerIds, usedPlayerIds.length + 1);
            newUsedPlayerIds[newUsedPlayerIds.length - 1] = playerId;
            usedPlayerIds = newUsedPlayerIds;
        }
    }
}
