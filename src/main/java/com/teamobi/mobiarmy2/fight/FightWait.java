package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.Room;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author tuyen
 */
public class FightWait {

    public final User[] users;
    public final boolean[] readies;

    public FightManager fight;
    public final Room parent;
    public final byte id;
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
    public byte mapId;
    public int boss;
    private Thread kickBoss;
    protected long timeStart;
    public boolean isLienHoan;
    public byte ntLH;
    public byte[] LHMap = new byte[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39};

    public FightWait(Room parent, byte type, byte id, byte maxPlayers, byte maxPlayerInit, byte mapId, byte teaFree, boolean isLienHoan, boolean isSieuBoss) {
        this.parent = parent;
        this.id = id;
        this.maxPlayer = maxPlayers;
        this.maxPlayerInit = maxPlayerInit;
        this.maxSetPlayer = maxPlayerInit;
        this.numPlayer = 0;
        this.numReady = 0;
        this.users = new User[maxPlayers];
        this.readies = new boolean[maxPlayers];
        this.item = new int[maxPlayers][8];
        this.type = type;
        this.teaFree = teaFree;
        this.money = this.parent.minXu;
        this.name = "";
        this.pass = "";
        this.isLienHoan = isLienHoan;
        this.ntLH = (byte) (isLienHoan ? 0 : -1);
        this.mapId = isLienHoan ? LHMap[ntLH] : mapId;
        this.fight = new FightManager();
        this.started = false;
        this.boss = -1;
        this.timeStart = 0L;
    }

    private User getRoomOwner() {
        return users[boss];
    }

    public void enterFireOval(User us) throws IOException {
        if (type == 6 && us.getClanId() == 0) {
            us.getUserService().sendServerMessage(GameString.notClan());
            return;
        }
        if (started || (isLienHoan && ntLH > 0)) {
            us.getUserService().sendServerMessage(GameString.joinKVError0());
            return;
        }
        if (money > us.getXu()) {
            us.getUserService().sendServerMessage(GameString.joinKVError2());
            return;
        }
        if (numPlayer >= maxSetPlayer) {
            us.getUserService().sendServerMessage(GameString.joinKVError3());
            return;
        }

        Message ms;
        DataOutputStream ds;
        synchronized (users) {
            int bestLocation = -1;
            for (byte i = 0; i < users.length; i++) {
                if (users[i] == null) {
                    bestLocation = i;
                    break;
                }
            }
            if (bestLocation == -1) {
                return;
            }
            us.setFightWait(this);
            if (numPlayer == 0) {
                changeBoss(bestLocation);
            }
            ms = new Message(Cmd.SOMEONE_JOINBOARD);
            ds = ms.writer();
            ds.writeByte(bestLocation);
            ds.writeInt(us.getPlayerId());
            ds.writeShort(us.getClanId());
            ds.writeUTF(us.getUsername());
            ds.writeByte(us.getCurrentLevel());
            ds.writeByte(us.getNvUsed());
            for (short i : us.getEquip()) {
                ds.writeShort(i);
            }
            ds.flush();
            sendToTeam(ms);

            users[bestLocation] = us;
            readies[bestLocation] = false;
            numPlayer++;

            ms = new Message(Cmd.JOIN_BOARD);
            ds = ms.writer();
            ds.writeInt(getRoomOwner().getPlayerId());
            ds.writeInt(money);
            ds.writeByte(mapId);
            ds.writeByte(0);
            for (byte i = 0; i < users.length; i++) {
                User user = users[i];
                if (user != null) {
                    ds.writeInt(user.getPlayerId());
                    ds.writeShort(user.getClanId());
                    ds.writeUTF(user.getUsername());
                    ds.writeInt(0);
                    ds.writeByte(user.getCurrentLevel());
                    ds.writeByte(user.getNvUsed());
                    for (short k : user.getEquip()) {
                        ds.writeShort(k);
                    }
                    ds.writeBoolean(readies[i]);
                } else {
                    ds.writeInt(-1);
                }
            }
            ds.flush();
            us.sendMessage(ms);

            // Update khu vuc
            ms = new Message(76);
            ds = ms.writer();
            ds.writeByte(parent.id);
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.writeByte(parent.type);
            ds.flush();
            us.sendMessage(ms);

            // Send map
            ms = new Message(75);
            ds = ms.writer();
            ds.writeByte(mapId);
            ds.flush();
            us.sendMessage(ms);
        }
    }

    private void changeBoss(int index) {
        boss = index;
        if (kickBoss != null) {
            kickBoss.interrupt();
        }
        kickBoss = new Thread(() -> {
            try {
                int second = 300;
                while (!started && boss != -1) {
                    Thread.sleep(1000L);
                    second--;
                    if (second == 0) {
                        System.out.println("kick " + boss);
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        });
        kickBoss.start();
    }

    public void sendToTeam(Message ms) {
        for (User user : users) {
            if (user != null) {
                user.sendMessage(ms);
            }
        }
    }

    private int getUserIndexByPlayerId(int playerId) {
        for (byte i = 0; i < users.length; i++) {
            User user = this.users[i];
            if (user == null) {
                continue;
            }
            if (user.getPlayerId() == playerId) {
                return i;
            }
        }
        return -1;
    }

    public void setPassRoom(String password, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        //Set new password
        passSet = true;
        pass = password;
    }

    public void setMoney(int xu, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        if (xu < parent.minXu || xu > parent.maxXu) {
            getRoomOwner().getUserService().sendServerMessage(GameString.datCuocError1(parent.minXu, parent.maxXu));
            return;
        }
        if (getRoomOwner().getXu() < xu) {
            getRoomOwner().getUserService().sendServerMessage(GameString.xuNotEnought());
            return;
        }

        // Reset ready
        Arrays.fill(readies, false);
        readies[boss] = true;
        numReady = 0;
        // Set new money
        money = xu;
        try {
            Message ms = new Message(19);
            DataOutputStream ds = ms.writer();
            ds.writeShort(0);
            ds.writeInt(xu);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReady(boolean ready, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() == playerId) {
            return;
        }

        int index = getUserIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }
        // Set new player ready
        if (readies[index] != ready) {
            readies[index] = ready;
            if (ready) {
                numReady++;
            } else {
                numReady--;
            }
        }
        try {
            Message ms = new Message(16);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeBoolean(ready);
            ds.flush();
            this.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kickPlayer(int playerId, int targetPlayerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        int index = getUserIndexByPlayerId(targetPlayerId);
        if (index == -1) {
            return;
        }
        if (readies[index]) {
            return;
        }
        if (users[index].isOpeningGift()) {
            getRoomOwner().getUserService().sendServerMessage(GameString.openingGift(users[index].getUsername()));
        }

        try {
            Message ms = new Message(Cmd.KICK);
            DataOutputStream ds = ms.writer();
            ds.writeShort(index);
            ds.writeInt(targetPlayerId);
            ds.writeUTF(GameString.kickString());
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo leave
    }

}