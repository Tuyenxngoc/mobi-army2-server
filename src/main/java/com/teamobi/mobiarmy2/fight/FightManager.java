package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.fight.BulletManager.AddBoss;
import com.teamobi.mobiarmy2.fight.BulletManager.BomHenGio;
import com.teamobi.mobiarmy2.fight.BulletManager.Bullets;
import com.teamobi.mobiarmy2.fight.BulletManager.VoiRong;
import com.teamobi.mobiarmy2.fight.boss.*;
import com.teamobi.mobiarmy2.fight.bullet.ItemBomHenGio;
import com.teamobi.mobiarmy2.model.SpecialItemData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FightManager {

    final FightWait fightWait;
    private final User userPractice;
    private boolean isShoot;
    private int teamlevel;
    public boolean isNextTurn;
    protected boolean isTraining;
    protected byte type;
    protected boolean isBossTurn;
    protected byte bossTurn;
    protected byte playerTurn;
    protected int nTurn;
    protected byte nHopQua;
    protected int playerCount;
    public int totalPlayers;
    protected int WindX;
    protected int WindY;
    protected final byte timeCountMax = 30;
    public Player[] players;
    public MapManager mapManager;
    public BulletManager bulletManager;
    public CountDownManager countDownManager;

    public FightManager(FightWait fo) {
        this.isShoot = false;
        this.isNextTurn = true;
        this.fightWait = fo;
        this.userPractice = null;
        this.isTraining = false;
        this.type = fo.getRoom().getType();
        this.playerCount = 0;
        this.totalPlayers = 0;
        this.playerTurn = -1;
        this.isBossTurn = false;
        this.bossTurn = 0;
        this.WindX = 0;
        this.WindY = 0;
        this.nHopQua = 0;
        this.players = new Player[100];
        this.mapManager = new MapManager(this);
        this.bulletManager = new BulletManager(this);
        this.countDownManager = new CountDownManager(this, timeCountMax);
    }

    void sendToTeam(Message ms) {
        if (isTraining) {
            userPractice.sendMessage(ms);
            return;
        }
        for (Player player : players) {
            if (player == null || player.user == null) {
                continue;
            }
            player.user.sendMessage(ms);
        }
    }

    public byte getPlayerIndexById(int playerId) {
        for (byte i = 0; i < players.length; i++) {
            if (players[i] != null &&
                    players[i].user != null &&
                    players[i].user.getPlayerId() == playerId) {
                return i;
            }
        }
        return -1;
    }

    public int getIDTurn() {
        if (isTraining) {
            return 0;
        } else if (isBossTurn && type == 5) {
            return bossTurn;
        } else {
            return playerTurn;
        }
    }

    public Player getPlayerTurn() {
        if (isBossTurn) {
            return players[bossTurn];
        }
        return players[playerTurn];
    }

    public Player getPlayerClosest(short X, short Y) {
        int XClosest = -1;
        Player plClosest = null;
        for (byte i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.isDie) {
                continue;
            }
            int kcX = Math.abs(pl.x - X);
            if (XClosest == -1 || kcX < XClosest) {
                XClosest = kcX;
                plClosest = pl;
            }
        }
        return plClosest;
    }

    public int getLevelTeam() {
        return teamlevel;
    }

    private void nextBoss() throws IOException {
        if (this.fightWait.getMapId() == 30) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) ((i % 2 == 0) ? Utils.nextInt(95, 315) : Utils.nextInt(890, 1070));
                short Y = (short) (50 + 40 * Utils.nextInt(3));
                players[totalPlayers] = new BigBoom(this, (byte) 12, "Big Boom", (byte) totalPlayers, 1500 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 31) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) (Utils.nextInt(445, 800) + i * 50);
                short Y = 180;
                players[totalPlayers] = new BigBoom(this, (byte) 12, "Small Boom", (byte) totalPlayers, 1500 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 32) {
            byte numBoss = (new byte[]{2, 2, 3, 3, 4, 4, 5, 5, 5})[playerCount];
            short[] tempX = new short[]{505, 1010, 743, 425, 1068};
            short[] tempY = new short[]{221, 221, 198, 369, 369, 369};
            for (byte i = 0; i < numBoss; i++) {
                players[totalPlayers] = new SpiderMachine(this, (byte) 13, "Spider Robot", (byte) totalPlayers, 4785 + (getLevelTeam() * 15), (short) tempX[i], (short) tempY[i]);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 33) {
            byte numBoss = (new byte[]{2, 2, 3, 3, 4, 4, 5, 5, 6})[playerCount];
            short[] tempX = new short[]{420, 580, 720, 240, 55, 900};
            for (int i = 0; i < numBoss; i++) {
                short X = tempX[i];
                short Y = (short) 200;
                players[totalPlayers] = new Robot(this, (byte) 14, "Robot", (byte) totalPlayers, 3700 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 34) {
            short X = 880;
            short Y = 400;
            players[totalPlayers] = new Trex(this, (byte) 15, "T-rex", (byte) totalPlayers, 15000 + (this.getLevelTeam() * 10), X, Y);
            totalPlayers++;

            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 6, 7, 7, 8})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                X = (short) (Utils.nextInt(470, 755));
                Y = 400;
                players[totalPlayers] = new BigBoom(this, (byte) 12, "Big Boom", (byte) totalPlayers, 1500 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 35) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) (Utils.nextInt(300, 800));
                short Y = (short) Utils.nextInt(-350, 100);
                players[totalPlayers] = new UFO(this, (byte) 16, "UFO", (byte) totalPlayers, 4500 + (this.getLevelTeam() * 12), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 36) {
            short X = (short) (Utils.nextInt(300, 800));
            short Y = (short) Utils.nextInt(-350, 100);
            players[totalPlayers] = new Balloon(this, (byte) 17, "Balloon", (byte) totalPlayers, 1, X, Y);
            totalPlayers++;
            players[totalPlayers] = new BalloonGun(this, (byte) 18, "Balloon Gun", (byte) totalPlayers, 2000 + (this.getLevelTeam() * 10), (short) (X + 51), (short) (Y + 19));
            totalPlayers++;
            players[totalPlayers] = new BalloonGunBig(this, (byte) 19, "Balloon Gun Big", (byte) totalPlayers, 2500 + (this.getLevelTeam() * 10), (short) (X - 5), (short) (Y + 30));
            totalPlayers++;
            players[totalPlayers] = new BalloonFanBack(this, (byte) 20, "Fan Back", (byte) totalPlayers, 1000 + (this.getLevelTeam() * 10), (short) (X - 67), (short) (Y - 6));
            totalPlayers++;
        } else if (this.fightWait.getMapId() == 37) {
            byte numBoss = (new byte[]{2, 3, 3, 4, 4, 5, 5, 6, 6})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) Utils.nextInt(20, this.mapManager.width - 20);
                short Y = (short) 250;
                players[totalPlayers] = new SpiderPoisonous(this, (byte) 22, "Spider Poisonous", (byte) totalPlayers, 3800 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 38) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) ((short) 700 - i * 80);
                short Y = (short) (Utils.nextInt(30));
                players[totalPlayers] = new Ghost(this, (byte) 25, "Ghost", (byte) totalPlayers, 1800 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        } else if (this.fightWait.getMapId() == 39) {
            byte numBoss = (new byte[]{4, 4, 5, 5, 6, 8, 8, 9, 9})[playerCount];
            for (byte i = 0; i < numBoss; i++) {
                short X = (short) (700 - i * 80);
                short Y = (short) Utils.nextInt(30);
                players[totalPlayers] = new Ghost2(this, (byte) 26, "Ghost II", (byte) totalPlayers, 1800 + (this.getLevelTeam() * 10), X, Y);
                totalPlayers++;
            }
        }

        int bossLen = this.totalPlayers - 8;
        Message ms = new Message(89);
        DataOutputStream ds = ms.writer();
        ds.writeByte(bossLen);
        for (byte i = 0; i < bossLen; i++) {
            Boss boss = (Boss) this.players[8 + i];
            ds.writeInt(-1);
            ds.writeUTF(boss.name);
            ds.writeInt(boss.HPMax);
            ds.writeByte(boss.idNV);
            ds.writeShort(boss.x);
            ds.writeShort(boss.y);
        }
        ds.flush();
        sendToTeam(ms);
    }

    private void nextAngry() {
        for (byte i = 0; i < 8; i++) {
            Player player = players[i];
            if (player == null) {
                continue;
            }

            if (player.isUpdateAngry) {
                player.isUpdateAngry = false;

                try {
                    Message ms = new Message(Cmd.ANGRY);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(i);
                    ds.writeByte(player.angry);
                    ds.flush();
                    sendToTeam(ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void calcMM() {
        for (byte i = 0; i < 8; i++) {
            Player pl = players[i];
            if (pl == null) {
                continue;
            }
            pl.nextMM();
        }
    }

    private void nextMM() {
        for (byte i = 0; i < 8; i++) {
            Player pl = players[i];
            if (pl == null) {
                continue;
            }
            if (pl.isLucky) {
                pl.isLucky = false;

                if (i == playerTurn || pl.isUpdateHP) {
                    try {
                        Message ms = new Message(Cmd.LUCKY);
                        DataOutputStream ds = ms.writer();
                        ds.writeByte(i);
                        ds.flush();
                        sendToTeam(ms);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void nextBiDoc() {
        for (byte i = 0; i < 8; i++) {
            Player pl = players[i];
            if (pl == null) {
                continue;
            }
            if (pl.isBiDoc) {
                try {
                    Message ms = new Message(Cmd.POISON);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(pl.index);
                    ds.flush();
                    sendToTeam(ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void nextCantSee() throws IOException {
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < 8; i++) {
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
        for (byte i = 0; i < 8; i++) {
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
        for (byte i = 0; i < this.totalPlayers; i++) {
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
        for (byte i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.user == null) {
                continue;
            }
            if (pl.isUpdateXP) {
                int oldXP = pl.user.getCurrentXp();
                pl.user.updateXp(pl.XPUp, true);
                int newXP = pl.user.getCurrentXp();
                pl.AllXPUp += newXP - oldXP;
                pl.XPUp = 0;
                pl.isUpdateXP = false;
            }
        }
    }

    private void nextCUP() {
        for (byte i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.user == null) {
                continue;
            }
            if (pl.isUpdateCup) {
                pl.user.updateCup(pl.CupUp);
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

    private void huyCantMove(int index) throws IOException {
        Message ms = new Message(107);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(index);
        ds.flush();
        this.sendToTeam(ms);
    }

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
        for (byte i = 0; i < this.totalPlayers; i++) {
            Player pl = players[i];
            if (pl == null) {
                continue;
            }
            pl.chuanHoaXY();
        }
        if (this.isTraining) {
            this.playerTurn = 0;
        } else {
            //kiem tra xem ai bi thieu dot tru hp va lan bi dot
            for (byte i = 0; i < this.totalPlayers; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.isDie) {
                    continue;
                }
                for (byte j = 0; j < 8; j++) {
                    if (pl.NHTItemThieuDot[j][0] == 0) {
                        continue;
                    }
                    pl.NHTItemThieuDot[j][0]--;
                    pl.updateHP(-(1 + (pl.HPMax * 1 / 100)));
                    this.nextHP();
                }
            }

            if (countDownManager != null) {
                countDownManager.stopCount();
            }
            if (this.playerTurn == -1) {
                while (true) {
                    int next = 0;
                    if (this.type != 5) {
                        next = Utils.nextInt(8);
                    } else {
                        next = Utils.nextInt(this.totalPlayers);
                    }
                    if (this.players[next] != null && this.players[next].idNV != 18 && this.players[next].idNV != 19 && this.players[next].idNV != 20 && this.players[next].idNV != 21 && this.players[next].idNV != 23 && this.players[next].idNV != 24) {
                        if (next < 8) {
                            this.playerTurn = (byte) next;
                            this.isBossTurn = false;
                            this.bossTurn = 8;
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
                            if (turn == 8) {
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
                            if (turn >= this.totalPlayers) {
                                turn = 8;
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
                        if (turn == this.totalPlayers) {
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
        if (this.bulletManager.hasVoiRong) {
            for (byte i = 0; i < this.bulletManager.voiRongs.size(); i++) {
                VoiRong vr = this.bulletManager.voiRongs.get(i);
                vr.count--;
                if (vr.count < 0) {
                    this.bulletManager.voiRongs.remove(i);
                    i--;
                }
            }
            if (this.bulletManager.voiRongs.isEmpty()) {
                this.bulletManager.hasVoiRong = false;
            }
        }
        if (this.bulletManager.boms.size() > 0) {
            for (byte i = 0; i < this.bulletManager.boms.size(); i++) {
                BomHenGio bom = this.bulletManager.boms.get(i);
                bom.count--;
                if (bom.count == 1) {
                    this.bulletManager.exploreBom(i);
                    i--;
                }
            }
        }
        if (this.bulletManager.addboss.size() > 0) {
            for (byte i = 0; i < this.bulletManager.addboss.size(); i++) {
                AddBoss bos = this.bulletManager.addboss.get(i);
                this.addBoss(bos.players);
                players[totalPlayers - 1].XPExist = bos.XPE;
            }
            this.bulletManager.addboss.clear();
        }
        if (this.bulletManager.buls.size() > 0) {
            for (byte i = 0; i < this.bulletManager.buls.size(); i++) {
                Bullets bul = this.bulletManager.buls.get(i);
                this.bulletManager.addBom((ItemBomHenGio) bul.bull);
            }
            this.bulletManager.buls.clear();
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
        if (this.isTraining) {
            return;
        }
        this.countDownManager.resetCount();
        if (this.isBossTurn) {
            new Thread(() -> ((Boss) players[bossTurn]).turnAction()).start();
        }
    }

    public boolean checkWin() throws IOException {
        if (isTraining) {
            return true;
        }
        if (!fightWait.isStarted()) {
            return true;
        }
        // Next HP
        nextHP();
        nextCantSee();
        nextCantMove();
        nextBiDoc();
        if (FightManager.this.type == 5) {
            byte nPlayerAlive = 0, nBossAlive = 0, i = 0;
            while (i < 8) {
                Player pl2 = players[i];
                if (pl2 != null && !pl2.isDie) {
                    nPlayerAlive++;
                }
                i++;
            }
            while (i < totalPlayers) {
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
            for (byte i = 0; i < 8; i++) {
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
        this.WindX = 0;
        this.WindY = 0;
        this.nHopQua = 0;
        this.bulletManager.hasVoiRong = false;
        this.bulletManager.voiRongs.clear();
        this.bulletManager.boms.clear();
        this.bulletManager.addboss.clear();
        if (fightWait.getRoom().isContinuous() && fightWait.getNumPlayers() > 0) {

        }
        if (this.type == 5 && checkWin == 1) {
            for (byte i = 0; i < 8; i++) {
                Player pl = this.players[i];
                if (pl != null && pl.user != null) {
                    pl.user.updateXp(10, true);
                }
            }
        }
        Message ms;
        DataOutputStream ds;
        // Update Win
        for (byte i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.user == null) {
                continue;
            }
            byte win = (byte) (pl.team ? checkWin : -checkWin);
            if (win == 1 && nTurn > 2) {
                if (this.playerCount == 2) {
                    pl.user.updateMission(0, 1);
                } else if (this.playerCount >= 5) {
                    pl.user.updateMission(17, 1);
                }
                switch (pl.idNV) {
                    case 0:
                        pl.user.updateMission(13, 1);
                        break;
                    case 1:
                        pl.user.updateMission(14, 1);
                        break;
                    case 2:
                        pl.user.updateMission(15, 1);
                        break;
                    default:
                        break;
                }
                // UFO
                switch (this.mapManager.mapId) {
                    case 35:
                        pl.user.updateMission(2, 1);
                        break;
                    case 36:
                        pl.user.updateMission(3, 1);
                        break;
                    case 38:
                    case 39:
                        pl.user.updateMission(4, 1);
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
            ds.writeInt(this.fightWait.getMoney());
            ds.flush();
            pl.user.sendMessage(ms);
        }

        // Update All XP and CUP
        for (byte i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.user == null) {
                continue;
            }
            ms = new Message(97);
            ds = ms.writer();
            ds.writeInt(pl.AllXPUp);
            ds.writeInt(pl.user.getCurrentXp());
            ds.writeInt(pl.user.getCurrentLevel() * (pl.user.getCurrentLevel() + 1) * 1000);
            ds.writeByte(0);
            ds.writeByte(pl.user.getCurrentLevelPercent());
            ds.flush();
            pl.user.sendMessage(ms);
        }
        for (byte i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.user == null) {
                continue;
            }
            ms = new Message(-24);
            ds = ms.writer();
            ds.writeByte(pl.AllCupUp);
            ds.writeInt(pl.user.getCup());
            ds.flush();
            pl.user.sendMessage(ms);
        }
        // Update Xu
        if (this.fightWait.getMoney() > 0) {
            for (byte i = 0; i < 8; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.user == null) {
                    continue;
                }
                byte win = (byte) (pl.team ? checkWin : -checkWin);
                if (win >= 0) {
                    pl.user.updateXu(this.fightWait.money * (win == 1 ? 2 : 1));
                    ms = new Message(52);
                    ds = ms.writer();
                    ds.writeInt(pl.user.getPlayerId());
                    ds.writeInt(this.fightWait.money * (win == 1 ? 2 : 1));
                    ds.writeInt(pl.user.getXu());
                    ds.flush();
                    sendToTeam(ms);
                }
            }
        }
        this.fightWait.setStarted(false);
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
        }
        this.fightWait.fightComplete();
        if (fightWait.getRoom().isContinuous()) {

        }
        if (nTurn > 2 && fightWait.getRoom().getType() < 5) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException ignored) {
            }
            for (byte i = 0; i < 8; i++) {
                Player pl = this.players[i];
                if (pl == null || pl.user == null) {
                    continue;
                }
                byte win = (byte) (pl.team ? checkWin : -checkWin);
                if (win == 1) {
                    // pl.us.updateQua_Start(2, 30, true);
                }
            }
        }
        this.nTurn = 0;
    }

    protected void startGame(int nTeamPointBlue, int nTeamPointRed) throws IOException {
        if (fightWait.isStarted()) {
            return;
        }
        if (!this.isTraining) {
            mapManager.mapEffects.clear();
            mapManager.setMapId(fightWait.getMapId());
        } else {
            this.mapManager.setMapId(this.mapManager.mapId);
        }
        this.playerTurn = -1;
        this.nTurn = 0;
        this.WindX = 0;
        this.WindY = 0;
        if (this.isTraining) {
            this.playerCount = 1;
        } else {
            this.playerCount = this.fightWait.getNumPlayers();
        }
        this.totalPlayers = 8;
        if (this.isTraining) {

        } else {
            if (this.type == 5) {
                this.nHopQua = (byte) (this.playerCount / 2);
            }
            int[] location = new int[8];
            int count = 0;
            this.teamlevel = 0;
            for (byte i = 0; i < this.fightWait.getMaxSetPlayers(); i++) {
                User us = this.fightWait.getUsers()[i];
                if (us == null) {
                    this.players[i] = null;
                    continue;
                }

                this.teamlevel += us.getCurrentLevel();
                us.updateXu(-this.fightWait.money);
                short X, Y;
                byte item[];
                int teamPoint;
                boolean exists;
                int locaCount = -1;
                do {
                    locaCount = Utils.nextInt(this.mapManager.playerInitXPositions.length);
                    exists = false;
                    for (int j = 0; j < count; j++) {
                        if (location[j] == locaCount) {
                            exists = true;
                            break;
                        }
                    }
                } while (exists);
                location[count++] = locaCount;
                X = this.mapManager.playerInitXPositions[locaCount];
                Y = this.mapManager.playerInitYPositions[locaCount];
                item = this.fightWait.getItems()[i];
                for (byte j = 0; j < 4; j++) {
                    if (item[4 + j] > 0) {
                        if (12 + j > 1) {
                            us.updateItems((byte) (12 + j), -1);
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
        }
        this.bulletManager.mangNhenId = 200;
        this.sendFightInfoMessage();
        if (this.type == 5) {
            nextBoss();
        }
        this.nextTurn();
    }

    public void leave(int playerId) {
        byte index = getPlayerIndexById(playerId);
        if (index == -1) {
            return;
        }

        Player player = players[index];

        try {
            Message ms = new Message(Cmd.CHAT_TO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeUTF(GameString.leave2());
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!player.isDie) {
            player.HP = 0;
            player.isUpdateHP = true;
            player.isDie = true;
        }
        player.user = null;

        if (!this.isTraining) {
            new Thread(() -> {
                try {
                    if (!checkWin()) {
                        if (index == getIDTurn()) {
                            nextTurn();
                        } else {
                            Message ms1 = new Message(24);
                            DataOutputStream ds1 = ms1.writer();
                            ds1.writeByte(isBossTurn ? bossTurn : playerTurn);
                            ds1.flush();
                            sendToTeam(ms1);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    protected void sendFightInfoMessage() {
        try {
            if (!isTraining && fightWait.money > 0) {
                for (Player player : players) {
                    if (player == null || player.user == null) {
                        continue;
                    }
                    Message ms = new Message(52);
                    DataOutputStream ds = ms.writer();
                    ds.writeInt(player.user.getPlayerId());
                    ds.writeInt(-fightWait.money);
                    ds.writeInt(player.user.getXu());
                    ds.flush();
                    sendToTeam(ms);
                }
            }

            for (Player player : players) {
                if (player == null || player.user == null) {
                    continue;
                }
                Message ms = new Message(20);
                DataOutputStream ds = ms.writer();
                if (isTraining) {
                    for (short id : userPractice.getEquip()) {
                        ds.writeShort(id);
                    }
                }
                ds.writeByte(0);//Todo find this value
                if (isTraining) {
                    ds.writeByte(0);
                } else {
                    ds.writeByte(timeCountMax);
                }
                ds.writeShort(player.dongDoi);
                if (!isTraining && fightWait.getRoom().getType() == 7) {
                    ds.writeByte(8);
                }
                for (Player pl : players) {
                    if (pl == null) {
                        ds.writeShort(-1);
                        continue;
                    }
                    ds.writeShort(pl.x);
                    ds.writeShort(pl.y);
                    ds.writeShort(pl.HPMax);
                }
                ds.flush();
                player.user.sendMessage(ms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void countOut() throws IOException {
        if (fightWait.isStarted() && !checkWin()) {
            nextTurn();
        }
    }

    public void changeLocation(int index) throws IOException {
        Player pl = this.players[index];
        ServerManager.getInstance().logger().logMessage("Player " + index + " change location X=" + pl.x + " Y=" + pl.y);
        Message ms = new Message(21);
        DataOutputStream ds = ms.writer();
        ds.writeByte(index);
        ds.writeShort(pl.x);
        ds.writeShort(pl.y);
        ds.flush();
        sendToTeam(ms);
        if (pl.y > this.mapManager.height) {
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
        ds.writeShort(pl.x);
        ds.writeShort(pl.y);
        ds.flush();
        sendToTeam(ms);
    }

    public void addBoss(Player pl) throws IOException {
        if (totalPlayers >= ServerManager.getInstance().config().getMaxElementFight()) {
            return;
        }
        players[totalPlayers] = pl;
        Boss boss = (Boss) this.players[totalPlayers];
        Message ms = new Message(89);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeInt(-1);
        ds.writeUTF(boss.name);
        ds.writeInt(boss.HPMax);
        ds.writeByte(boss.idNV);
        ds.writeShort(boss.x);
        ds.writeShort(boss.y);
        ds.flush();
        this.sendToTeam(ms);
        totalPlayers++;
    }

    public void newShoot(int index, byte bullId, short arg, byte force, byte force2, byte nshoot, boolean ltap) throws IOException {
        //ServerManager.getInstance().logger().logMessage("New shoot index=" + index + " bullId: " + bullId + " arg: " + arg + " force: " + force + " force2: " + force2 + " nshoot: " + nshoot);
        final Player pl = this.players[index];
        short x = pl.x, y = pl.y;
        if (!ltap) {
            this.calcMM();
        }
        bulletManager.addShoot(pl, bullId, arg, force, force2, nshoot);
        bulletManager.fillXY();
        if (!this.isTraining) {
            this.nextMM();
        }
        ArrayList<Bullet> bullets = bulletManager.entrys;
        if (bullets.isEmpty()) {
            return;
        }
        bullId = bulletManager.entrys.get(0).bullId;
        Message ms = new Message(ltap ? 84 : 22);
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
            ds.writeByte(bulletManager.force2);
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
            if (bulletManager.typeSC > 0 && pl.user != null) {
                pl.user.updateMission(12, 1);
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
                            ds.writeByte(bulletManager.mgtAddX);
                            ds.writeByte(bulletManager.mgtAddY);
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
            bulletManager.typeSC = 0;
        }
        ds.writeByte(bulletManager.typeSC);
        if (bulletManager.typeSC == 1 || bulletManager.typeSC == 2) {
            // X, Y super
            ds.writeShort(bulletManager.XSC);
            ds.writeShort(bulletManager.YSC);
        }
        ds.flush();
        bulletManager.reset();
        this.sendToTeam(ms);
        pl.isUseItem = false;
        pl.itemUsed = -1;
        if (this.isTraining) {
            nextTurn();
        } else if (this.isNextTurn) {
            new Thread(() -> {
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
            }).start();
        }
    }

    public void chatMessage(int playerId, String s) {
        int index = this.getPlayerIndexById(playerId);
        if (index == -1) {
            return;
        }
        try {
            Message ms = new Message(9);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeUTF(s);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeLocationMessage(User us, short x, short y) throws IOException {
        int index = this.getPlayerIndexById(us.getPlayerId());
        if (index == -1) {
            return;
        }
        Player pl = this.players[index];
        pl.updateXY(x, y);
        changeLocation(index);
    }

    public void shootMessage(User us, byte bullId, short x, short y, short arg, byte force, byte force2, byte nshoot) throws IOException {
        int index = this.getPlayerIndexById(us.getPlayerId());
        if (index == -1 || index != this.playerTurn || this.isShoot || !fightWait.isStarted()) {
            return;
        }
        this.isShoot = true;
        Player pl = this.players[index];
        if (pl.banX2) {
            nshoot = 2;
            pl.banX2 = false;
        } else {
            nshoot = 1;
        }
        if (this.isTraining) {
            pl.setXY(x, y);
        } else if (x != pl.x && y != pl.y) {
            pl.updateXY(x, y);
        }
        newShoot(index, bullId, (arg > 360 ? 360 : (arg < -360 ? -360 : arg)), (force > 30 ? 30 : (force < 0 ? 0 : force)), (force2 > 30 ? 30 : (force2 < 0 ? 0 : force2)), nshoot, isTraining);
    }

    public void boLuotMessage(User us) throws IOException {
        int index = this.getPlayerIndexById(us.getPlayerId());
        if (index == -1 || index != this.playerTurn || checkWin()) {
            return;
        }
        // Set next turn
        nextTurn();
    }

    public void useItemMessage(User us, Message ms) throws IOException {

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
        this.mapManager.handleCollision((short) X, (short) Y, bull);
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
                if ((x < -200) || (x > bull.fm.mapManager.width + 200) || (y > bull.fm.mapManager.height + 200)) {
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
                if (bull.fm.mapManager.isCollision(X, Y)) {
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
        ds.writeShort(pl.x);
        ds.writeShort(pl.y);
        ds.flush();
        pl.user.sendMessage(ms);
    }

    private void nextGift() throws IOException {
        for (int i = 0; i < 8; i++) {
            Player pl = this.players[i];
            if (pl == null || pl.user == null) {
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
                        pl.user.updateXu(nb.getNumb());
                        break;
                    //item
                    case 1:
                        ds.writeByte(nb.getId());
                        ds.writeByte(nb.getNumb());
                        //    pl.us.updateItem(nb.getId(), nb.getNumb());
                        break;
                    //xp
                    case 3:
                        ds.writeByte(nb.getNumb());
                        pl.user.updateXp(nb.getNumb(), false);
                        //notification
                    case 4:
                        ds.writeUTF(SpecialItemData.getSpecialItemById(nb.getId()).getName());
                        //  pl.us.updateSpecialItem(nb.getId(), nb.getNumb());
                        break;
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
                        pl.user.updateXu(nb.getNumb());
                        break;
                    //item
                    case 1:
                        ds.writeByte(nb.getId());
                        ds.writeByte(nb.getNumb());
                        //   pl.us.updateItem(nb.getId(), nb.getNumb());
                        break;
                    //xp
                    case 3:
                        ds.writeByte(nb.getNumb());
                        pl.user.updateXp(nb.getNumb(), false);
                        //notification
                    case 4:
                        ds.writeUTF(SpecialItemData.getSpecialItemById(nb.getId()).getName());
                        //  pl.us.updateSpecialItem(nb.getId(), nb.getNumb());
                        break;
                }
                ds.flush();
                this.sendToTeam(ms);
                pl.GiftBoxFalling.remove(0);
            }
        }
    }
}
