package com.teamobi.mobiarmy2.model.entry.user;

import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

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
        return equipEntry != null && ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now()) > equipEntry.getExpirationDays();
    }

    /**
     * Gets the number of days since the equipment was purchased.
     *
     * @return the number of days since purchase
     */
    public int getDaysSincePurchase() {
        return (int) ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now());
    }

    /**
     * Returns the number of remaining days for the equipment entry.
     * If there is no equipment entry, it returns 0.
     *
     * @return the number of remaining days for the equipment entry
     */
    public int getRemainingDays() {
        if (equipEntry == null) {
            return 0;
        }
        return Math.max(equipEntry.getExpirationDays() - getDaysSincePurchase(), 0);
    }

    public void decrementEmptySlot() {
        emptySlot--;
        if (emptySlot < 0) {
            emptySlot = 0;
        }
    }

    public void addPoints(short[] ability) {
        for (int i = 0; i < addPoints.length; i++) {
            addPoints[i] += ability[i];
        }
    }

    public void subtractPoints(short[] ability) {
        for (int i = 0; i < addPoints.length; i++) {
            addPoints[i] -= ability[i];
        }
    }

    public void setNewSlot(byte itemId) {
        if (emptySlot <= 0 || emptySlot > 3) {
            return;
        }
        slots[3 - emptySlot] = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentChestEntry that = (EquipmentChestEntry) o;
        return key == that.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}