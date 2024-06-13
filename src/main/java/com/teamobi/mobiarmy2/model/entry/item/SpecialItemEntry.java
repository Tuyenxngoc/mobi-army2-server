package com.teamobi.mobiarmy2.model.entry.item;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author tuyen
 */
@Getter
@Setter
public class SpecialItemEntry {
    private byte id;
    private int saleIndex;
    private String name;
    private String detail;
    private short[] ability;
    private int priceXu;
    private int priceLuong;
    private int priceSellXu;
    private short expirationDays;
    private boolean showSelection;
    private boolean isOnSale;
    private boolean isGem;
    private boolean isMaterial;
    private boolean isUsable;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialItemEntry that = (SpecialItemEntry) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SpecialItemEntry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}