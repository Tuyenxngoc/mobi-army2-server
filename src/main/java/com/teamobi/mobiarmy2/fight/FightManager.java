package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.fight.BulletManager.AddBoss;
import com.teamobi.mobiarmy2.fight.BulletManager.BomHenGio;
import com.teamobi.mobiarmy2.fight.BulletManager.Bullets;
import com.teamobi.mobiarmy2.fight.BulletManager.VoiRong;
import com.teamobi.mobiarmy2.fight.boss.*;
import com.teamobi.mobiarmy2.fight.bullet.ItemBomHenGio;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class FightManager {

    final FightWait wait;
    private boolean isShoot;
    private int teamlevel;
    private int teamCS;
    public boolean isNextTurn;
    private Date matchTime;
    protected byte type;
    protected boolean isBossTurn;
    protected byte bossTurn;
    protected byte playerTurn;
    protected int nTurn;
    protected byte nHopQua;
    protected int playerCount;
    public int allCount;
    protected int WindX;
    protected int WindY;
    protected boolean isFight;
    protected final byte timeCountMax = 30;
    public Player[] players;
    public MapManager mapMNG;
    public BulletManager bullMNG;
    public CountDownMNG countDownMNG;

    public FightManager(FightWait fo) {
        this.isShoot = false;
        this.isNextTurn = true;
        this.wait = fo;
        this.type = fo.type;
        this.playerCount = 0;
        this.allCount = 0;
        this.playerTurn = -1;
        this.isBossTurn = false;
        this.bossTurn = 0;
        this.WindX = 0;
        this.WindY = 0;
        this.nHopQua = 0;
        this.players = new Player[ServerManager.maxElementFight];
        this.isFight = false;
        this.mapMNG = new MapManager(this);
        this.bullMNG = new BulletManager(this);
        this.countDownMNG = new CountDownMNG(this, timeCountMax);
    }

    protected void setMap(byte map) {
        this.mapMNG.setMapId(map);
    }

    void sendToTeam(Message ms) throws IOException {
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            pl.us.sendMessage(ms);
        }
    }

    public void removeUser(User us) {
        synchronized (this.players) {
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                if (this.players[i].us.getPlayerId() == us.getPlayerId()) {
                    this.players[i] = null;
                    break;
                }
            }
        }
    }

    public int getIndexByIDDB(int iddb) {
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            if (this.players[i] != null && this.players[i].us != null && this.players[i].us.getPlayerId() == iddb) {
                return i;
            }
        }
        return -1;
    }

    public int getIDTurn() {
        if (isBossTurn && type == 5) {
            return this.bossTurn;
        } else {
            return this.playerTurn;
        }
    }

    public Player getPlayerTurn() {
        if (isBossTurn) {
            return this.players[this.bossTurn];
        }
        return this.players[this.playerTurn];
    }

    public Player getPlayerClosest(short X, short Y) {
        int XClosest = -1;
        Player plClosest = null;
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.isDie) {
                continue;
            }
            int kcX = Math.abs(pl.X - X);
            if (XClosest == -1 || kcX < XClosest) {
                XClosest = kcX;
                plClosest = pl;
            }
        }
        return plClosest;
    }

    public Boss getBossClosest(short X, short Y) {
        int XClosest = -1;
        Boss bsClosest = null;
        int bossLen = this.allCount - ServerManager.maxPlayers;
        for (byte i = 0; i < bossLen; i++) {
            Boss boss = (Boss) this.players[ServerManager.maxPlayers + i];
            if (boss == null || boss.isDie || boss.name == "PET" || boss.name == "Box Gift Falling" || boss.name == "Box Gift") {
                continue;
            }
            int kcX = Math.abs(boss.X - X);
            if (XClosest == -1 || kcX < XClosest) {
                XClosest = kcX;
                bsClosest = boss;
            }
        }
        return bsClosest;
    }

    public int getWindX() {
        return this.WindX;
    }

    public int getWindY() {
        return this.WindY;
    }

    public int getLevelTeam() {
        return this.teamlevel;
    }

    public int getCSTeam() {
        return this.teamCS;
    }

    protected boolean getisLH() {
        return this.wait.isLH;
    }

    private void nextBoss() throws IOException {
        //Map Bom 1
        if (this.wait.mapId == 30) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) ((i % 2 == 0) ? Utils.nextInt(95, 315) : Utils.nextInt(890, 1070));
                short Y = (short) (50 + 40 * Utils.nextInt(3));
                players[allCount] = new BigBoom(this, (byte) 12, "SmallBoom", (byte) allCount, 1500 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }
        //Map Bom 2
        if (this.wait.mapId == 31) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) (Utils.nextInt(445, 800) + i * 50);
                short Y = 180;
                players[allCount] = new BigBoom(this, (byte) 12, "SmallBoom", (byte) allCount, 1500 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }
        //map nhen may
        if (this.wait.mapId == 32) {
            byte numBoss = (new byte[]{2, 2, 3, 3, 4, 4, 5, 5, 5})[playerCount];
            short[] tempX = new short[]{505, 1010, 743, 425, 1068};
            short[] tempY = new short[]{221, 221, 198, 369, 369, 369};
            for (byte i = 0; i < numBoss; i++) {
                players[allCount] = new SpiderMachine(this, (byte) 13, "Spider Robot", (byte) allCount, 4785 + (getLevelTeam() * 15), (short) tempX[i], (short) tempY[i]);
                allCount++;
            }
        }
        //map thanh pho may
        if (this.wait.mapId == 33) {
            byte numBoss = (new byte[]{2, 2, 3, 3, 4, 4, 5, 5, 6})[playerCount];
            short[] tempX = new short[]{420, 580, 720, 240, 55, 900};
            for (int i = 0; i < numBoss; i++) {
                short X = tempX[i];
                short Y = (short) 200;
                players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }
        //Map T-rex Máy
        if (this.wait.mapId == 34) {
            short X = 880;
            short Y = 400;
            players[allCount] = new Trex(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 10), X, Y);
            allCount++;

            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 6, 7, 7, 8})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                X = (short) (Utils.nextInt(470, 755));
                Y = 400;
                players[allCount] = new BigBoom(this, (byte) 12, "BigBooom", (byte) allCount, 1500 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }
        //Map KV cam
        if (this.wait.mapId == 35) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) (Utils.nextInt(300, 800));
                short Y = (short) Utils.nextInt(-350, 100);
                players[allCount] = new UFO(this, (byte) 16, "UFO", (byte) allCount, 4500 + (this.getLevelTeam() * 12), X, Y);
                allCount++;
            }
        }
        //Map HMLS
        if (this.wait.mapId == 36) {
            short X = (short) (Utils.nextInt(300, 800));
            short Y = (short) Utils.nextInt(-350, 100);
            players[allCount] = new Balloon(this, (byte) 17, "Balloon", (byte) allCount, 1, X, Y);
            allCount++;
            players[allCount] = new Balloon_Gun(this, (byte) 18, "Balloon Gun", (byte) allCount, 2000 + (this.getLevelTeam() * 10), (short) (X + 51), (short) (Y + 19));
            allCount++;
            players[allCount] = new Balloon_GunBig(this, (byte) 19, "Balloon Gun Big", (byte) allCount, 2500 + (this.getLevelTeam() * 10), (short) (X - 5), (short) (Y + 30));
            allCount++;
            players[allCount] = new Balloon_FanBack(this, (byte) 20, "Fan Back", (byte) allCount, 1000 + (this.getLevelTeam() * 10), (short) (X - 67), (short) (Y - 6));
            allCount++;
        }

        //map nhen doc
        if (this.wait.mapId == 37) {
            byte numBoss = (new byte[]{2, 3, 3, 4, 4, 5, 5, 6, 6})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) Utils.nextInt(20, this.mapMNG.Width - 20);
                short Y = (short) 250;
                players[allCount] = new SpiderPoisonous(this, (byte) 22, "Spider Poisonous", (byte) allCount, 3800 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }

        //map Nghia trang 1
        if (this.wait.mapId == 38) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) ((short) 700 - i * 80);
                short Y = (short) (Utils.nextInt(30));
                players[allCount] = new Ghost(this, (byte) 25, "Ghost", (byte) allCount, 1800 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }

        //map Nghia trang 2
        if (this.wait.mapId == 39) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) (700 - i * 80);
                short Y = (short) Utils.nextInt(30);
                players[allCount] = new Ghost2(this, (byte) 26, "Ghost II", (byte) allCount, 1800 + (this.getLevelTeam() * 10), X, Y);
                allCount++;
            }
        }

        //Super Boss
        if (this.wait.mapId == 40) {
            short X = (short) (Utils.nextInt(300, 800));
            short Y = (short) Utils.nextInt(-350, 50);
            players[allCount] = new Balloon(this, (byte) 17, "Balloon", (byte) allCount, 1, X, Y);
            allCount++;
            players[allCount] = new Balloon_Gun(this, (byte) 18, "Balloon Gun", (byte) allCount, 2000 + (this.getLevelTeam() * 10), (short) (X + 51), (short) (Y + 19));
            allCount++;
            players[allCount] = new Balloon_GunBig(this, (byte) 19, "Balloon Gun Big", (byte) allCount, 2500 + (this.getLevelTeam() * 10), (short) (X - 5), (short) (Y + 30));
            allCount++;
            players[allCount] = new Balloon_FanBack(this, (byte) 20, "Fan Back", (byte) allCount, 1000 + (this.getLevelTeam() * 10), (short) (X - 67), (short) (Y - 6));
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 142, (short) 83);
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 1069, (short) 83);
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 105, (short) 51);
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 1099, (short) 51);
            allCount++;
            players[allCount] = new Trex(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 10), (short) 217, (short) 260);
            allCount++;
            players[allCount] = new Trex(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 10), (short) 985, (short) 260);
            allCount++;
            for (byte i = 0; i < 10; i++) {
                X = (short) ((i % 2 == 0) ? Utils.nextInt(75, 150) : Utils.nextInt(1050, 1125));
                Y = (short) 200;
                players[allCount] = new BigBoom(this, (byte) 12, "BigBoom", (byte) allCount, 1000 + (this.getLevelTeam() * 10), X, Y);
                allCount++;

            }

        }
        //TG Boss
        if (this.wait.mapId == 41) {
            short X = 600;
            short Y = 99;
            players[allCount] = new TrexTG(this, (byte) 15, "Trex-TG", (byte) allCount, 15000 + (this.getLevelTeam() * 1300), X, Y);
            allCount++;

        }
        if (this.wait.mapId == 42) {
            for (byte i = 0; i < 30; i++) {
                short X = (short) (Utils.nextInt(300, 800));
                short Y = (short) Utils.nextInt(0, 1);
                X = (short) ((i % 2 == 0) ? Utils.nextInt(20, 530) : Utils.nextInt(950, 1450));
                Y = (short) 500;
                players[allCount] = new BigBoomHTCC(this, (byte) 12, "BigBoomHTCC", (byte) allCount, 1000 + (this.getLevelTeam() * 60), X, Y);
                allCount++;

            }
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 356, (short) 381);
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 485, (short) 337);
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 1010, (short) 335);
            allCount++;
            players[allCount] = new Robot(this, (byte) 14, "Robot", (byte) allCount, 3700 + (this.getLevelTeam() * 10), (short) 1139, (short) 385);
            allCount++;
            players[allCount] = new TrexTG(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 400), (short) 1330, (short) 335);
            allCount++;
            players[allCount] = new TrexTG(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 400), (short) 167, (short) 335);
            allCount++;
        }
        if (this.wait.mapId == 43) {
            short X = (short) (Utils.nextInt(275, 830));
            short Y = (short) Utils.nextInt(95, 128);
            players[allCount] = new Balloon(this, (byte) 17, "Balloon", (byte) allCount, 1, X, Y);
            allCount++;
            players[allCount] = new Balloon_Gun(this, (byte) 18, "Balloon Gun", (byte) allCount, 5000 + (this.getLevelTeam() * 100), (short) (X + 51), (short) (Y + 19));
            allCount++;
            players[allCount] = new Balloon_GunBig(this, (byte) 19, "Balloon Gun Big", (byte) allCount, 5500 + (this.getLevelTeam() * 100), (short) (X - 5), (short) (Y + 30));
            allCount++;
            players[allCount] = new Balloon_FanBack(this, (byte) 20, "Fan Back", (byte) allCount, 4000 + (this.getLevelTeam() * 100), (short) (X - 67), (short) (Y - 6));
            allCount++;
            players[allCount] = new TrexTG(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 200), (short) 187, (short) 136);
            allCount++;
            players[allCount] = new TrexTG(this, (byte) 15, "T-rex", (byte) allCount, 15000 + (this.getLevelTeam() * 200), (short) 903, (short) 135);
            allCount++;
        }
        if (this.wait.mapId == 44) {
            for (byte i = 0; i < 5; i++) {
                short X = (short) (Utils.nextInt(361, 710));
                short Y = (short) (172);
                players[allCount] = new Monkey(this, (byte) 3, "Monkey", (byte) allCount, 1500000 + (this.getLevelTeam() * 25000), X, Y
                );
                allCount++;

            }
        }
        int bossLen = this.allCount - ServerManager.maxPlayers;
        Message ms = new Message(89);
        DataOutputStream ds = ms.writer();
        ds.writeByte(bossLen);
        for (byte i = 0; i < bossLen; i++) {
            Boss boss = (Boss) this.players[ServerManager.maxPlayers + i];
            ds.writeInt(-1);
            ds.writeUTF(boss.name);
            ds.writeInt(boss.HPMax);
            ds.writeByte(boss.idNV);
            ds.writeShort(boss.X);
            ds.writeShort(boss.Y);
        }
        ds.flush();
        this.sendToTeam(ms);
    }

    private void nextAngry() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            if (pl.isUpdateAngry) {
                ms = new Message(113);
                ds = ms.writer();
                ds.writeByte(i);
                ds.writeByte(pl.angry);
                ds.flush();
                this.sendToTeam(ms);
                pl.isUpdateAngry = false;
            }
        }
    }

    private void calcMM() {
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            pl.nextMM();
        }
    }

    private void nextMM() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            if (pl.isMM) {
                if (i == this.playerTurn || pl.isUpdateHP) {
                    ms = new Message(100);
                    ds = ms.writer();
                    ds.writeByte(i);
                    ds.flush();
                    this.sendToTeam(ms);
                }
                pl.isMM = false;
            }
        }
    }

    private void nextBiDoc() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            if (pl.isBiDoc) {
                ms = new Message(108);
                ds = ms.writer();
                ds.writeByte(pl.index);
                ds.flush();
                this.sendToTeam(ms);
            }
        }
    }

    private void nextCantSee() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            if (pl.cantSeeCount > 0) {
                ms = new Message(106);
                ds = ms.writer();
                ds.writeByte(0);
                ds.writeByte(pl.index);
                ds.flush();
                this.sendToTeam(ms);
            }
        }
    }

    private void nextCantMove() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            if (pl.cantMoveCount > 0) {
                ms = new Message(107);
                ds = ms.writer();
                ds.writeByte(0);
                ds.writeByte(pl.index);
                ds.flush();
                this.sendToTeam(ms);
            }
        }
    }

    private void nextHP() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < this.allCount; i++) {
            Player pl = this.players[i];
            if (pl == null) {
                continue;
            }
            if (pl.isUpdateHP) {
                ms = new Message(51);
                ds = ms.writer();
                ds.writeByte(i);
                ds.writeShort(pl.HP);
                ds.writeByte(pl.pixel);
                ds.flush();
                this.sendToTeam(ms);
                pl.isUpdateHP = false;
            }
        }
        this.nextGift();
        this.nextXP();
        this.nextCUP();
        this.nextAngry();
    }

    private void nextXP() {
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            if (pl.isUpdateXP) {
                int oldXP = pl.us.getCurrentXp();
                pl.us.updateXp(pl.XPUp, true);
                int newXP = pl.us.getCurrentXp();
                pl.AllXPUp += newXP - oldXP;
                pl.XPUp = 0;
                pl.isUpdateXP = false;
            }
        }
    }

    private void nextCUP() {
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            if (pl.isUpdateCup) {
                pl.us.updateCup(pl.CupUp);
                pl.CupUp = 0;
                pl.isUpdateCup = false;
            }
        }
    }

    private void nextWind() throws IOException {
        Player pl = this.players[this.getIDTurn()];
        if (pl.ngungGioCount > 0) {
            this.WindX = 0;
            this.WindY = 0;
            pl.ngungGioCount--;
        } else {
            if (Utils.nextInt(0, 100) > 25) {
                this.WindX = Utils.nextInt(-70, 70);
                this.WindY = Utils.nextInt(-70, 70);
            }
        }
        Message ms = new Message(25);
        DataOutputStream ds = ms.writer();
        ds.writeByte(WindX);
        ds.writeByte(WindY);
        ds.flush();
        this.sendToTeam(ms);
    }

    private void huyVoHinh(byte index) throws IOException {
        Message ms = new Message(80);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.flush();
        this.sendToTeam(ms);
    }

    private void huyCantSee(byte index) throws IOException {
        Message ms = new Message(106);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(index);
        ds.flush();
        this.sendToTeam(ms);
    }

    /*
    private void huyCantMove(int index) throws IOException {
        Message ms = new Message(107); DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(index);
        ds.flush();
        this.sendToTeam(ms);
    }
     */
    public void nextTurn() throws IOException {
        if (!this.isNextTurn) {
            return;
        }
//        if (nTurn > 0 && this.nHopQua > 0) {
//            for (byte i = 0; i < 1; i++) 
//        } //else if (nTurn > 0 && this.mapMNG.Id == 30) {
//            for (byte i = 0; i < 1; i++) {
//                short x, y;
//                x = (short) Utils.nextInt(20, this.mapMNG.Width - 20);
//                y = (short) Utils.nextInt(100, 100);
//                this.addBoss(new GiftBoxFalling(this, (byte) 23, "Box Gift Falling", (byte) allCount, 1, x, y));
//                this.nHopQua--;
//            }
//        }
        this.nTurn++;
        // Update XY Player
        for (byte i = 0; i < this.allCount; i++) {
            Player pl = players[i];
            if (pl == null) {
                continue;
            }
            pl.chuanHoaXY();
        }

        //kiem tra xem ai bi thieu dot tru hp va lan bi dot
        for (byte i = 0; i < this.allCount; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.isDie) {
                continue;
            }
            for (byte j = 0; j < ServerManager.maxPlayers; j++) {
                if (pl.NHTItemThieuDot[j][0] == 0) {
                    continue;
                }
                pl.NHTItemThieuDot[j][0]--;
                pl.updateHP(-(1 + (pl.HPMax * 1 / 100)));
                this.nextHP();
            }
        }

        if (this.countDownMNG != null) {
            this.countDownMNG.stopCount();
        }
        if (this.playerTurn == -1) {
            while (true) {
                int next = 0;
                if (this.type != 5) {
                    next = Utils.nextInt(ServerManager.maxPlayers);
                } else {
                    next = Utils.nextInt(this.allCount);
                }
                if (this.players[next] != null && this.players[next].idNV != 18 && this.players[next].idNV != 19 && this.players[next].idNV != 20 && this.players[next].idNV != 21 && this.players[next].idNV != 23 && this.players[next].idNV != 24) {
                    if (next < ServerManager.maxPlayers) {
                        this.playerTurn = (byte) next;
                        this.isBossTurn = false;
                        this.bossTurn = ServerManager.maxPlayers;
                    } else {
                        this.bossTurn = (byte) next;
                        this.isBossTurn = true;
                        this.playerTurn = 0;
                    }
                    break;
                }
            }
        } else {
            this.nextAngry();
            if (!isBossTurn) {
                Player plTurn = null;
                if (this.playerTurn >= 0 && this.playerTurn < this.players.length) {
                    plTurn = this.players[this.playerTurn];
                }
                if (plTurn != null) {
                    this.players[this.playerTurn].isUsePow = false;
                    this.players[this.playerTurn].isUseItem = false;
                    this.players[this.playerTurn].itemUsed = -1;
                }
            }
            if (this.type == 5) {
                if (this.isBossTurn) {
                    this.isBossTurn = false;
                    int turn = this.playerTurn + 1;
                    while (turn != this.playerTurn) {
                        if (turn == ServerManager.maxPlayers) {
                            turn = 0;
                        }
                        if (this.players[turn] != null && !this.players[turn].isDie && this.players[turn].idNV != 18 && this.players[turn].idNV != 19 && this.players[turn].idNV != 20 && this.players[turn].idNV != 21 && this.players[turn].idNV != 23 && this.players[turn].idNV != 24) {
                            this.playerTurn = (byte) turn;
                            break;
                        }
                        turn++;
                    }
                } else {
                    this.isBossTurn = true;
                    byte turn = (byte) (this.bossTurn + 1);
                    while (turn != this.bossTurn) {
                        if (turn >= this.allCount) {
                            turn = ServerManager.maxPlayers;
                        }
                        if (this.players[turn] != null && !this.players[turn].isDie && this.players[turn].idNV != 18 && this.players[turn].idNV != 19 && this.players[turn].idNV != 20 && this.players[turn].idNV != 21 && this.players[turn].idNV != 23 && this.players[turn].idNV != 24) {
                            this.bossTurn = turn;
                            break;
                        }
                        turn++;
                    }
                }
            } else {
                byte turn = (byte) (this.playerTurn + 1);
                while (turn != this.playerTurn) {
                    if (turn == this.allCount) {
                        turn = 0;
                    }
                    if (this.players[turn] != null && !this.players[turn].isDie && this.players[turn].idNV != 18 && this.players[turn].idNV != 19 && this.players[turn].idNV != 20 && this.players[turn].idNV != 21 && this.players[turn].idNV != 23 && this.players[turn].idNV != 24) {
                        this.playerTurn = (byte) turn;
                        break;
                    }
                    turn++;
                }
            }
        }

        if (!isBossTurn) {
            this.isShoot = false;
            Player pl = this.players[this.playerTurn];
            pl.buocDi = 0;
            if (pl.hutMauCount > 0) {
                pl.hutMauCount--;
            }
            if (pl.voHinhCount > 0) {
                pl.voHinhCount--;
                if (pl.voHinhCount == 0) {
                    huyVoHinh(this.playerTurn);
                }
            }
            if (pl.tangHinhCount > 0) {
                pl.tangHinhCount--;
                if (pl.tangHinhCount == 0) {
                    huyVoHinh(this.playerTurn);
                }
            }
            if (pl.cantSeeCount > 0) {
                pl.cantSeeCount--;
                if (pl.cantSeeCount == 0) {
                    huyCantSee(this.playerTurn);
                }
            }
            if (pl.cantMoveCount > 0) {
                pl.cantMoveCount--;
                if (pl.cantMoveCount == 0) {
                    huyCantSee(this.playerTurn);
                }
            }
            if (pl.isBiDoc) {
                pl.updateHP(-150);
            }
            pl.updateAngry(10);
        } else {
            Player pl = this.players[this.bossTurn];
            pl.buocDi = 0;
        }
        if (this.bullMNG.hasVoiRong) {
            for (byte i = 0; i < this.bullMNG.voiRongs.size(); i++) {
                VoiRong vr = this.bullMNG.voiRongs.get(i);
                vr.count--;
                if (vr.count < 0) {
                    this.bullMNG.voiRongs.remove(i);
                    i--;
                }
            }
            if (this.bullMNG.voiRongs.isEmpty()) {
                this.bullMNG.hasVoiRong = false;
            }
        }
        if (this.bullMNG.boms.size() > 0) {
            for (byte i = 0; i < this.bullMNG.boms.size(); i++) {
                BomHenGio bom = this.bullMNG.boms.get(i);
                bom.count--;
                if (bom.count == 1) {
                    this.bullMNG.exploreBom(i);
                    i--;
                }
            }
        }
        if (this.bullMNG.addboss.size() > 0) {
            for (byte i = 0; i < this.bullMNG.addboss.size(); i++) {
                AddBoss bos = this.bullMNG.addboss.get(i);
                this.addBoss(bos.players);
                players[allCount - 1].XPExist = bos.XPE;
            }
            this.bullMNG.addboss.clear();
        }
        if (this.bullMNG.buls.size() > 0) {
            for (byte i = 0; i < this.bullMNG.buls.size(); i++) {
                Bullets bul = this.bullMNG.buls.get(i);
                this.bullMNG.addBom((ItemBomHenGio) bul.bull);
            }
            this.bullMNG.buls.clear();
        }
        if (!checkWin() && players[this.getIDTurn()].isDie) {
            nextTurn();
            return;
        }
        Message ms = new Message(24);
        DataOutputStream ds = ms.writer();
        ds.writeByte(this.isBossTurn ? this.bossTurn : this.playerTurn);
        ds.flush();
        this.sendToTeam(ms);
        this.nextWind();
        this.countDownMNG.resetCount();
        if (this.isBossTurn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ((Boss) players[bossTurn]).turnAction();
                }
            }).start();
        }
    }

    public boolean checkWin() throws IOException {
        if (!isFight) {
            return true;
        }
        // Next HP
        nextHP();
        nextCantSee();
        nextCantMove();
        nextBiDoc();
        if (FightManager.this.type == 5) {
            byte nPlayerAlive = 0, nBossAlive = 0, i = 0;
            while (i < ServerManager.maxPlayers) {
                Player pl2 = players[i];
                if (pl2 != null && !pl2.isDie) {
                    nPlayerAlive++;
                }
                i++;
            }
            while (i < allCount) {
                Boss boss = (Boss) this.players[i];
                if (boss != null && !boss.isDie && boss.name != "PET" && boss.name != "Box Gift Falling" && boss.name != "Box Gift") {
                    nBossAlive++;
                }
                i++;
            }
            if (nPlayerAlive == 0 || nBossAlive == 0) {
                if (nPlayerAlive == nBossAlive && nPlayerAlive == 0) {
                    if (isBossTurn) {
                        fightComplete((byte) -1);
                    } else {
                        fightComplete((byte) 1);
                    }
                } else if (nPlayerAlive == 0) {
                    fightComplete((byte) -1);
                } else {
                    fightComplete((byte) 1);
                }
            } else {
                return false;
            }
        } else {
            int nRedAlive = 0, nBlueAlive = 0;
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl2 = players[i];
                if (pl2 == null) {
                    continue;
                }
                if (!pl2.isDie) {
                    if (pl2.team) {
                        nBlueAlive++;
                    } else {
                        nRedAlive++;
                    }
                }
            }
            if (nRedAlive == 0 || nBlueAlive == 0) {
                if ((nRedAlive == nBlueAlive) && (nRedAlive == 0)) {
                    fightComplete((byte) 0);
                } else if (nRedAlive == 0) {
                    fightComplete((byte) 1);
                } else {
                    fightComplete((byte) -1);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void fightComplete(byte checkWin) throws IOException {
        this.isFight = false;
        this.WindX = 0;
        this.WindY = 0;
        this.nHopQua = 0;
        this.bullMNG.hasVoiRong = false;
        this.bullMNG.voiRongs.clear();
        this.bullMNG.boms.clear();
        this.bullMNG.addboss.clear();
        boolean LHfinish = false;
        boolean LHSuccess = false;
        if (wait.isLH && wait.numPlayers > 0) {
            if (checkWin == 1) {
                wait.continuousLevel++;
                if (wait.continuousLevel == wait.LHMap.length) {
                    wait.continuousLevel = 0;
                    LHfinish = true;
                } else {
                    LHSuccess = true;
                }
            } else {
                wait.continuousLevel = 0;
            }
            wait.mapId = wait.LHMap[wait.continuousLevel];
        }
        if (this.type == 5 && checkWin == 1) {
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl = this.players[i];
                if (pl != null && pl.us != null) {
                    pl.us.updateXp(10, true);
                }
            }
        }
        Message ms;
        DataOutputStream ds;
        // Update Win
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            byte win = (byte) (pl.team ? checkWin : -checkWin);
            if (win == 1 && nTurn > 2) {
                if (this.playerCount == 2) {
                    pl.us.updateMission(0, 1);
                } else if (this.playerCount >= 5) {
                    pl.us.updateMission(17, 1);
                }
                switch (pl.idNV) {
                    case 0:
                        pl.us.updateMission(13, 1);
                        break;
                    case 1:
                        pl.us.updateMission(14, 1);
                        break;
                    case 2:
                        pl.us.updateMission(15, 1);
                        break;
                    default:
                        break;
                }
                // UFO
                switch (this.mapMNG.Id) {
                    case 35:
                        pl.us.updateMission(2, 1);
                        break;
                    case 36:
                        pl.us.updateMission(3, 1);
                        break;
                    case 38:
                    case 39:
                        pl.us.updateMission(4, 1);
                        break;
                    default:
                        break;
                }
            }
            ms = new Message(50);
            ds = ms.writer();
            // Team win->0: hoa 1: win -1: thua
            ds.writeByte(win);
            // Null byte
            ds.writeByte(0);
            // money Bonus
            ds.writeInt(this.wait.money);
            ds.flush();
            pl.us.sendMessage(ms);
        }

        // Update All XP and CUP
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            ms = new Message(97);
            ds = ms.writer();
            ds.writeInt(pl.AllXPUp);
            ds.writeInt(pl.us.getCurrentXp());
            ds.writeInt(pl.us.getCurrentLevel() * (pl.us.getCurrentLevel() + 1) * 1000);
            ds.writeByte(0);
            ds.writeByte(pl.us.getCurrentLevelPercent());
            ds.flush();
            pl.us.sendMessage(ms);
        }
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            ms = new Message(-24);
            ds = ms.writer();
            ds.writeByte(pl.AllCupUp);
            ds.writeInt(pl.us.getCup());
            ds.flush();
            pl.us.sendMessage(ms);
        }
        // Update Xu
        if (this.wait.money > 0) {
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.us == null) {
                    continue;
                }
                byte win = (byte) (pl.team ? checkWin : -checkWin);
                if (win >= 0) {
                    pl.us.updateXu(this.wait.money * (win == 1 ? 2 : 1));
                    ms = new Message(52);
                    ds = ms.writer();
                    ds.writeInt(pl.us.getPlayerId());
                    ds.writeInt(this.wait.money * (win == 1 ? 2 : 1));
                    ds.writeInt(pl.us.getXu());
                    ds.flush();
                    sendToTeam(ms);
                }
            }
        }
        this.wait.started = false;
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
        }
        this.wait.fightComplete();
        if (wait.isLH) {
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.us == null) {
                    continue;
                }
                if (pl.isDie) {
                    wait.kick(i);
                    continue;
                }
                String strItem = "";
                int[] arXP = new int[]{0, 500, 2500, 5000, 20000, 35000, 45000, 65000, 80000, 100000, 500000};
                int[] arXu = new int[]{0, 5000, 7000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 100000};
                int[] arLg = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                if (LHfinish) {
                    System.out.println("LHfinish");
                } else if (LHSuccess) {
                    System.out.println("LHSuccess");
                } else {
                    System.out.println("LH failed");
                }
            }
        }
        if (nTurn > 2 && wait.type < 5) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
            }
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.us == null) {
                    continue;
                }
                byte win = (byte) (pl.team ? checkWin : -checkWin);
                if (win == 1) {
//                    pl.us.updateQua_Start(2, 30, true);
                }
            }
        }
        this.nTurn = 0;
    }

    protected void startGame(int nTeamPointBlue, int nTeamPointRed) throws IOException {
        if (this.isFight) {
            return;
        }
        this.mapMNG.entrys.clear();
        this.setMap(this.wait.mapId);
        this.playerTurn = -1;
        this.nTurn = 0;
        this.WindX = 0;
        this.WindY = 0;
        this.isFight = true;

        this.playerCount = this.wait.numPlayers;
        this.allCount = ServerManager.maxPlayers;
        if (this.type == 5) {
            this.nHopQua = (byte) (this.playerCount / 2);
        }
        int[] location = new int[8];
        int count = 0;
        this.teamCS = 0;
        this.teamlevel = 0;
        for (byte i = 0; i < this.wait.maxPlayer; i++) {
            User us = this.wait.users[i];
            if (us == null) {
                this.players[i] = null;
                continue;
            }
            this.teamlevel += us.getCurrentXpLevel();
            us.updateXu(-this.wait.money);
            short X, Y;
            byte[] item;
            int teamPoint;
            boolean exists;
            int locaCount = -1;
            do {
                locaCount = Utils.nextInt(this.mapMNG.XPlayerInit.length);
                exists = false;
                for (int j = 0; j < count; j++) {
                    if (location[j] == locaCount) {
                        exists = true;
                        break;
                    }
                }
            } while (exists);
            location[count++] = locaCount;
            X = this.mapMNG.XPlayerInit[locaCount];
            Y = this.mapMNG.YPlayerInit[locaCount];
            item = this.wait.items[i];
            for (byte j = 0; j < 4; j++) {
                if (item[4 + j] > 0) {
                    if (12 + j > 1) {
                        us.updateItems((byte) (12 + j), (byte) -1);
                    }
                }
            }
            if (this.type == 5 || i % 2 == 0) {
                teamPoint = nTeamPointBlue;
            } else {
                teamPoint = nTeamPointRed;
            }
            this.players[i] = new Player(this, i, X, Y, item, teamPoint, us);
        }
        this.bullMNG.mangNhenId = 200;
        this.sendFightInfoMessage();
        if (this.type == 5) {
            nextBoss();
        }
        this.matchTime = new Date();
        this.nextTurn();
    }

    public void leave(int playerId) throws IOException {
        if (!this.isFight) {
            return;
        }
        int index = this.getIndexByIDDB(playerId);
        if (index == -1) {
            return;
        }
        Player pl = this.players[index];

        Message ms = new Message(9);
        DataOutputStream ds = ms.writer();
        ds.writeInt(playerId);
        ds.writeUTF(GameString.leave2(pl.us.getUsername()));
        ds.flush();
        sendToTeam(ms);
        if (!pl.isDie) {
            pl.HP = 0;
            pl.isUpdateHP = true;
            pl.isDie = true;
        }
        pl.us = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!checkWin()) {
                        if (index == getIDTurn()) {
                            nextTurn();
                        } else {
                            Message ms = new Message(24);
                            DataOutputStream ds = ms.writer();
                            ds.writeByte(isBossTurn ? bossTurn : playerTurn);
                            ds.flush();
                            sendToTeam(ms);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    protected void sendFightInfoMessage() throws IOException {
        if (!this.isFight) {
            return;
        }
        // Update Xu
        if (this.wait.money > 0) {
            for (byte i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.us == null) {
                    continue;
                }
                Message ms = new Message(52);
                DataOutputStream ds = ms.writer();
                ds.writeInt(pl.us.getPlayerId());
                ds.writeInt(-this.wait.money);
                ds.writeInt(pl.us.getXu());
                ds.flush();
                this.sendToTeam(ms);
            }
        }
        for (byte i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            Message ms = new Message(20);
            DataOutputStream ds = ms.writer();
            ds.writeByte(wait.mapId);
            ds.writeByte(this.timeCountMax);
            // Team point
            ds.writeShort(pl.dongDoi);
            // X, Y, HP
            for (byte j = 0; j < 8; j++) {
                Player pl2 = this.players[j];
                if (pl2 == null) {
                    ds.writeShort(-1);
                    continue;
                }
                ds.writeShort(pl2.X);
                ds.writeShort(pl2.Y);
                ds.writeShort(pl2.HPMax);
            }
            ds.flush();
            pl.us.sendMessage(ms);
        }
    }

    protected void countOut() throws IOException {
        if (this.isFight && !this.checkWin()) {
            nextTurn();
        }
    }

    public void changeLocation(int index) throws IOException {
        Player pl = this.players[index];
        ServerManager.getInstance().logger().logMessage("Player " + index + " change location X=" + pl.X + " Y=" + pl.Y);
        Message ms = new Message(21);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeShort(pl.X);
        ds.writeShort(pl.Y);
        ds.flush();
        sendToTeam(ms);
        if (pl.Y > this.mapMNG.Height) {
            pl.isDie = true;
            pl.HP = 0;
            pl.isUpdateHP = true;
            if (!checkWin() && index == getIDTurn()) {
                nextTurn();
            }
        }
    }

    public void flyChangeLocation(int index) throws IOException {
        Player pl = this.players[index];
        Message ms = new Message(93);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeShort(pl.X);
        ds.writeShort(pl.Y);
        ds.flush();
        sendToTeam(ms);
    }

    public void addBoss(Player pl) throws IOException {
        if (allCount >= ServerManager.maxElementFight) {
            return;
        }
        players[allCount] = pl;
        Boss boss = (Boss) this.players[allCount];
        Message ms = new Message(89);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeInt(-1);
        ds.writeUTF(boss.name);
        ds.writeInt(boss.HPMax);
        ds.writeByte(boss.idNV);
        ds.writeShort(boss.X);
        ds.writeShort(boss.Y);
        ds.flush();
        this.sendToTeam(ms);
        allCount++;
    }

    public void newShoot(int index, byte bullId, short arg, byte force, byte force2, byte nshoot) throws IOException {
        ServerManager.getInstance().logger().logMessage("New shoot index=" + index + " bullId: " + bullId + " arg: " + arg + " force: " + force + " force2: " + force2 + " nshoot: " + nshoot);
        final Player pl = this.players[index];
        short x = pl.X, y = pl.Y;
        this.calcMM();
        bullMNG.addShoot(pl, bullId, arg, force, force2, nshoot);
        bullMNG.fillXY();
        this.nextMM();
        ArrayList<Bullet> bullets = bullMNG.entrys;
        if (bullets.isEmpty()) {
            return;
        }
        bullId = bullMNG.entrys.get(0).bullId;
        Message ms = new Message(22);
        DataOutputStream ds = ms.writer();
        // typeshoot
        byte typeshoot = 0;
        // Type shoot 0: pem buoc nhay 1: pem tang dan
        ds.writeByte(typeshoot);
        // Ban pow
        ds.writeByte(pl.isUsePow ? 1 : 0);
        // id trong phong
        ds.writeByte(index);
        // id dan
        ds.writeByte(bullId);
        // x, y, goc
        ds.writeShort(x);
        ds.writeShort(y);
        ds.writeShort(arg);
        // Apa or chicky: send force 2
        if (bullId == 17 || bullId == 19) {
            ds.writeByte(bullMNG.force2);
        }
        // dan laser
        if (bullId == 14 || bullId == 40) {
            // Goc
            ds.writeByte(0);
            // Null byte
            ds.writeByte(0);
        }
        // Send goc
        if (bullId == 44 || bullId == 45 || bullId == 47) {
            ds.writeByte(0);
        }
        // So lan ban
        ds.writeByte(nshoot);
        // So dan
        ds.writeByte(bullets.size());

        for (Bullet bull : bullets) {
            if (bullMNG.typeSC > 0 && pl.us != null) {
                pl.us.updateMission(12, 1);
            }
            ArrayList<Short> X = bull.XArray;
            ArrayList<Short> Y = bull.YArray;

            // Length
            ds.writeShort(X.size());

            if (typeshoot == 0) {
                for (int j = 0; j < X.size(); j++) {
                    if (j == 0) {
                        // Toa do x, y dau
                        ds.writeShort(X.get(0));
                        ds.writeShort(Y.get(0));
                    } else {
                        if ((j == X.size() - 1) && bullId == 49) {
                            ds.writeShort(X.get(j));
                            ds.writeShort(Y.get(j));
                            ds.writeByte(bullMNG.mgtAddX);
                            ds.writeByte(bullMNG.mgtAddY);
                            break;
                        }
                        // Buoc nhay
                        ds.writeByte((byte) (X.get(j) - X.get(j - 1)));
                        ds.writeByte((byte) (Y.get(j) - Y.get(j - 1)));
                    }
                }
            } else if (typeshoot == 1) {
                for (int j = 0; j < X.size(); j++) {
                    // Toa do x, y thu j
                    ds.writeShort(X.get(j));
                    ds.writeShort(Y.get(j));
                }
            }
            if (bullId == 48) {
                // Lent
                ds.writeByte(1);
                for (int j = 0; j < 1; j++) {
                    // xHit, yHit
                    ds.writeShort(0);
                    ds.writeShort(0);
                }
            }
        }

        // Type Sieu cao
        if (bullId == 42) {
            bullMNG.typeSC = 0;
        }
        ds.writeByte(bullMNG.typeSC);
        if (bullMNG.typeSC == 1 || bullMNG.typeSC == 2) {
            // X, Y super
            ds.writeShort(bullMNG.XSC);
            ds.writeShort(bullMNG.YSC);
        }
        ds.flush();
        bullMNG.reset();
        this.sendToTeam(ms);
        pl.isUseItem = false;
        pl.itemUsed = -1;
        if (this.isNextTurn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (pl != null && !(pl instanceof Boss)) {
                            pl.netWait();
                        }
                        if (!checkWin()) {
                            nextTurn();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void chatMessage(User us, Message ms) throws IOException {
        int index = this.getIndexByIDDB(us.getPlayerId());
        if (index == -1) {
            return;
        }
        String s = ms.reader().readUTF();
        ms = new Message(9);
        DataOutputStream ds = ms.writer();
        ds.writeInt(us.getPlayerId());
        ds.writeUTF(s);
        ds.flush();
        this.sendToTeam(ms);
    }

    public void changeLocationMessage(User us, Message ms) throws IOException {
        int index = this.getIndexByIDDB(us.getPlayerId());
        if (index == -1) {
            return;
        }
        Player pl = this.players[index];
        short x = ms.reader().readShort();
        short y = ms.reader().readShort();
        pl.updateXY(x, y);
        changeLocation(index);
    }

    public void shootMessage(User us, Message ms) throws IOException {
        int index = this.getIndexByIDDB(us.getPlayerId());
        if (index == -1 || index != this.playerTurn || this.isShoot || !wait.started) {
            return;
        }
        this.isShoot = true;
        Player pl = this.players[index];
        DataInputStream dis = ms.reader();
        // id dan
        byte bullId = dis.readByte();
        short x = dis.readShort();
        short y = dis.readShort();
        short arg = dis.readShort();
        // 2 luc
        byte force = dis.readByte();
        byte force2 = 0;
        // Neu la apa or chicky -> 2 luc
        if (bullId == 17 || bullId == 19) {
            force2 = dis.readByte();
        }
        // so lan ban
        byte nshoot = dis.readByte();
        if (pl.banX2) {
            nshoot = 2;
            pl.banX2 = false;
        } else {
            nshoot = 1;
        }
        if (x != pl.X && y != pl.Y) {
            pl.updateXY(x, y);
        }
        newShoot(index, bullId, (arg > 360 ? 360 : (arg < -360 ? -360 : arg)), (force > 30 ? 30 : (force < 0 ? 0 : force)), (force2 > 30 ? 30 : (force2 < 0 ? 0 : force2)), nshoot);
    }

    public void boLuotMessage(User us) throws IOException {
        int index = this.getIndexByIDDB(us.getPlayerId());
        if (index == -1 || index != this.playerTurn || checkWin()) {
            return;
        }
        // Set next turn
        nextTurn();
    }

    public void useItemMessage(User us, Message ms) throws IOException {
        int index = this.getIndexByIDDB(us.getPlayerId());
        if (index == -1 || index != this.playerTurn) {
            return;
        }
        byte idItem = ms.reader().readByte();
//        if (idItem < 0 || idItem > ItemData.nItemDcMang.length - 1 && idItem != 100) {
//            return;
//        }
        Player pl = this.players[index];
        if (pl == null || pl.isUseItem) {
            return;
        }
        if (this.type == 5 && (idItem == 9 || idItem == 26 || idItem == 23 || idItem == 28 || idItem == 30 || idItem == 31)) {
            ms = new Message(45);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(GameString.unauthorizedItem());
            ds.flush();
            us.sendMessage(ms);
            return;
        }
//        if (this.wait.map == 42 && (idItem == 2 || idItem == 3 || idItem == 4 || idItem >= 6 && idItem <= 9 || idItem == 11 || idItem == 16 || idItem >= 18 && idItem <= 23 || idItem == 26 || idItem == 28 || idItem >= 29 && idItem <= 31)) {
//            ms = new Message(45);
//            DataOutputStream ds = ms.writer();
//            ds.writeUTF(GameString.unauthorized_Item());
//            ds.flush();
//            us.sendMessage(ms);
//            return;
//        }
        int indexItem = -1;
        if (idItem != 100) {
            for (byte i = 0; i < pl.item.length; i++) {
                if (pl.item[i] == idItem) {
                    indexItem = i;
                }
            }
            if (indexItem == -1) {
                return;
            }
        }
        ms = new Message(26);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeByte(idItem);
        ds.flush();
        this.sendToTeam(ms);
        pl.isUseItem = true;
        pl.itemUsed = idItem;
        //fix item
        if (indexItem >= 0) {
            if (idItem > 1) {
                pl.us.updateItems(idItem, (byte) -1);
            }
            pl.item[indexItem] = -1;
        }
        // HP
        if (idItem == 0) {
            pl.updateHP(350);
            this.nextHP();
        }
        //ban X2
        if (idItem == 2) {
            pl.banX2 = true;
        }
        // Di X2
        if (idItem == 3) {
            pl.diX2 = true;
        }
        // Tang hinh
        if (idItem == 4) {
            pl.tangHinhCount = 5;
        }
        // Ngung gio
        if (idItem == 5) {
            pl.ngungGioCount = 5;
            this.nextWind();
        }
        // HP dong doi
        if (idItem == 10) {
            byte lent = (byte) (type == 5 ? 1 : 2);
            byte i = (byte) (pl.team ? 0 : 1);
            for (; i < ServerManager.maxPlayers; i += lent) {
                Player pl2 = this.players[i];
                if (pl2 == null || pl2.isDie) {
                    continue;
                }
                pl2.updateHP(300);
            }
            this.nextHP();
        }
        // Tu sat
        if (idItem == 24) {
            newShoot(index, (byte) 50, (short) 0, (byte) 0, (byte) 0, (byte) 1);
        }
        //item UFO new UFOFire(this, (byte) 16, "UFO", (byte) allCount, 980 + (pl.us.getLevel() * 20), (short) 100, (short) 0, pl, (byte) 3));
        if (idItem == 27) { //new Ghost(this, (byte) 25, "Ghost", (byte) allCount, 1800 + (this.getLevelTeam() * 10), X, Y);

        }
        // HP 50%
        if (idItem == 32) {
            pl.updateHP(pl.HPMax / 2);
            this.nextHP();
        }
        // HP 100%
        if (idItem == 33) {
            pl.updateHP(pl.HPMax);
            this.nextHP();
        }
        // Vo hinh
        if (idItem == 34) {
            pl.voHinhCount = 3;
        }
        // Ma ca rong
        if (idItem == 35) {
            pl.hutMauCount = 3;
        }
        // Pow
        if (idItem == 100) {
            if (pl.angry == 100) {
                pl.updateAngry(-100);
                pl.isUsePow = true;
            }
        }
        if (idItem == 0 || idItem == 2 || idItem == 3 || idItem == 4 || idItem == 5 || idItem == 10 || idItem == 32 || idItem == 33 || idItem == 34 || idItem == 35 || idItem == 100) {
            pl.itemUsed = -1;
        }
    }

    public void removeBullMessage(User us, Message ms) throws IOException {
        int[] X, Y;
        int lent = ms.reader().readByte();
        X = new int[lent];
        Y = new int[lent];
        for (byte i = 0; i < lent; i++) {
            X[i] = ms.reader().readInt();
            Y[i] = ms.reader().readInt();
        }
    }

    public void addBom(int id, int X, int Y) throws IOException {
        Message ms = new Message(109);
        DataOutputStream ds = ms.writer();
        ds.writeByte(0);
        ds.writeByte(id);
        ds.writeInt(X);
        ds.writeInt(Y);
        ds.flush();
        this.sendToTeam(ms);
    }

    public void exploreBom(int id, int X, int Y, Bullet bull) throws IOException {
        this.mapMNG.collision((short) X, (short) Y, bull);
        Message ms = new Message(109);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(id);
        ds.flush();
        this.sendToTeam(ms);
    }

    public void updateCantSee(Player pl) throws IOException {
        pl.cantSeeCount = 5;
    }

    public void updateCantMove(Player pl) throws IOException {
        pl.cantMoveCount = 5;
    }

    public void updateBiDoc(Player pl) throws IOException {
        pl.isBiDoc = true;
    }

    public short[] getForceArgXY(int idGun, BulletManager bull, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100) {
        byte i = (byte) (Utils.nextInt(2) == 0 ? -1 : 1);
        short argS = (short) (i == 1 ? arg : 180 - arg);
        byte forceS = (byte) force;
        do {
            short x, y, vx, vy;
            x = (short) (X + (20 * Utils.cos(argS) >> 10));
            y = (short) (Y - 12 - (20 * Utils.sin(argS) >> 10));
            vx = (short) (forceS * Utils.cos(argS) >> 10);
            vy = (short) -(forceS * Utils.sin(argS) >> 10);
            short ax100 = (short) (bull.fm.WindX * msg / 100);
            short ay100 = (short) (bull.fm.WindY * msg / 100);
            short vxTemp = 0, vyTemp = 0, vyTemp2 = 0;

            if (idGun == 13) {
                y -= 25;
            }
            while (true) {
                if ((x < -200) || (x > bull.fm.mapMNG.Width + 200) || (y > bull.fm.mapMNG.Height + 200)) {
                    break;
                }
                short preX = x, preY = y;
                x += vx;
                y += vy;
                byte collision = getCollisionPoint(bull, preX, preY, x, y, toX, toY, Mx, My, isXuyenMap);
                if (collision == 1) {
                    return new short[]{argS, forceS};
                } else if (collision == 2) {
                    break;
                }
                vxTemp += Math.abs(ax100);
                vyTemp += Math.abs(ay100);
                vyTemp2 += g100;
                if (Math.abs(vxTemp) >= 100) {
                    if (ax100 > 0) {
                        vx += vxTemp / 100;
                    } else {
                        vx -= vxTemp / 100;
                    }
                    vxTemp %= 100;
                }
                if (Math.abs(vyTemp) >= 100) {
                    if (ay100 > 0) {
                        vy += vyTemp / 100;
                    } else {
                        vy -= vyTemp / 100;
                    }
                    vyTemp %= 100;
                }
                if (Math.abs(vyTemp2) >= 100) {
                    vy += vyTemp2 / 100;
                    vyTemp2 %= 100;
                }
            }
            forceS++;
            if (forceS > 30) {
                argS += i;
                forceS = (byte) force;
                argS = (short) Utils.toArg0_360(argS);
                if (argS == arg) {
                    break;
                }
            }
        } while (true);

        return null;
    }

    private byte getCollisionPoint(BulletManager bull, short X1, short Y1, short X2, short Y2, short toX, short toY, short Mx, short My, boolean isXuyenMap) {
        int Dx = X2 - X1;
        int Dy = Y2 - Y1;
        byte x_unit = 0;
        byte y_unit = 0;
        byte x_unit2 = 0;
        byte y_unit2 = 0;
        if (Dx < 0) {
            x_unit = x_unit2 = -1;
        } else if (Dx > 0) {
            x_unit = x_unit2 = 1;
        }
        if (Dy < 0) {
            y_unit = y_unit2 = -1;
        } else if (Dy > 0) {
            y_unit = y_unit2 = 1;
        }
        int k1 = Math.abs(Dx);
        int k2 = Math.abs(Dy);
        if (k1 > k2) {
            y_unit2 = 0;
        } else {
            k1 = Math.abs(Dy);
            k2 = Math.abs(Dx);
            x_unit2 = 0;
        }
        int k = k1 >> 1;
        short X = X1, Y = Y1;
        for (int i = 0; i <= k1; i++) {
            if (Math.abs(X - toX) <= Mx && Math.abs(Y - toY) <= My) {
                return 1;
            }
            if (!isXuyenMap) {
                if (bull.fm.mapMNG.isCollision(X, Y)) {
                    return 2;
                }
            }
            k += k2;
            if (k >= k1) {
                k -= k1;
                X += x_unit;
                Y += y_unit;
            } else {
                X += x_unit2;
                Y += y_unit2;
            }
        }
        return 0;
    }

    public void GhostBullet(int index, int toIndex) throws IOException {
        Message ms = new Message(124);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeByte(toIndex);
        ds.flush();
        this.sendToTeam(ms);
    }

    public void capture(byte index, byte toindex) throws IOException {
        Message ms = new Message(95);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeByte(toindex);
        ds.flush();
        this.sendToTeam(ms);
    }

    public void thadocBullet(byte index, byte toindex) throws IOException {
        Message ms = new Message(96);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeByte(toindex);
        ds.flush();
        this.sendToTeam(ms);
    }

    public void cLocation(byte toIndex) throws IOException {
        Player pl = players[toIndex];
        Message ms = new Message(21);
        DataOutputStream ds = ms.writer();
        ds.writeByte(toIndex);
        ds.writeShort(pl.X);
        ds.writeShort(pl.Y);
        ds.flush();
        pl.us.sendMessage(ms);
    }

    private void nextGift() throws IOException {
        for (int i = 0; i < ServerManager.maxPlayers; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.us == null) {
                continue;
            }
            for (int j = 0; j < pl.GiftBox.size(); j++) {
                Player.box nb = pl.GiftBox.get(0);
                Message ms = new Message(119);
                DataOutputStream ds = ms.writer();
                //null byte
                ds.writeByte(0);
                //player id
                ds.writeByte(pl.index);
                //type gift
                ds.writeByte(nb.type);
                switch (nb.type) {
                    //xu
                    case 0:
                        ds.writeShort(nb.getNumb());
                        pl.us.updateXu(nb.getNumb());
                        break;
                    //item
                    case 1:
                        ds.writeByte(nb.getId());
                        ds.writeByte(nb.getNumb());
                        pl.us.updateItems(nb.getId(), (byte) nb.getNumb());
                        break;
                    //xp
                    case 3:
                        ds.writeByte(nb.getNumb());
                        pl.us.updateXp(nb.getNumb(), false);
                        //notification

                }
                ds.flush();
                this.sendToTeam(ms);
                pl.GiftBox.remove(0);
            }
            for (int j = 0; j < pl.GiftBoxFalling.size(); j++) {
                Player.box nb = pl.GiftBoxFalling.get(0);
                Message ms = new Message(119);
                DataOutputStream ds = ms.writer();
                //null byte
                ds.writeByte(0);
                //player id
                ds.writeByte(pl.index);
                //type gift
                ds.writeByte(nb.type);
                switch (nb.type) {
                    //xu
                    case 0:
                        ds.writeShort(nb.getNumb());
                        pl.us.updateXu(nb.getNumb());
                        break;
                    //item
                    case 1:
                        ds.writeByte(nb.getId());
                        ds.writeByte(nb.getNumb());
                        pl.us.updateItems(nb.getId(), (byte) nb.getNumb());
                        break;
                    //xp
                    case 3:
                        ds.writeByte(nb.getNumb());
                        pl.us.updateXp(nb.getNumb(), false);
                        //notification

                }
                ds.flush();
                this.sendToTeam(ms);
                pl.GiftBoxFalling.remove(0);
            }
        }
    }
}
