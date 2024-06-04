package com.teamobi.mobiarmy2.model.entry.user;

import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentChestEntry {
    private int key;
    private LocalDateTime purchaseDate;
    private byte vipLevel;
    private byte emptySlot;
    private byte[] addPoints;
    private byte[] addPercents;
    private byte[] slots;
    private boolean inUse;
    private EquipmentEntry equipEntry;

    /**
     * Checks if the equipment is expired based on its expiration days and the current date.
     *
     * @return true if the equipment is expired, false otherwise
     */
    public boolean isExpired() {
        if (equipEntry == null) {
            return true;
        }
        long daysSincePurchase = ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now());
        return daysSincePurchase - equipEntry.getExpirationDays() > 0;
    }
}