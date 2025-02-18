package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.GameConstants;
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
public class SpecialItemChest {
    private short quantity;
    private SpecialItem item;

    public SpecialItemChest(short quantity, SpecialItem item) {
        setQuantity(quantity);
        this.item = item;
    }

    public SpecialItemChest(SpecialItemChest other) {
        this.quantity = other.quantity;
        this.item = other.item;
    }

    public void setQuantity(short quantity) {
        if (quantity < 0) {
            this.quantity = 0;
        } else if (quantity > GameConstants.MAX_QUANTITY) {
            this.quantity = GameConstants.MAX_QUANTITY;
        } else {
            this.quantity = quantity;
        }
    }

    public void increaseQuantity(short quantityToAdd) {
        if (quantityToAdd <= 0) {
            return;
        }
        int newQuantity = quantity + quantityToAdd;
        if (newQuantity > GameConstants.MAX_QUANTITY) {
            quantity = GameConstants.MAX_QUANTITY;
        } else {
            quantity = (short) newQuantity;
        }
    }

    public void decreaseQuantity(short quantityToDecrease) {
        if (quantityToDecrease <= 0) {
            return;
        }
        int newQuantity = quantity - quantityToDecrease;
        quantity = (short) Math.max(newQuantity, 0);
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
        SpecialItemChest that = (SpecialItemChest) o;
        return quantity == that.quantity && item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, item);
    }

    @Override
    public String toString() {
        return "SpecialItemChest{" +
                "quantity=" + quantity +
                ", item=" + item +
                '}';
    }

}