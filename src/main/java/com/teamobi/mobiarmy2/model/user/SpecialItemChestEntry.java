package com.teamobi.mobiarmy2.model.user;

import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialItemChestEntry {
    private static final short MAX_QUANTITY = 30_000;

    private short quantity;
    private SpecialItemEntry item;

    public SpecialItemChestEntry(SpecialItemChestEntry other) {
        this.quantity = other.quantity;
        this.item = other.item;
    }

    public void increaseQuantity(short quantityToAdd) {
        if (quantityToAdd <= 0) {
            return;
        }
        if (quantityToAdd + quantity > MAX_QUANTITY) {
            quantity = MAX_QUANTITY;
            return;
        }
        quantity += quantityToAdd;
    }

    public void decreaseQuantity(int quantityToDecrease) {
        if (quantityToDecrease <= 0) {
            return;
        }
        if (quantityToDecrease > quantity) {
            quantity = 0;
            return;
        }
        quantity -= quantityToDecrease;
    }

    public int getSellPrice() {
        if (item == null) {
            return 0;
        }
        return quantity * item.getPriceSellXu();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialItemChestEntry that = (SpecialItemChestEntry) o;
        return quantity == that.quantity && item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, item);
    }

    @Override
    public String toString() {
        return "SpecialItemChestEntry{" +
                "quantity=" + quantity +
                ", item=" + item +
                '}';
    }

}