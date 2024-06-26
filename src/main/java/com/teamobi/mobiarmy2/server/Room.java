package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.fight.FightWait;

/**
 * @author tuyen
 */
public class Room {
    public byte id;
    public byte type;
    public String name;
    public int maxXu;
    public int minXu;
    public int minMap;
    public int maxMap;
    public FightWait[] fightWaits;
}
