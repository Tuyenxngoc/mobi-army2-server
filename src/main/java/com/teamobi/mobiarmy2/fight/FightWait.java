package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.server.Room;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FightWait {

    private FightManager fightManager;

    public FightWait(Room room, byte type, byte i, byte maxPlayers, byte maxPlayerInit, byte map, byte nextInt, boolean isLH) {

    }
}
