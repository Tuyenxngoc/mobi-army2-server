package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.fight.BulletManager.AddBoss;
import com.teamobi.mobiarmy2.fight.boss.Ghost2;
import com.teamobi.mobiarmy2.model.FightItemData;
import com.teamobi.mobiarmy2.model.ItemClanData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Player {

    protected static class box {

        public byte type;
        private int numb;
        private byte id;

        public void Coins(int numb) {
            this.numb = numb;
            this.type = 0;
        }

        public void Item(byte id, byte numb) {
            this.id = id;
            this.numb = numb;
            this.type = 1;
        }

        public void XP(int numb) {
            this.numb = numb;
            this.type = 3;
        }

        public void SpecialItem(byte id, int numb) {
            this.id = id;
            this.numb = numb;
            this.type = 4;
        }

        public byte getId() {
            switch (this.type) {
                case 1:
                case 3:
                case 4:
                    return id;
                default:
                    return -1;
            }
        }

        public int getNumb() {
            return this.numb;
        }
    }

    protected FightManager fightManager;
    protected User user;
    protected boolean team;
    protected short bulletId;
    public short x;
    public short y;
    public short width;
    public short height;
    protected byte itemInit[];
    protected byte item[];
    protected byte itemUsed;
    protected boolean isUseItem;
    protected boolean isUsePow;
    public boolean isDie;
    public boolean isUpdateHP;
    protected boolean isUpdateAngry;
    protected boolean isUpdateXP;
    protected boolean isUpdateCup;
    protected int XPUp;
    protected int CupUp;
    protected int AllXPUp;
    protected int AllCupUp;
    protected ArrayList<box> GiftBox;
    protected ArrayList<box> GiftBoxFalling;

    protected byte idNV;
    public byte index;
    protected short gunId;
    protected int angry;
    protected byte pixel;
    public int HP;
    protected int HPMax;
    protected long satThuong;
    protected int phongThu;
    protected int mayMan;
    protected int dongDoi;
    protected byte ngungGioCount;
    protected byte hutMauCount;
    protected byte tangHinhCount;
    public byte voHinhCount;
    protected byte cantSeeCount;
    protected byte cantMoveCount;
    public boolean isBiDoc;
    protected boolean diX2;
    protected boolean banX2;
    protected byte buocDi;
    protected byte theLuc;
    protected boolean isMM;
    protected boolean fly;
    protected int XPExist;
    protected int[][] NHTItemThieuDot = new int[8][2];
    private boolean[] itemclan = new boolean[ItemClanData.CLAN_ITEM_ENTRY_MAP.size() + 1];

    public Player(FightManager fightManager, byte location, short X, short Y, byte item[], int teamPoint, User user) {
        this.fightManager = fightManager;
        this.index = location;
        this.team = fightManager.type == 5 || location % 2 == 0;
        this.bulletId = -1;
        this.gunId = -1;
        this.x = X;
        this.y = Y;
        this.theLuc = 60;
        this.width = 24;
        this.height = 24;
        this.itemInit = item;
        this.fly = false;
        this.XPExist = 0;
        this.GiftBox = new ArrayList<>();
        this.GiftBoxFalling = new ArrayList<>();
        if (item != null) {
            this.item = new byte[item.length];
            System.arraycopy(item, 0, this.item, 0, item.length);
        }
        this.user = user;
        this.itemUsed = -1;
        this.isUseItem = false;
        this.isUsePow = false;
        this.isDie = false;
        this.angry = 0;
        this.pixel = 25;
        this.dongDoi = teamPoint;
        this.ngungGioCount = 0;
        this.hutMauCount = 0;
        this.voHinhCount = 0;
        this.cantSeeCount = 0;
        this.cantMoveCount = 0;
        this.isBiDoc = false;
        this.diX2 = false;
        this.banX2 = false;
        this.buocDi = 0;
        this.isMM = false;
        this.isUpdateAngry = false;
        this.isUpdateHP = false;
        this.isUpdateXP = false;
        this.XPUp = 0;
        this.CupUp = 0;
        this.idNV = 0;
        this.HPMax = 0;
        this.satThuong = 0;
        this.phongThu = 0;
        this.mayMan = 0;
        this.HP = 0;
        if (user == null) {
            return;
        }
        if (this.user.getClanId() > 0) {
            //Todo get Clan item
        }
        this.bulletId = user.getIDBullet();
        this.gunId = user.getGunId();
        this.idNV = user.getNvUsed();
        int[] ability = user.getAbility();
        this.HPMax = ability[0] + (teamPoint * 5);
        this.satThuong = ability[1] + (teamPoint / 1);
        this.phongThu = ability[2] + (teamPoint * 5);
        this.mayMan = ability[3] + (teamPoint * 5);
        //5% mm
        if (this.itemclan[2]) {
            this.mayMan = this.mayMan * 105 / 100;
        }
        //5% dong doi
        if (this.itemclan[3]) {
            this.dongDoi = this.dongDoi * 105 / 100;
        }
        //5% phong thu
        if (this.itemclan[4]) {
            this.phongThu = this.phongThu * 105 / 100;
        }
        //5% HP
        if (this.itemclan[6]) {
            this.HPMax = this.HPMax * 105 / 100;
        }
        //10% mm
        if (this.itemclan[9]) {
            this.mayMan = this.mayMan * 110 / 100;
        }
        //10% dong doi
        if (this.itemclan[10]) {
            this.dongDoi = this.dongDoi * 110 / 100;
        }

        this.HP = this.HPMax;
    }

    public static int[] getLuyenTapItem() {
        return new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    public final void setXY(short X, short Y) {
        if (X >= 0 && X < this.fightManager.mapManager.width && Y < this.fightManager.mapManager.height) {
            this.x = X;
            this.y = Y;
        }
    }

    public final void updateXY(short X, short Y) {
        while (X != this.x || Y != this.y) {
            int preX = this.x;
            int preY = this.y;
            if (X < this.x) {
                move(false);
            } else if (X > this.x) {
                move(true);
            }
            // if ko di chuyen dc
            if (preX == this.x && preY <= this.y) {
                return;
            }
        }
    }

    public void chuanHoaXY() {
        while (this.y < this.fightManager.mapManager.height + 200) {
            if (this.fightManager.mapManager.isCollision(x, y) || this.fly) {
                return;
            }
            y++;
        }
    }

    public boolean isDropMap() {
        short X = this.x, Y = this.y;
        boolean isFly = this.fly;
        short mapHeight = this.fightManager.mapManager.height;
        while (Y < mapHeight + 200) {
            if (this.fightManager.mapManager.isCollision(X, Y) || isFly) {
                return false;
            }
            Y++;
        }
        return true;
    }

    protected void move(boolean addX) {
        if (this.cantMoveCount > 0) {
            return;
        }
        byte step = 1;
        if (this.diX2) {
            step = 2;
        }
        if (buocDi > theLuc) {
            return;
        }
        buocDi++;
        if (addX) {
            x += step;
        } else {
            x -= step;
        }
        if (fightManager.mapManager.isCollision(x, (short) (y - 5))) {
            buocDi--;
            if (addX) {
                x -= step;
            } else {
                x += step;
            }
            return;
        }
        for (int i = 4; i >= 0; i--) {
            if (this.fightManager.mapManager.isCollision(x, (short) (y - i))) {
                y -= i;
                return;
            }
        }
        this.chuanHoaXY();
    }

    public final void updateHP(int addHP) {
        this.isUpdateHP = true;
        this.HP += addHP;
        if (this.HP <= 0) {
            this.HP = 0;
        } else if (this.HP < 10) {
            this.HP = 10;
        } else if (this.HP > this.HPMax) {
            this.HP = this.HPMax;
        }
        int oldPixel = this.pixel;
        this.pixel = (byte) (this.HP * 25 / this.HPMax);
        if (addHP < 0) {
            this.updateAngry((oldPixel - pixel) * 4);
        }
        if (this.HP <= 0) {
            die();
        }
    }

    public final void updateAngry(int addAngry) {
        this.isUpdateAngry = true;
        this.angry += addAngry;
        if (this.angry < 0) {
            this.angry = 0;
        }
        if (this.angry > 100) {
            this.angry = 100;
        }
    }

    public final void updateEXP(int addXP) throws IOException {
        if (user == null || addXP == 0) {
            return;
        }
        this.isUpdateXP = true;
        if (this.user.getClanId() > 0) {
            //Todo check exp clan
        }
        if (this.itemclan[1]) {
            addXP *= 2;
        }
        if (this.itemclan[8]) {
            addXP *= 3;
        }
        this.XPUp += addXP;
        addXP -= 2;
        if (addXP < 1) {
            return;
        }
        int i = this.team ? 0 : 1;
        int lent = this.fightManager.type == 5 ? 1 : 2;
        for (; i < 8; i += lent) {
            Player pl = this.fightManager.players[i];
            if (pl == null || pl == this) {
                continue;
            }
            pl.isUpdateXP = true;
            pl.XPUp += addXP;
            pl.AllXPUp += addXP;
        }
    }

    public final void updateCUP(int addCup) throws IOException {
        if (user == null || addCup == 0) {
            return;
        }
        this.isUpdateCup = true;
        this.CupUp += addCup;
        this.AllCupUp += addCup;
        addCup -= 2;
        if (addCup < 1) {
            return;
        }
        int i = this.team ? 0 : 1;
        int lent = this.fightManager.type == 5 ? 1 : 2;
        for (; i < 8; i += lent) {
            Player pl = this.fightManager.players[i];
            if (pl == null || pl == this) {
                continue;
            }
            pl.isUpdateCup = true;
            pl.CupUp += addCup;
            pl.AllCupUp += addCup;
        }
    }

    private void die() {
        if (this.isMM && this.x > 0 && this.y < this.fightManager.mapManager.height && this.x < this.fightManager.mapManager.width) {
            this.HP = 10;
        } else {
            this.isDie = true;
            if (user != null) {
                user.notifyNetWait();
            }
        }
    }

    public void netWait() {
        this.fightManager.countDownManager.second += 2;
        if (user != null) {
            user.netWait();
        }
    }

    public void notifyNetWait() {
        if (user != null) {
            user.notifyNetWait();
        }
    }

    public boolean isCollision(short X, short Y) {
        if (this.voHinhCount > 0 || this.isDie) {
            return false;
        }
        return Utils.inRegion(X, Y, this.x - this.width / 2, this.y - this.height, this.width, this.height);
    }

    public void collision(short bx, short by, Bullet bull) {
        if (this.fightManager.ltap) {
            return;
        }

        int tamAH = Bullet.getTamAHByBullID(bull.bullId);
        if (bull.bullId == 35 && bull.pl.idNV == 15) {
            tamAH = 250;
        }
        // Neu la tz or apa or chicky or rocket dung pow-> no rong gap doi
        if (bull.pl.isUsePow && (bull.pl.idNV == 3 || bull.pl.idNV == 4 || bull.pl.idNV == 6 || bull.pl.idNV == 7 || bull.pl.idNV == 8)) {
            tamAH = tamAH * 2;
        }
        if (this.isDie || this.voHinhCount > 0 || !Utils.intersecRegions(x, y, width, height, bx, by, tamAH * 2, tamAH * 2)) {
            return;
        }
        if ((bull.bullId == 31 || bull.bullId == 32 || bull.bullId == 35) && this.index >= 8) {
            return;
        }
        int kcX = Math.abs(x - bx);
        int kcY = Math.abs(y - this.height / 2 - by);
        int kc = (int) Math.sqrt(kcX * kcX + kcY * kcY);
        long dame = bull.satThuong;
        if (kc > this.width / 2) {
            dame = dame - ((dame * (kc - this.width / 2)) / tamAH);
        }
        int PhongThu = this.phongThu;
        boolean isDropMap = isDropMap();
        if (isDropMap) {
            this.HP = 0;
            this.isDie = true;
            this.isUpdateHP = true;
            this.pixel = 0;
        }
        if (dame > 0) {
            if (bull.pl.isMM) {
                dame *= 2;
            }
            if (bull.pl.itemclan[7]) {
                dame = dame * 105 / 100;
            }
            if (bull.pl.isUsePow && bull.pl.itemclan[5]) {
                dame = dame * 105 / 100;
            }
            if (bull.typeSC > 0) {
                switch (bull.typeSC) {
                    case 1:
                        fightManager.bulletManager.typeSC = 1;
                        dame = dame * 11 / 10; // x1.1
                        fightManager.bulletManager.XSC = bull.XmaxY;
                        fightManager.bulletManager.YSC = bull.maxY;
                        break;
                    case 2:
                        fightManager.bulletManager.typeSC = 1;
                        dame = dame * 6 / 5; // x1.2
                        fightManager.bulletManager.XSC = bull.XmaxY;
                        fightManager.bulletManager.YSC = bull.maxY;
                        break;
                    case 4:
                        fightManager.bulletManager.typeSC = 2;
                        dame = dame * 13 / 10; // x1.3
                        break;
                    default:
                        break;
                }
            }
            if (this.isMM) {
                PhongThu = this.phongThu + this.phongThu / 10;
            }
            if (this.isMM) {
                dame = Math.round((float) (dame * 1) / 2);
            }
        }
        PhongThu *= isMM ? 2 : 1;
        int maxPhongThu = 100000;
//        PhongThu = PhongThu > 95000 ? 95000 : PhongThu;
//        dame = Math.round(dame - (dame * (PhongThu * 100 / maxPhongThu) / 100));
        PhongThu = PhongThu > 95000 ? 95000 : PhongThu;
        dame = Math.round(dame - (dame * (PhongThu * 10 / maxPhongThu) / 100));
        if (dame > 0) {
            int oldHP = this.HP;
            if (!isDropMap) {
                this.updateHP((int) -dame);
            }
            if (bull.pl instanceof Boss) {
                return;
            }

            bull.pl.user.updateMission(1, oldHP - this.HP);
            if (bull.pl.hutMauCount > 0 && this.HP > dame) {
                bull.pl.updateHP((int) (dame / 2));
            }
            if (bull.pl.hutMauCount > 0 && this.HP < dame) {
                bull.pl.updateHP(this.HP / 2);
            }
            // Neu ban chet + xp va dvong
            if (this.isDie) {
                // Tarzan
                if (this.idNV == 7) {
                    bull.pl.user.updateMission(6, 1);
                }
                if (this.idNV == 6) {
                    bull.pl.user.updateMission(7, 1);
                }
                if (this.idNV == 9) {
                    bull.pl.user.updateMission(8, 1);
                }
                try {
                    if (this.idNV == 23) {
                        switch (Utils.nextInt(4)) {
                            //xu
                            case 0:
                                int[] xuup = new int[]{1000, 5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 60000};
                                bull.pl.GiftBoxFalling.add(new box());
                                bull.pl.GiftBoxFalling.get(bull.pl.GiftBoxFalling.size() - 1).Coins(xuup[Utils.nextInt(xuup.length)]);
                                break;
                            //item
                            case 1:
                                bull.pl.GiftBoxFalling.add(new box());
                                bull.pl.GiftBoxFalling.get(bull.pl.GiftBoxFalling.size() - 1).Item((byte) (Utils.nextInt(FightItemData.FIGHT_ITEM_ENTRIES.size() - 2) + 2), (byte) Utils.nextInt(1, 5));
                                break;
                            //xp
                            case 2:
                                bull.pl.GiftBoxFalling.add(new box());
                                bull.pl.GiftBoxFalling.get(bull.pl.GiftBoxFalling.size() - 1).XP(Utils.nextInt(20000, 30000));
                                break;
                            //item dac biet
                            case 3:
                                bull.pl.GiftBoxFalling.add(new box());
                                bull.pl.GiftBoxFalling.get(bull.pl.GiftBoxFalling.size() - 1).SpecialItem((byte) (Utils.nextInt(8) + Utils.nextInt(5) * 10), 1);
                                break;
                        }
                    } else if (this.idNV == 24) {
                        switch (Utils.nextInt(4)) {
                            //xu
                            case 0:
                                int[] xuup = new int[]{1000, 5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 60000};
                                bull.pl.GiftBox.add(new box());
                                bull.pl.GiftBox.get(bull.pl.GiftBox.size() - 1).Coins(xuup[Utils.nextInt(xuup.length)]);
                                break;
                            //item
                            case 1:
                                bull.pl.GiftBox.add(new box());
                                bull.pl.GiftBox.get(bull.pl.GiftBox.size() - 1).Item((byte) (Utils.nextInt(FightItemData.FIGHT_ITEM_ENTRIES.size() - 2) + 2), (byte) Utils.nextInt(1, 5));
                                break;
                            //xp
                            case 2:
                                bull.pl.GiftBox.add(new box());
                                bull.pl.GiftBox.get(bull.pl.GiftBox.size() - 1).XP(Utils.nextInt(1, 127));
                                break;
                            //item dac biet
                            case 3:
                                bull.pl.GiftBox.add(new box());
                                bull.pl.GiftBox.get(bull.pl.GiftBox.size() - 1).SpecialItem((byte) (Utils.nextInt(8) + Utils.nextInt(5) * 10), 1);
                                break;
                        }
                    } else if (this.idNV == 26) {
                        for (int i = 0; i < 2; i++) {
                            Player players = new Ghost2(fightManager, (byte) 26, "Ghost II", (byte) (fightManager.allCount + fightManager.bulletManager.addboss.size()), 1800 + (fightManager.getLevelTeam() * 10), (short) (Utils.nextInt(100, fightManager.mapManager.width - 100)), (short) Utils.nextInt(150));
                            fightManager.bulletManager.addboss.add(new AddBoss(players, fightManager.getisLH() ? 50 : 6));
                        }
                    }
                    // Ban dong doi -5xp -5cup
                    if (this.fightManager.type != 5 && this.team == bull.pl.team && this.idNV != 23 && this.idNV != 24 && this.index != bull.pl.index) {
                        bull.pl.updateCUP(-5);
                        //bull.pl.updateEXP(-5);
                        return;
                    }
                    if (index == bull.pl.index || this.fightManager.type == 5 && !(this instanceof Boss)) {
                        return;
                    }
                    if (this instanceof Boss) {
                        if (fightManager.mapManager.mapId != 35 && this.idNV != 23 && this.idNV != 24) {
                            switch (Utils.nextInt(3)) {
                                case 0:
                                    int kichno = Utils.nextInt(10, 20);
                                    bull.pl.updateAngry(kichno);
                                    bull.pl.flyNotice(kichno + " Siêu kích Power");
                                    break;
                                case 1:
                                    int xuroi = Utils.nextInt(1, 100);
                                    bull.pl.user.updateXu(xuroi);
                                    bull.pl.flyNotice(xuroi + " Xu chiến đấu");
                                    break;
                                case 2:
                                    int cuplen = Utils.nextInt(1, 20);
                                    bull.pl.updateCUP(cuplen);
                                    bull.pl.flyNotice(cuplen + " Danh dự chiến đấu");
                                    break;
                            }
                        }

                        int thaoancut = this.XPExist * 4;
                        bull.pl.updateEXP(thaoancut);
                    } else {
                        int cupCL = bull.pl.user.getDanhVong() - this.user.getDanhVong();
                        int cupAdd = ((3000 - cupCL) / 100);
                        int levelPL = this.user.getCurrentLevel();
                        if (levelPL > 255) {
                            levelPL = 255;
                        }
                        if (cupAdd > 60) {
                            cupAdd = 60;
                        }
                        if (cupAdd < 0) {
                            cupAdd = 0;
                        }
                        bull.pl.updateCUP(cupAdd);
                        bull.pl.updateEXP((levelPL / 2) + 2);
                        updateCUP(-cupAdd);
                        //updateEXP(-((levelPL / 2) + 2));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void nextMM() {
        if (this.mayMan > 7500) {
            this.mayMan = 7500;
        }
        this.isMM = Utils.nextInt(10000) <= this.mayMan;
    }

    public void flyNotice(String text) {
        try {
            Message ms = new Message(119);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(0);
            ds.writeByte(4);
            ds.writeUTF(text);
            ds.flush();
            fightManager.sendToTeam(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
