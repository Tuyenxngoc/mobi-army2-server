package com.teamobi.mobiarmy2.model.entry.user;

import com.teamobi.mobiarmy2.model.entry.item.SpecialItemEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialItemChestEntry {
    private static final short MAX_QUANTITY = 30_000;

    public short quantity;
    public SpecialItemEntry item;

    public void increaseQuantity(int quantityToAdd) {
        if (quantityToAdd < 0) {
            return;
        }
        if (quantityToAdd + quantity > MAX_QUANTITY) {
            quantity = MAX_QUANTITY;
            return;
        }
        quantity += quantityToAdd;
    }

    public void decreaseQuantity(int quantityToDecrease) {
        if (quantityToDecrease < 0) {
            return;
        }
        if (quantityToDecrease > quantity) {
            quantity = 0;
            return;
        }
        quantity -= quantityToDecrease;
    }
}