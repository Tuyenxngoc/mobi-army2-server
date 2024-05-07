package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.Room;


public class FightWait {

    public final User[] players;
    public FightManager fight;
    final Room parent;
    public final byte id;
    public final boolean[] readys;
    public final int[][] item;
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
    private Thread kickBoss;
    protected long timeStart;
    public boolean isLH;
    public byte ntLH;
    public byte[] LHMap = new byte[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
    public boolean isSieuBoss;

    public FightWait(Room parent, byte type, byte id, byte maxPlayers, byte maxPlayerInit, byte map, byte teaFree, boolean isLH, boolean isSieuBoss) {
        this.parent = parent;
        this.id = id;
        this.maxPlayer = maxPlayers;
        this.maxPlayerInit = maxPlayerInit;
        this.maxSetPlayer = maxPlayerInit;
        this.numPlayer = 0;
        this.numReady = 0;
        this.players = new User[maxPlayers];
        this.readys = new boolean[maxPlayers];
        this.item = new int[maxPlayers][8];
        this.type = type;
        this.teaFree = teaFree;
        this.money = this.parent.minXu;
        this.name = "";
        this.pass = "";
        this.isLH = isLH;
        this.ntLH = (byte) (isLH ? 0 : -1);
        this.isSieuBoss = isSieuBoss;
        this.map = isLH ? LHMap[ntLH] : map;
        this.fight = new FightManager();
        this.started = false;
        this.boss = -1;
        this.timeStart = 0L;
    }
}