package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.UserCharacterDAO;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.entity.Character;
import com.teamobi.mobiarmy2.entity.Equipment;
import com.teamobi.mobiarmy2.entity.SpecialItem;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class ShopMessageHandler extends BaseMessageHandler {
    public static final int MAX_MATERIAL_PURCHASE_LIMIT = 20;

    private final UserCharacterDAO userCharacterDAO;

    public ShopMessageHandler(Session session, UserCharacterDAO userCharacterDAO) {
        super(session);
        this.userCharacterDAO = userCharacterDAO;
    }

    public void handlePurchaseItem(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte unit = dis.readByte();
        byte itemIndex = dis.readByte();
        byte quantity = dis.readByte();
        if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
            return;
        }
        if (us().getFightItems()[itemIndex] + quantity > GameConstants.MAX_FIGHT_ITEM_QUANTITY) {
            return;
        }
        if (unit == 0) {
            int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyXu() * quantity;
            if (us().getXu() < total || total < 0) {
                return;
            }
            us().updateXu(-total);
        } else {
            int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyLuong() * quantity;
            if (us().getLuong() < total || total < 0) {
                return;
            }
            us().updateLuong(-total);
        }
        us().updateFightItems(itemIndex, quantity);
        ms = new Message(Cmd.BUY_ITEM);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(itemIndex);
        ds.writeByte(us().getFightItems()[itemIndex]);
        ds.writeInt(us().getXu());
        ds.writeInt(us().getLuong());
        ds.flush();
        sendMessage(ms);
        us().sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    public void handleBuyCharacter(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte index = dis.readByte();
        byte unit = dis.readByte();
        if (index < 0 || index >= us().getOwnedCharacters().length - 3) {
            return;
        }
        index += 3;
        if (us().getOwnedCharacters()[index]) {
            return;
        }
        Character character = CharacterManager.CHARACTERS.get(index);
        if (unit == 0) {
            if (character.getPriceXu() <= 0) {
                return;
            }
            if (us().getXu() < character.getPriceXu()) {
                us().sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-character.getPriceXu());
        } else {
            if (character.getPriceLuong() <= 0) {
                return;
            }
            if (us().getLuong() < character.getPriceLuong()) {
                us().sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateLuong(-character.getPriceLuong());
        }

        Optional<Integer> result = userCharacterDAO.create(us().getUserId(), index);
        if (result.isPresent()) {
            UserCharacterDTO userCharacterDTO = userCharacterDAO.findByUserIdAndCharacterId(us().getUserId(), index);
            if (userCharacterDTO != null) {
                us().getLevels()[index] = userCharacterDTO.getLevel();
                us().getXps()[index] = userCharacterDTO.getXp();
                us().getPoints()[index] = userCharacterDTO.getPoints();
                us().getAddedPoints()[index] = userCharacterDTO.getAdditionalPoints();
                us().getUserCharacterIds()[index] = userCharacterDTO.getUserCharacterId();
                us().getOwnedCharacters()[index] = true;
                us().getEquipData()[index] = userCharacterDTO.getData();

                ms = new Message(Cmd.BUY_GUN);
                DataOutputStream ds = ms.writer();
                ds.writeByte(index - 3);
                ds.flush();
                sendMessage(ms);
            }
        }
    }

    public void handleSendShopEquipments() throws IOException {
        Message ms = CacheManager.cachedShopEquipments;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        ms = new Message(Cmd.SHOP_EQUIP);
        DataOutputStream ds = ms.writer();
        List<Short> equipIds = EquipmentManager.SALE_INDEX_TO_ID;
        ds.writeShort(equipIds.size());
        for (Short id : equipIds) {
            Equipment equip = EquipmentManager.getEquipment(id);
            ds.writeByte(equip.getCharacterId());
            ds.writeByte(equip.getEquipType());
            ds.writeShort(equip.getEquipIndex());
            ds.writeUTF(equip.getName());
            ds.writeInt(equip.getPriceXu());
            ds.writeInt(equip.getPriceLuong());
            ds.writeByte(equip.getExpirationDays());
            ds.writeByte(equip.getLevelRequirement());
        }
        ds.flush();

        CacheManager.cachedShopEquipments = ms;

        sendMessage(ms);
    }

    public void handleSpecialItemShop(Message ms) throws IOException {
        if (us().isNotWaiting()) {
            return;
        }
        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        if (type == 0) {
            sendSpecialItem();
        } else {
            byte unit = dis.readByte();
            byte itemId = dis.readByte();
            byte quantity = dis.readByte();
            purchaseSpecialItem(unit, itemId, quantity);
        }
    }

    private void purchaseSpecialItem(byte unit, byte itemId, byte quantity) {
        // Kiểm tra số lượng mua hợp lệ
        if (quantity < 1) {
            return;
        }

        // Kiểm tra số lượng đang có trong rương
        if (us().getInventorySpecialItemCount(itemId) + quantity > GameConstants.MAX_SPECIAL_ITEM_SLOTS) {
            us().sendServerMessage(GameString.CHEST_MAXIMUM_REACHED);
            return;
        }

        SpecialItem item = SpecialItemManager.getSpecialItemById(itemId);
        if (item == null || !item.isOnSale() || (unit == 0 ? item.getPriceXu() : item.getPriceLuong()) < 0) {
            return;
        }

        // Giới hạn số lần mua vật liệu
        if (item.isMaterial()) {
            if (us().getMaterialsPurchased() >= MAX_MATERIAL_PURCHASE_LIMIT) {
                us().sendServerMessage(GameString.MATERIAL_PURCHASE_LIMIT);
                return;
            } else if (us().getMaterialsPurchased() + quantity > MAX_MATERIAL_PURCHASE_LIMIT) {
                us().sendServerMessage(GameString.createMaterialPurchaseLimitMessage(
                        MAX_MATERIAL_PURCHASE_LIMIT - us().getMaterialsPurchased()));
                return;
            }
        }

        if (unit == 0) {//Mua bằng xu
            int totalPrice = quantity * item.getPriceXu();
            if (us().getXu() < totalPrice) {
                us().sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-totalPrice);
        } else {//Mua bằng lượng
            int totalPrice = quantity * item.getPriceLuong();
            if (us().getLuong() < totalPrice) {
                us().sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateLuong(-totalPrice);
        }

        //Xử lý khi mua item đặc biệt
        boolean saveItem = handleSpecialItemPurchase(itemId);

        if (saveItem) {
            //Tạo item mới
            SpecialItemChest newItem = new SpecialItemChest(quantity, item);

            //Thêm vào rương đồ
            us().updateInventory(null, null, List.of(newItem), null);
        }

        //Cập nhật số lượng mua nếu là vật liệu
        if (item.isMaterial()) {
            us().incrementMaterialsPurchased(quantity);
        }

        //Gửi thông báo mua thành công
        us().sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    private boolean handleSpecialItemPurchase(byte itemId) {
        if (itemId == 50) {
            us().resetPoints();
            us().sendCharacterInfo();
            return false;
        }

        return true;
    }

    private void sendSpecialItem() throws IOException {
        Message ms = CacheManager.cachedSpecialItemShop;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        ms = new Message(Cmd.SHOP_LINHTINH);
        DataOutputStream ds = ms.writer();
        Map<Byte, SpecialItem> sorted = new TreeMap<>(SpecialItemManager.SPECIAL_ITEMS);
        for (SpecialItem specialItem : sorted.values()) {
            if (!specialItem.isOnSale()) {
                continue;
            }
            ds.writeByte(specialItem.getId());
            ds.writeUTF(specialItem.getName());
            ds.writeUTF(specialItem.getDetail());
            ds.writeInt(specialItem.getPriceXu());
            ds.writeInt(specialItem.getPriceLuong());
            ds.writeByte(specialItem.getExpirationDays());
            ds.writeByte(specialItem.isShowSelection() ? 0 : 1);
        }
        ds.flush();

        CacheManager.cachedSpecialItemShop = ms;

        sendMessage(ms);
    }
}
