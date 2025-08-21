package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.Formula;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.FormulaManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormulaMessageHandler extends BaseMessageHandler {
    public FormulaMessageHandler(Session session) {
        super(session);
    }

    public void handleMergeEquipments(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte id = dis.readByte();
        byte action = dis.readByte();
        if (action == 1) {
            sendFormulaInfo(id);
        } else if (action == 2) {
            byte level = dis.readByte();
            processFormulaCrafting(id, level);
        }
    }

    private void sendFormulaInfo(byte id) throws IOException {
        Map<Byte, List<Formula>> formulaMap = FormulaManager.FORMULAS.get(id);
        if (formulaMap == null) {
            return;
        }
        List<Formula> formulaEntries = formulaMap.get(us().getActiveCharacterId());
        if (formulaEntries == null) {
            return;
        }
        Message ms = new Message(Cmd.FOMULA);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(id);
        ds.writeByte(formulaEntries.size());
        for (Formula formula : formulaEntries) {
            boolean hasRequiredItem = true;
            boolean hasRequiredEquip = us().hasEquipment(formula.getRequiredEquip().getEquipIndex(), formula.getLevel());
            boolean hasRequiredLevel = us().getCurrentLevel() >= formula.getLevelRequired();

            ds.writeByte(formula.getResultEquip().getEquipIndex());
            ds.writeUTF("%s cấp %d".formatted(formula.getResultEquip().getName(), (formula.getLevel() + 1)));
            ds.writeByte(formula.getLevelRequired());
            ds.writeByte(formula.getCharacterId());
            ds.writeByte(formula.getEquipType());
            ds.writeByte(formula.getRequiredItems().size());
            for (SpecialItemChest item : formula.getRequiredItems()) {
                short itemCountInInventory = us().getInventorySpecialItemCount(item.getItem().getId());
                ds.writeByte(item.getItem().getId());
                ds.writeUTF(item.getItem().getName());
                ds.writeByte(item.getQuantity());
                if (itemCountInInventory < item.getQuantity()) {
                    hasRequiredItem = false;
                    ds.writeByte(itemCountInInventory);
                } else {
                    ds.writeByte(item.getQuantity());
                }
            }
            ds.writeByte(formula.getRequiredEquip().getEquipIndex());
            ds.writeUTF(formula.getRequiredEquip().getName());
            ds.writeByte(formula.getLevel());
            ds.writeBoolean(hasRequiredEquip);
            ds.writeBoolean(hasRequiredEquip && hasRequiredItem && hasRequiredLevel);
            ds.writeByte(formula.getDetails().length);
            for (String detail : formula.getDetails()) {
                ds.writeUTF(detail);
            }
        }
        ds.flush();
        sendMessage(ms);
    }

    private void processFormulaCrafting(byte id, byte level) throws IOException {
        Map<Byte, List<Formula>> formulaMap = FormulaManager.FORMULAS.get(id);
        if (formulaMap == null) {
            return;
        }
        List<Formula> formulas = formulaMap.get(us().getActiveCharacterId());
        if (formulas == null) {
            return;
        }
        Formula formula = formulas.get(level);
        if (formula == null) {
            return;
        }

        //Kiểm tra có đủ level chế đồ yêu cầu không
        if (us().getCurrentLevel() < formula.getLevelRequired()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Kiểm tra có trang bị yêu cầu không
        EquipmentChest requiredEquip = us().getEquipment(formula.getRequiredEquip().getEquipIndex(), us().getActiveCharacterId(), formula.getLevel());
        if (requiredEquip == null) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Tạo một danh sách item cần xóa
        List<SpecialItemChest> itemsToRemove = new ArrayList<>();

        //Kiểm tra có đủ item yêu cầu không
        for (SpecialItemChest item : formula.getRequiredItems()) {
            short itemCountInInventory = us().getInventorySpecialItemCount(item.getItem().getId());
            if (itemCountInInventory < item.getQuantity()) {
                sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
                return;
            }
            itemsToRemove.add(item);
        }

        //Kiểm tra có công thức hoặc đủ xu không
        SpecialItemChest material = us().getSpecialItemById(formula.getMaterial().getId());
        if (material == null && us().getXu() < formula.getMaterial().getPriceXu()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        } else {
            if (material != null) {//Nếu có công thức thì thêm vào danh sách item xóa
                itemsToRemove.add(new SpecialItemChest((short) 1, material.getItem()));
            } else {//Nếu chưu có thì trừ xu
                us().updateXu(-formula.getMaterial().getPriceXu());
            }
        }

        //Xoá trang bị và item yêu cầu
        us().updateInventory(null, requiredEquip, null, itemsToRemove);

        //Random chỉ số
        byte[] addPoints = new byte[5];
        byte[] addPercents = new byte[5];
        for (int i = 0; i < 5; i++) {
            addPoints[i] = (byte) Utils.nextInt(formula.getAddPointsMin()[i], formula.getAddPointsMax()[i]);
            addPercents[i] = (byte) Utils.nextInt(formula.getAddPercentsMin()[i], formula.getAddPercentsMax()[i]);
        }

        //Tạo trang bị mới
        EquipmentChest newEquip = new EquipmentChest();
        newEquip.setEquipment(formula.getResultEquip());
        newEquip.setVipLevel((byte) (formula.getLevel() + 1));
        newEquip.setAddPoints(addPoints);
        newEquip.setAddPercents(addPercents);

        //Thêm trang bị vào rương
        us().addEquipment(newEquip);

        //Gửi thông báo
        sendFormulaProcessingResult(GameString.ITEM_CRAFT_SUCCESS);
    }

    private void sendFormulaProcessingResult(String message) throws IOException {
        Message ms = new Message(Cmd.FOMULA);
        DataOutputStream ds = ms.writer();
        ds.writeByte(0);
        ds.writeUTF(message);
        ds.flush();
        sendMessage(ms);
    }
}
