package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class FormulaData {

    public static final class FormulaDataEntry {

        SpecialItemData.SpecialItemEntry ins;
        byte equipType;
        NVData.EquipmentEntry[] equip;
        NVData.EquipmentEntry[] equipNeed;
        ArrayList<FomularEntry> entrys;
    }

    public static class FomularEntry {

        public byte level;
        public int levelRequire;
        public short[] invAddMin;
        public short[] invAddMax;
        public short[] percenAddMin;
        public short[] percenAddMax;
        public SpecialItemData.SpecialItemEntry[] itemNeed;
        public short[] itemNeedNum;
        public String[] detail;
    }

    public static ArrayList<FormulaDataEntry> entrys = new ArrayList<>();

    public static void addFomularEntry(int materialId, byte equipType, short[] eqId, short[] eqNeedId, FomularEntry fEntry) {
        System.out.println("Set fomular materialId=" + materialId + " equipType=" + equipType);
        FormulaDataEntry fDatEntry = null;
        for (FormulaDataEntry fDatEntry2 : entrys) {
            if (fDatEntry2.ins.id == materialId) {
                fDatEntry = fDatEntry2;
                break;
            }
        }
        // Neu ko ton tai -> tao moi
        if (fDatEntry == null) {
            fDatEntry = new FormulaDataEntry();
            fDatEntry.ins = SpecialItemData.getSpecialItemById(materialId);
            fDatEntry.equipType = equipType;
            fDatEntry.equip = new NVData.EquipmentEntry[eqId.length];
            fDatEntry.equipNeed = new NVData.EquipmentEntry[eqNeedId.length];
            for (int i = 0; i < eqId.length; i++) {
                fDatEntry.equip[i] = NVData.getEquipEntryById(i, equipType, eqId[i]);
                fDatEntry.equipNeed[i] = NVData.getEquipEntryById(i, equipType, eqNeedId[i]);
            }
            fDatEntry.entrys = new ArrayList<>();
            entrys.add(fDatEntry);
        }
        // Them fomular entry neu ko ton tai
        for (FomularEntry fE1 : fDatEntry.entrys) {
            // Neu ton tai -> thoat
            if (fE1.level == fEntry.level) {
                return;
            }
        }
        // Neu ko ton tai -> tao moi
        fDatEntry.entrys.add(fEntry);
    }

    public static FormulaDataEntry getFomularDataEntryById(int materialId) {
        for (FormulaDataEntry fDatEntry : entrys) {
            if (fDatEntry.ins.id == materialId) {
                return fDatEntry;
            }
        }
        return null;
    }

}