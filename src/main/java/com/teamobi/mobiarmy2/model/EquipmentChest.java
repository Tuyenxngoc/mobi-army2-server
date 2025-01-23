package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentChest {
    private int key;
    private LocalDateTime purchaseDate;
    private byte vipLevel;
    private byte emptySlot;
    private byte[] addPoints;
    private byte[] addPercents;
    private byte[] slots;
    private boolean inUse;
    private Equipment equipment;

    public int getDaysSincePurchase() {
        return (int) ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now());
    }

    public boolean isExpired() {
        return getRemainingDays() <= 0;
    }

    public int getRemainingDays() {
        if (equipment == null) {
            return 0;
        }
        return Math.max(equipment.getExpirationDays() - getDaysSincePurchase(), 0);
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
        EquipmentChest that = (EquipmentChest) o;
        return key == that.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}