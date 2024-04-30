package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class MissionData {

    public static class Mission {
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
        public int id;
        public byte idNeed;
        public ArrayList<Mission> missions = new ArrayList<>();
    }

    public static ArrayList<MissDataEntry> entrys = new ArrayList<>();

    public static void addMissionEntry(int id, byte idneed, Mission mEntry) {
        System.out.println("Set mission id=" + id + " idneed=" + idneed);
        MissDataEntry mDatE = null;
        for (MissDataEntry mDatE1 : entrys) {
            if (mDatE1.id == id) {
                mDatE = mDatE1;
                break;
            }
        }
        if (mDatE == null) {
            mDatE = new MissDataEntry();
            mDatE.id = id;
            mDatE.idNeed = idneed;
            mDatE.missions = new ArrayList<>();
            entrys.add(mDatE);
        }
        for (Mission mE : mDatE.missions) {
            if (mE.level == mEntry.level) {
                return;
            }
        }
        mEntry.mDatE = mDatE;
        mDatE.missions.add(mEntry);
    }

    public static Mission getMissionData(int index) {
        for (MissDataEntry mDatE1 : entrys) {
            for (Mission me : mDatE1.missions) {
                if (me.index == index) {
                    return me;
                }
            }
        }
        return null;
    }

}
