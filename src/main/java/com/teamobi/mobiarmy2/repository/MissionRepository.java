package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.model.MissionEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class MissionRepository {
    public static final Map<Byte, List<MissionEntry>> MISSION_LIST = new HashMap<>();

    public static void addMission(MissionEntry missionEntry) {
        Byte type = missionEntry.getType();
        if (!MISSION_LIST.containsKey(type)) {
            MISSION_LIST.put(type, new ArrayList<>());
        }
        MISSION_LIST.get(type).add(missionEntry);
    }

    public static MissionEntry getMissionById(byte missionId) {
        for (List<MissionEntry> missionEntryList : MISSION_LIST.values()) {
            for (MissionEntry missionEntry : missionEntryList) {
                if (missionEntry.getId() == missionId) {
                    return missionEntry;
                }
            }
        }
        return null;
    }
}
