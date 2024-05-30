package com.teamobi.mobiarmy2.model.item;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialItemEntry {
    private byte id;
    private int indexSale;
    private String name;
    private String detail;
    private short[] ability;
    private int priceXu;
    private int priceLuong;
    private int priceSellXu;
    private short expiration_days;
    private boolean showSelection;
    private boolean isOnSale;
}