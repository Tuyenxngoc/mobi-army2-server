package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.Mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class MissionManager {
    public static final Map<Byte, Mission> MISSIONS = new HashMap<>();
    public static final Map<Byte, List<Byte>> MISSIONS_BY_TYPE = new HashMap<>();

    public static void addMission(Mission mission) {
        MISSIONS.put(mission.getMissionId(), mission);

        MISSIONS_BY_TYPE.computeIfAbsent(mission.getType(), k -> new ArrayList<>())
                .add(mission.getMissionId());
    }

    public static Mission getMissionById(byte missionId) {
        return MISSIONS.get(missionId);
    }

    public static void clear() {
        MISSIONS.clear();
        MISSIONS_BY_TYPE.clear();
    }
}
