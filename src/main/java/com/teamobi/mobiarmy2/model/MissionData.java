package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;

public class MissionData {

    public static class MissionEntry {

        public int index;
        public byte level;
        public String name;
        public int require;
        public String reward;
        public int rewardXu;
        public int rewardLuong;
        public int rewardXP;
        public int rewardCUP;
        public MissDataEntry mDatE;
    }

    public static class MissDataEntry {

        int id;
        byte idNeed;
        ArrayList<MissionEntry> entrys = new ArrayList<>();
    }

    public static ArrayList<MissDataEntry> entries = new ArrayList<>();

    public static void addMissionEntry(int id, byte idneed, MissionEntry mEntry) {
        System.out.println("Set mission id=" + id + " idneed=" + idneed);
        MissDataEntry mDatE = null;
        for (MissDataEntry mDatE1 : entries) {
            if (mDatE1.id == id) {
                mDatE = mDatE1;
                break;
            }
        }
        if (mDatE == null) {
            mDatE = new MissDataEntry();
            mDatE.id = id;
            mDatE.idNeed = idneed;
            mDatE.entrys = new ArrayList<>();
            entries.add(mDatE);
        }
        for (MissionEntry mE : mDatE.entrys) {
            if (mE.level == mEntry.level) {
                return;
            }
        }
        mEntry.mDatE = mDatE;
        mDatE.entrys.add(mEntry);
    }

    public static MissionEntry getMissionData(int index) {
        for (MissDataEntry mDatE1 : entries) {
            for (MissionEntry me : mDatE1.entrys) {
                if (me.index == index) {
                    return me;
                }
            }
        }
        return null;
    }

}
