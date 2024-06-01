package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.Mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class MissionData {

    public static final Map<Byte, List<Mission>> MISSION_LIST = new HashMap<>();

    public static void addMission(Mission mission) {
        Byte type = mission.getType();
        if (!MISSION_LIST.containsKey(type)) {
            MISSION_LIST.put(type, new ArrayList<>());
        }
        MISSION_LIST.get(type).add(mission);
    }

    public static Mission getMissionById(byte missionId) {
        for (List<Mission> missionList : MISSION_LIST.values()) {
            for (Mission mission : missionList) {
                if (mission.getId() == missionId) {
                    return mission;
                }
            }
        }
        return null;
    }
}
