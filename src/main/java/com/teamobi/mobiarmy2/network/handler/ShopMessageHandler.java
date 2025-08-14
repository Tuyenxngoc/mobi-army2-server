package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameConstants;
import com.teamobi.mobiarmy2.common.constant.GameString;
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
        if (user.getFightItems()[itemIndex] + quantity > ApplicationContext.getInstance().getBean(ServerConfig.class).getMaxItem()) {
            return;
        }
        if (unit == 0) {
            int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyXu() * quantity;
            if (user.getXu() < total || total < 0) {
                return;
            }
            user.updateXu(-total);
        } else {
            int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyLuong() * quantity;
            if (user.getLuong() < total || total < 0) {
                return;
            }
            user.updateLuong(-total);
        }
        user.updateFightItems(itemIndex, quantity);
        ms = new Message(Cmd.BUY_ITEM);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(itemIndex);
        ds.writeByte(user.getFightItems()[itemIndex]);
        ds.writeInt(user.getXu());
        ds.writeInt(user.getLuong());
        ds.flush();
        sendMessage(ms);
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    public void handleBuyCharacter(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte index = dis.readByte();
        byte unit = dis.readByte();
        if (index < 0 || index >= user.getOwnedCharacters().length - 3) {
            return;
        }
        index += 3;
        if (user.getOwnedCharacters()[index]) {
            return;
        }
        Character character = CharacterManager.CHARACTERS.get(index);
        if (unit == 0) {
            if (character.getPriceXu() <= 0) {
                return;
            }
            if (user.getXu() < character.getPriceXu()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-character.getPriceXu());
        } else {
            if (character.getPriceLuong() <= 0) {
                return;
            }
            if (user.getLuong() < character.getPriceLuong()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-character.getPriceLuong());
        }

        Optional<Integer> result = userCharacterDAO.create(user.getUserId(), index);
        if (result.isPresent()) {
            UserCharacterDTO userCharacterDTO = userCharacterDAO.findByUserIdAndCharacterId(user.getUserId(), index);
            if (userCharacterDTO != null) {
                user.getLevels()[index] = userCharacterDTO.getLevel();
                user.getXps()[index] = userCharacterDTO.getXp();
                user.getPoints()[index] = userCharacterDTO.getPoints();
                user.getAddedPoints()[index] = userCharacterDTO.getAdditionalPoints();
                user.getUserCharacterIds()[index] = userCharacterDTO.getUserCharacterId();
                user.getOwnedCharacters()[index] = true;
                user.getEquipData()[index] = userCharacterDTO.getData();

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
        if (user.isNotWaiting()) {
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
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
        //Kiểm tra số lượng mua hợp lệ
        if (quantity < 1) {
            return;
        }

        //Kiểm tra số lượng đang có trong rương
        if (user.getInventorySpecialItemCount(itemId) + quantity > serverConfig.getMaxSpecialItemSlots()) {
            sendServerMessage(GameString.CHEST_MAXIMUM_REACHED);
            return;
        }

        SpecialItem item = SpecialItemManager.getSpecialItemById(itemId);
        if (item == null || !item.isOnSale() || (unit == 0 ? item.getPriceXu() : item.getPriceLuong()) < 0) {
            return;
        }

        //Giới hạn số lần mua vật liệu
        if (item.isMaterial()) {
            if (user.getMaterialsPurchased() >= GameConstants.MAX_MATERIAL_PURCHASE_LIMIT) {
                sendServerMessage(GameString.MATERIAL_PURCHASE_LIMIT);
                return;
            } else if (user.getMaterialsPurchased() + quantity > GameConstants.MAX_MATERIAL_PURCHASE_LIMIT) {
                sendServerMessage(GameString.createMaterialPurchaseLimitMessage(GameConstants.MAX_MATERIAL_PURCHASE_LIMIT - user.getMaterialsPurchased()));
                return;
            }
        }

        if (unit == 0) {//Mua bằng xu
            int totalPrice = quantity * item.getPriceXu();
            if (user.getXu() < totalPrice) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-totalPrice);
        } else {//Mua bằng lượng
            int totalPrice = quantity * item.getPriceLuong();
            if (user.getLuong() < totalPrice) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-totalPrice);
        }

        //Xử lý khi mua item đặc biệt
        boolean saveItem = handleSpecialItemPurchase(itemId);

        if (saveItem) {
            //Tạo item mới
            SpecialItemChest newItem = new SpecialItemChest(quantity, item);

            //Thêm vào rương đồ
            user.updateInventory(null, null, List.of(newItem), null);
        }

        //Cập nhật số lượng mua nếu là vật liệu
        if (item.isMaterial()) {
            user.incrementMaterialsPurchased(quantity);
        }

        //Gửi thông báo mua thành công
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    private boolean handleSpecialItemPurchase(byte itemId) {
        if (itemId == 50) {
            user.resetPoints();
            sendCharacterInfo();
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
