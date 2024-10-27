package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.repository.CharacterRepository;
import com.teamobi.mobiarmy2.repository.SpecialItemRepository;
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
    private EquipmentChestEntry equip;
    private SpecialItemChestEntry specialItem;

    public void coins(short xu) {
        this.type = 0;
        this.xu = xu;
    }

    public void equip() {
        this.type = 2;
        EquipmentEntry entry = CharacterRepository.getRandomEquip(
                equipmentEntry ->
                        equipmentEntry.isOnSale()
                                && !equipmentEntry.isDisguise()
                                && equipmentEntry.getPriceXu() > 0
                                && equipmentEntry.getPriceXu() < 200_000
        );
        if (entry == null) {
            throw new IllegalStateException("No suitable equipment entry found for reward.");
        }
        equip = new EquipmentChestEntry();
        equip.setEquipEntry(entry);
    }

    public void items(byte itemIndex, byte quantity) {
        this.type = 1;
        this.itemIndex = itemIndex;
        this.quantity = quantity;
    }

    public void xp(short xp) {
        this.type = 3;
        this.xp = xp;
    }

    public void specialItems(byte id, byte quantity) {
        this.type = 4;
        specialItem = new SpecialItemChestEntry();
        specialItem.setItem(SpecialItemRepository.getSpecialItemById(id));
        if (specialItem.getItem() == null) {
            throw new IllegalArgumentException("Special item with ID " + id + " does not exist.");
        }
        specialItem.setQuantity(quantity);
    }
}
