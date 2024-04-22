package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.Room;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FightWait {

    private FightManager fightManager;

    public User[] players;
    public Room parent;
    public byte id;
    public boolean[] readys;
    public int[][] item;
    public boolean started;
    public int numReady;
    public int maxSetPlayer;
    public int maxPlayerInit;
    public int maxPlayer;
    public int numPlayer;
    public boolean passSet;
    public String pass;
    public int money;
    public String name;
    public byte type;
    public byte teaFree;
    public byte map;
    public int boss;
    public Thread kickBoss;
    public long timeStart;
    public boolean isLH;
    public byte ntLH;
    public byte[] LHMap = new byte[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
    public boolean isSieuBoss;

    public FightWait(Room room, byte type, byte i, byte maxPlayers, byte maxPlayerInit, byte map, byte nextInt, boolean isLH, boolean isSieuBoss) {

    }
}
