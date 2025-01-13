package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.server.CharacterManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    private byte type;
    private byte itemIndex;
    private short quantity;
    private short xu;
    private short xp;
    private EquipmentChest equip;
    private SpecialItemChest specialItem;

    public void coins(short xu) {
        this.type = 0;
        this.xu = xu;
    }

    public void items(byte itemIndex, byte quantity) {
        this.type = 1;
        this.itemIndex = itemIndex;
        this.quantity = quantity;
    }

    public void equip() {
        this.type = 2;
        Equipment entry = CharacterManager.getRandomEquip(
                equipmentEntry ->
                        equipmentEntry.isOnSale()
                                && !equipmentEntry.isDisguise()
                                && equipmentEntry.getPriceXu() > 0
                                && equipmentEntry.getPriceXu() < 200_000
        );
        if (entry == null) {
            throw new IllegalStateException("No suitable equipment entry found for reward.");
        }
        equip = new EquipmentChest();
        equip.setEquipment(entry);
    }

    public void xp(short xp) {
        this.type = 3;
        this.xp = xp;
    }

    public void specialItems(byte id, byte quantity) {
        this.type = 4;
        specialItem = new SpecialItemChest();
        specialItem.setItem(SpecialItemManager.getSpecialItemById(id));
        if (specialItem.getItem() == null) {
            throw new IllegalArgumentException("Special item with ID " + id + " does not exist.");
        }
        specialItem.setQuantity(quantity);
    }
}
