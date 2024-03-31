package com.teamobi.mobiarmy2.army2.server;

import com.teamobi.mobiarmy2.army2.fight.FightManager;
import com.teamobi.mobiarmy2.army2.fight.FightWait;
import com.teamobi.mobiarmy2.network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.teamobi.mobiarmy2.army2.server.FomularData.FomularDataEntry;
import static com.teamobi.mobiarmy2.army2.server.FomularData.FomularEntry;
import static com.teamobi.mobiarmy2.army2.server.ItemData.ItemEntry;
import static com.teamobi.mobiarmy2.army2.server.MissionData.MissDataEntry;
import static com.teamobi.mobiarmy2.army2.server.MissionData.MissionEntry;
import static com.teamobi.mobiarmy2.army2.server.NVData.EquipmentEntry;
import static com.teamobi.mobiarmy2.army2.server.NVData.NVEntry;
import static com.teamobi.mobiarmy2.army2.server.SpecialItemData.SpecialItemEntry;

public class User {

    public static class ruongDoItemEntry {

        int numb;
        SpecialItemEntry entry;
    }

    public static class ruongDoTBEntry {

        int index;
        Date dayBuy;
        byte slotNull;
        byte vipLevel;
        short[] invAdd;
        short[] percenAdd;
        short[] anAdd;
        int[] slot;
        boolean isUse;
        EquipmentEntry entry;
        byte cap;
    }

    public static class equip2 {

        private byte nv;
        private byte type;
        private Date time;
    }

    public enum State {
        Waiting, WaitFight, Fighting
    }

    public static class QuaInfo {

        // 0: xu 1: xp 2: specialItem 3: item
        int type;
        int idItem;
        int numb;
    }

    public final Session client;
    private State state;
    private int iddb;
    private String username;
    private byte nv;
    private int xu;
    private int luong;
    private int dvong;
    private int eventScore;
    private int event_halloween;
    private Date baodanhsk;
    private short clan;
    private Date xpX2Time;
    private Date xpX6Time;
    private Date xpX0Time;
    private short csinh;
    private byte[] nvCSinh;
    private boolean[] nvStt;
    private int[] lever;
    private byte[] leverPercen;
    private int[] xp;
    private short[] point;
    private short[][] pointAdd;
    private byte[] item;
    private int[][] NvData;
    private ruongDoTBEntry[][] nvEquip;
    private int[] mission;
    private byte[] missionLevel;
    private final ArrayList<ruongDoItemEntry> ruongDoItem;
    private final ArrayList<ruongDoTBEntry> ruongDoTB;
    private final ArrayList<equip2> Equip2;
    private JSONArray friends = new JSONArray();

    private byte hopNgocAction;
    private Boolean baoHiem;
    private int fabricateId;
    private int hopNgocNum;
    private int hopNgocGia;
    private ruongDoTBEntry hopNgocTB;
    private ruongDoItemEntry hopNgocItem;
    private final ArrayList<ruongDoItemEntry> hopNgocItemArray;
    private final ArrayList<QuaInfo> quas;
    private byte moQua;
    private boolean[] dataQua = new boolean[12];
    private int timeQua;
    public boolean startQua;
    private Thread Gift_finish;
    protected FightWait waitFight;
    protected FightManager luyentap;
    protected FightManager fight;
    protected static EquipmentEntry[][] nvEquipDefault;
    private final short[][] GIFT_DATA_BACH_KIM = {{54, 52, 53, 55, 56}, {51, 49, 50, 53, 52}, {51, 49, 50, 53, 52}, {52, 50, 51, 54, 53}, {51, 49, 50, 53, 52}, {50, 48, 49, 52, 51}, {51, 49, 50, 53, 52}, {52, 54, 53, 50, 51}, {51, 53, 52, 49, 50}, {51, 53, 52, 49, 50}};
    private final short[][] GIFT_DATA_HOANG_KIM = {{89, 87, 88, 90, 91}, {86, 84, 85, 87, 88}, {86, 84, 85, 87, 88}, {86, 84, 85, 87, 88}, {86, 84, 85, 87, 88}, {85, 83, 84, 86, 87}, {86, 84, 85, 87, 88}, {87, 85, 86, 88, 89}, {86, 84, 85, 87, 88}, {86, 84, 85, 87, 88}};

    public User(Session client) {
        this.client = client;
        this.state = State.Waiting;
        this.ruongDoItem = new ArrayList<>();
        this.ruongDoTB = new ArrayList<>();
        this.Equip2 = new ArrayList<>();
        this.hopNgocItemArray = new ArrayList<>();
        this.quas = new ArrayList<>();
        this.moQua = 0;
    }

    public State getState() {
        return this.state;
    }

    protected void setState(State st) {
        this.state = st;
    }

    public int getIDDB() {
        return this.iddb;
    }

    public String getUserName() {
        return this.username;
    }

    public byte getNVUse() {
        return this.nv;
    }

    public int getXu() {
        return this.xu;
    }

    public Date getBaodanhsk() {
        return this.baodanhsk;
    }

    public synchronized void updateEventScore(int score) {
        this.eventScore += score;
    }

    public synchronized void updateEventHalloween(int score) {
        this.event_halloween += score;
    }

    public synchronized void updateBaoDanhSk(Date nd) {
        this.baodanhsk = nd;
    }

    public synchronized void updateXu(int xuUp) {
        try {
            if (xuUp == 0) {
                return;
            }
            long sum = (long) xuUp + (long) this.xu;
            if (sum > Integer.MAX_VALUE) {
                this.xu = Integer.MAX_VALUE;
            } else if (sum < Integer.MIN_VALUE) {
                this.xu = Integer.MIN_VALUE;
            } else {
                this.xu += xuUp;
            }
            // Send update Xu
            Message ms = new Message(105);
            DataOutputStream ds = ms.writer();
            ds.writeInt(this.xu);
            ds.writeInt(this.luong);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateItem(byte id, int numb) throws IOException {
        this.item[id] += numb;
        if (this.item[id] < 0) {
            this.item[id] = 0;
        }
        if (this.item[id] > ServerManager.max_item) {
            this.item[id] = (byte) ServerManager.max_item;
        }
        this.item[0] = this.item[1] = 99;
    }

    public int getLuong() {
        return this.luong;
    }

    public synchronized void updateLuong(int luongUp) {
        try {
            if (luongUp == 0) {
                return;
            }
            long sum = (long) luongUp + (long) this.luong;
            if (sum > Integer.MAX_VALUE) {
                this.luong = Integer.MAX_VALUE;
            } else if (sum < Integer.MIN_VALUE) {
                this.luong = Integer.MIN_VALUE;
            } else {
                this.luong += luongUp;
            }
            // Send update Xu
            Message ms = new Message(105);
            DataOutputStream ds = ms.writer();
            ds.writeInt(this.xu);
            ds.writeInt(this.luong);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDvong() {
        return this.dvong;
    }

    public synchronized void updateDvong(int dvUp) throws IOException {
        if (dvUp == 0) {
            return;
        }
        long sum = (long) dvUp + (long) this.dvong;
        if (sum > Integer.MAX_VALUE) {
            this.dvong = Integer.MAX_VALUE;
        } else if (sum < Integer.MIN_VALUE) {
            this.dvong = Integer.MIN_VALUE;
        } else {
            this.dvong += dvUp;
        }
        // Send update Dvong
        Message ms = new Message(-24);
        DataOutputStream ds = ms.writer();
        ds.writeByte(dvUp);
        ds.writeInt(this.dvong);
        ds.flush();
        sendMessage(ms);
    }

    public int getClan() {
        return this.clan;
    }

    public int getLevel() {
        return this.lever[this.nv];
    }

    public int getCS() {
        return this.nvCSinh[this.nv];
    }

    public byte getLevelPercen() {
        return this.leverPercen[this.nv];
    }

    public int getXP() {
        return this.xp[this.nv];
    }

    public synchronized void updateXP(int addXP, boolean canX2) {
        try {
            Calendar calendar = Calendar.getInstance();
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            if (canX2 && addXP > 0) {
                // Con x2 kinh nghiem, x2 kinh nghi?m
                if (this.xpX2Time.after(new Date())) {
                    addXP *= 2;
                }
                if (this.xpX6Time.after(new Date())) {
                    addXP *= 6;
                }
                // x0 
                if (this.xpX0Time.after(new Date())) {
                    addXP *= 0;
                }
                if (hours >= ServerManager.MIN_TIME_X6_1 && hours <= ServerManager.MAX_TIME_X6_1 || hours >= ServerManager.MIN_TIME_X6_2 && hours <= ServerManager.MAX_TIME_X6_2 || hours >= ServerManager.MIN_TIME_X6_3 && hours <= ServerManager.MAX_TIME_X6_3) {
                    addXP *= 6;
                }
                addXP *= ServerManager.KINH_NGHIEM_UP;
            }
            boolean updateLevel = false;
            int oldXP = this.xp[this.nv];
            int newXP;
            long sum = (long) addXP + (long) oldXP;
            if (sum > Integer.MAX_VALUE) {
                newXP = Integer.MAX_VALUE;
            } else if (sum < Integer.MIN_VALUE) {
                newXP = Integer.MIN_VALUE;
            } else {
                newXP = addXP + oldXP;
            }
            int nextLevel = 0;
            if (newXP > 0) {
                nextLevel = ((int) Math.sqrt(1 + newXP / 125) + 1) >> 1;
            }
            if (nextLevel > 2000) {
                nextLevel = 2000;
            }
            if (nextLevel < this.lever[this.nv]) {
                nextLevel = this.lever[this.nv];
                newXP = 1000 * nextLevel * (nextLevel - 1);
                addXP = newXP - oldXP;
            } else if (nextLevel > this.lever[this.nv]) {
                byte xpoint = 3;
                if (this.nvCSinh[this.nv] > 0) {
                    xpoint = 1;
                }
                this.point[this.nv] += (nextLevel - this.lever[this.nv]) * xpoint;
                this.lever[this.nv] = nextLevel;
                updateLevel = true;
            }
            this.xp[this.nv] = newXP;
            this.leverPercen[this.nv] = (byte) ((newXP - (nextLevel * (nextLevel - 1) * 500)) / nextLevel / 10);
            if (addXP == 0) {
                return;
            }
            Message ms = new Message(97);
            DataOutputStream ds = ms.writer();
            ds.writeInt(addXP);
            ds.writeInt(this.xp[this.nv]);
            ds.writeInt(this.lever[this.nv] * (this.lever[this.nv] + 1) * 1000);
            if (updateLevel) {
                ds.writeByte(1);
                int lvS = this.lever[this.nv];
                ds.writeByte("2.2.3".equals(client.version) ? (lvS > 127 ? 127 : lvS) : (lvS > 255 ? 255 : lvS));
                ds.writeByte(this.leverPercen[this.nv]);
                ds.writeShort(this.point[this.nv]);
            } else {
                ds.writeByte(0);
                ds.writeByte(this.leverPercen[this.nv]);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMissionInfo() throws IOException {
        Message ms;
        DataOutputStream ds;
        ms = new Message(-23);
        ds = ms.writer();
        for (int i = 0; i < MissionData.entrys.size(); i++) {
            MissDataEntry mDatE = MissionData.entrys.get(i);
            if (this.missionLevel[i] >= mDatE.entrys.size()) {
                continue;
            }
            MissionEntry me = mDatE.entrys.get(missionLevel[i] - 1);
            ds.writeByte(me.index);
            ds.writeByte(me.level);
            ds.writeUTF(me.name);
            ds.writeUTF(me.reward);
            ds.writeInt(me.require);
            ds.writeInt(mission[mDatE.idNeed - 1] > me.require ? me.require : mission[mDatE.idNeed - 1]);
            ds.writeBoolean(mission[mDatE.idNeed - 1] >= me.require);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void updateMission(int mission, int add) {
        if (mission < 0 || mission >= this.mission.length) {
            return;
        }
        this.mission[mission] += add;
    }

    public int getPoint() {
        return this.point[this.nv];
    }

    public short[] getPointAdd() {
        return this.pointAdd[this.nv];
    }

    public int[] getAbility() {
        int[] ability = new int[5];
        int[] envAdd = new int[5];
        int[] percenAdd = new int[5];
        for (int i = 0; i < 6; i++) {
            if (this.NvData[this.nv][i] < 0 || this.NvData[this.nv][i] >= this.ruongDoTB.size()) {
                continue;
            }
            ruongDoTBEntry rdE = this.ruongDoTB.get(this.NvData[this.nv][i]);
            // xem co con han ko?
            int hanSD = rdE.entry.hanSD - Until.getNumDay(rdE.dayBuy, new Date());
            if (hanSD <= 0) {
                continue;
            }
            for (int j = 0; j < 5; j++) {
                envAdd[j] += rdE.invAdd[j];
                percenAdd[j] += rdE.percenAdd[j];
            }
        }
        ability[0] = 1000 + this.pointAdd[nv][0] * 10 + envAdd[0] * 10;
        ability[0] += (1000 + this.pointAdd[nv][0]) * percenAdd[0] / 100;
        ability[1] = (NVData.entrys.get(nv).sat_thuong * (100 + (envAdd[1] + this.pointAdd[nv][1]) / 3 + percenAdd[1]) / 100) * 100 / NVData.entrys.get(nv).sat_thuong;
        ability[2] = (envAdd[2] + this.pointAdd[nv][2]) * 10;
        ability[2] += ability[2] * percenAdd[2] / 100;
        ability[3] = (envAdd[3] + this.pointAdd[nv][3]) * 10;
        ability[3] += ability[3] * percenAdd[3] / 100;
        ability[4] = (envAdd[4] + this.pointAdd[nv][4]) * 10;
        ability[4] += ability[4] * percenAdd[4] / 100;
        return ability;

///////////////////////////////////////
//        ability[0] = 1000 + this.pointAdd[nv][0] * 10 + envAdd[0] * 10;
//        ability[0] += ((1000 + this.pointAdd[nv][0] * 10) * percenAdd[0]) / 100;
//        ability[1] = (envAdd[1] + this.pointAdd[nv][1]) / 3 + 100 + percenAdd[1];
//        ability[2] = (envAdd[2] + this.pointAdd[nv][2]) * 10;
//        ability[2] += (ability[2] * percenAdd[2]) / 100;
//        ability[3] = (envAdd[3] + this.pointAdd[nv][3]) * 10;
//        ability[3] += (ability[3] * percenAdd[3]) / 100;
//        ability[4] = (envAdd[4] + this.pointAdd[nv][4]) * 10;
//        ability[4] += (ability[4] * percenAdd[4]) / 100;
//        return ability;
    }

    public byte getItemNum(int index) {
        if (index >= 0 && index < this.item.length) {
            return this.item[index];
        }
        return 0;
    }

    public short[] getEquip() {
        short[] equip = new short[5];
        if (this.nvEquip[this.nv][5] != null && this.nvEquip[this.nv][5].entry.isSet) {
            equip[0] = this.nvEquip[this.nv][5].entry.arraySet[0];
            equip[1] = this.nvEquip[this.nv][5].entry.arraySet[1];
            equip[2] = this.nvEquip[this.nv][5].entry.arraySet[2];
            equip[3] = this.nvEquip[this.nv][5].entry.arraySet[3];
            equip[4] = this.nvEquip[this.nv][5].entry.arraySet[4];
        } else {
            for (int i = 0; i < 5; i++) {
                if (this.nvEquip[this.nv][i] != null && !this.nvEquip[this.nv][i].entry.isSet) {
                    equip[i] = this.nvEquip[this.nv][i].entry.id;
                } else if (nvEquipDefault[this.nv][i] != null) {
                    equip[i] = nvEquipDefault[this.nv][i].id;
                } else {
                    equip[i] = -1;
                }
            }
        }
        return equip;
    }

    public short getGunId() {
        if (nvEquip[this.nv][0] != null) {
            return this.nvEquip[this.nv][0].entry.id;
        }
        return nvEquipDefault[this.nv][0].id;
    }

    public short getIDBullet() {
        if (nvEquip[this.nv][0] != null) {
            return this.nvEquip[this.nv][0].entry.bullId;
        }
        return nvEquipDefault[this.nv][0].bullId;
    }

    protected void sendTBInfo() throws IOException {
        Message ms = new Message(-7);
        DataOutputStream ds = ms.writer();
        for (int i = 0; i < 5; i++) {
            ds.writeInt(this.NvData[this.nv][i] | 0x10000);
        }
        ds.flush();
        sendMessage(ms);
    }

    protected void sendInfo() throws IOException {
        // Send mss 99
        Message ms = new Message(99);
        DataOutputStream ds = ms.writer();
        // lever
        int lvS = this.lever[this.nv];
        ds.writeByte("2.2.3".equals(client.version) ? (lvS > 127 ? 127 : lvS) : (lvS > 255 ? 255 : lvS));
        // lever %
        ds.writeByte(this.leverPercen[this.nv]);
        // Diem con lai de nang cap
        ds.writeShort(this.point[this.nv]);
        // So diem da cong
        for (int i = 0; i < 5; i++) {
            ds.writeShort(this.pointAdd[this.nv][i]);
        }
        // XP Get
        ds.writeInt(this.xp[this.nv]);
        // XP Max Lever
        ds.writeInt(this.lever[this.nv] * (this.lever[this.nv] + 1) * 1000);
        /* Danh vong */
        ds.writeInt(this.dvong);
        ds.flush();
        sendMessage(ms);
    }

    protected void sendRuongDoInfo() throws IOException {
        // Ruong trang bi
        Message ms = new Message(101);
        DataOutputStream ds = ms.writer();
        int lent = this.ruongDoTB.size();
        ds.writeByte(lent);
        for (int i = 0; i < lent; i++) {
            ruongDoTBEntry rdtbEntry = this.ruongDoTB.get(i);
            // dbKey
            ds.writeInt(i | 0x10000);
            // idNV
            ds.writeByte(rdtbEntry.entry.idNV);
            // EquipType
            ds.writeByte(rdtbEntry.entry.idEquipDat);
            // idEquip
            ds.writeShort(rdtbEntry.entry.id);
            // Name
            ds.writeUTF(rdtbEntry.entry.name + (rdtbEntry.cap > 0 ? String.format(" (+%d)", rdtbEntry.cap) : ""));
            // pointNV
            ds.writeByte(rdtbEntry.invAdd.length * 2);
            for (int j = 0; j < rdtbEntry.invAdd.length; j++) {
                ds.writeByte(rdtbEntry.invAdd[j]);
                ds.writeByte(rdtbEntry.percenAdd[j]);
            }
            // Ngay het han
            int hanSD = rdtbEntry.entry.hanSD - Until.getNumDay(rdtbEntry.dayBuy, new Date());
            if (hanSD < 0) {
                hanSD = 0;
            }
            ds.writeByte(hanSD);
            // Slot trong
            ds.writeByte(rdtbEntry.slotNull);
            // Vip I != 0 -> co tang % thoc tinh
            ds.writeByte(rdtbEntry.entry.isSet ? 1 : 0);
            // Vip Level
            ds.writeByte(rdtbEntry.vipLevel);
        }
        // DB Key
        for (int i = 0; i < 5; i++) {
            ds.writeInt(this.NvData[this.nv][i] | 0x10000);
        }
        ds.flush();
        sendMessage(ms);
        // Ruong do dac biet
        ms = new Message(125);
        ds = ms.writer();
        lent = this.ruongDoItem.size();
        ds.writeByte(0);
        ds.writeByte(lent);
        for (int i = 0; i < lent; i++) {
            ruongDoItemEntry rdiE = this.ruongDoItem.get(i);
            // Id
            ds.writeByte(rdiE.entry.id);
            // Numb 
            ds.writeShort(rdiE.numb);
            // Name
            ds.writeUTF(rdiE.entry.name);
            // Detail
            ds.writeUTF(rdiE.entry.detail);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void sendMessage(Message ms) throws IOException {
        this.client.sendMessage(ms);
    }

    public void netWait() {
        synchronized (this.client.obj) {
            try {
                this.client.obj.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public void notifyNetWait() {
        synchronized (this.client.obj) {
            this.client.obj.notifyAll();
        }
    }

    public void setFightWait(FightWait fw) {
        this.waitFight = fw;
        this.state = State.WaitFight;
    }

    public void setFightManager(FightManager fmng) {
        this.fight = fmng;
        this.state = State.Fighting;
    }

    protected static User login(Session cl, String user, String pass) {
        try {
            Message ms;
            DataOutputStream ds;
            User us = new User(cl);
            ResultSet red = SQLManager.getStatement().executeQuery("SELECT * FROM `user` WHERE (`user`=\"" + user + "\" AND `password`=\"" + pass + "\") LIMIT 1;");
            if (red != null && red.first()) {
                //  id user
                us.iddb = red.getInt("user_id");
                us.username = red.getString("user");
                boolean lock = red.getBoolean("lock");
                red.close();
                if (lock) {
                    ms = new Message(4);
                    ds = ms.writer();
                    ds.writeUTF("Tài khoản hiện không thể đăng nhập vui lòng liên hệ admin qua Zalo 0778.541.159 để biết thêm chi tiết !");
                    ds.flush();
                    cl.sendMessage(ms);
                    return null;
                }
                // Get user detals

                red = SQLManager.getStatement().executeQuery("SELECT * FROM `armymem` WHERE `id`=\"" + us.iddb + "\" LIMIT 1;");
                //neu armymem ko ton tại thi tạo mới from 
                if (!red.first()) {
                    SQLManager.getStatement().executeUpdate("INSERT INTO armymem(`id`, `xu`, `luong`, `ruongItem`, `ruongTrangBi`) VALUES (" + us.iddb + ", 1000, 0, '[]', '[]');");
                    red.close();
                    red = SQLManager.getStatement().executeQuery("SELECT * FROM `armymem` WHERE id=\"" + us.iddb + "\" LIMIT 1;");
                    red.first();
                }
                User us1 = ServerManager.getUser(us.iddb);
                if (us1 != null) {
                    red.close();
                    us1.client.close();
                    SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `online`=0, `idapp`=-1 WHERE `id`=" + us.iddb + " LIMIT 1;");
                    ms = new Message(4);
                    ds = ms.writer();
                    ds.writeUTF(GameString.loginErr1());
                    ds.flush();
                    cl.sendMessage(ms);

                    ms = new Message(10);
                    ds = ms.writer();
                    ds.writeUTF(GameString.userLoginMany());
                    ds.flush();
                    us1.sendMessage(ms);
                    red.close();
                    Thread.sleep(3000);
                    return null;
                }
                JSONArray friends = (JSONArray) JSONValue.parse(red.getString("friends"));
                if (friends != null && friends.size() > 0) {
                    us.friends = friends;
                } else {
                    us.friends = (JSONArray) JSONValue.parse("[2]");
                }
                // Send message login OK
                ms = new Message(3);
                ds = ms.writer();
                // iddb
                ds.writeInt(us.iddb);
                // xu
                ds.writeInt(us.xu = red.getInt("xu"));
                // luong
                ds.writeInt(us.luong = red.getInt("luong"));
                // dvong
                us.dvong = red.getInt("dvong");
                // chuyen sinh
                us.csinh = red.getShort("CSinh");
                // nhan vat
                ds.writeByte(us.nv = (byte) (red.getByte("NVused") - 1));
                // clan id
                ds.writeShort(us.clan = red.getShort("clan"));
                // x2 xp time
                us.xpX2Time = Until.getDate(red.getString("x2XPTime"));
                // x6 xp time
                us.xpX6Time = Until.getDate(red.getString("x6XPTime"));
                // x0 xp time
                us.xpX0Time = Until.getDate(red.getString("x0XPTime"));
                // diem sk
                us.eventScore = red.getInt("point_event");
                // bao danh
                us.baodanhsk = Until.getDate(red.getString("baodanhsk"));
                //
                us.event_halloween = red.getInt("point_halloween");
                // null byte
                ds.writeByte(0);

                int i, j, len = NVData.entrys.size();
                us.nvStt = new boolean[len];
                us.lever = new int[len];
                us.leverPercen = new byte[len];
                us.xp = new int[len];
                us.point = new short[len];
                us.pointAdd = new short[len][5];
                us.NvData = new int[len][6];
                us.nvCSinh = new byte[len];
                us.nvEquip = new ruongDoTBEntry[len][6];
                // nhan vat chiyen sinh
                JSONArray jarr1 = (JSONArray) JSONValue.parse(red.getString("nvCSinh"));
                if (jarr1 != null) {
                    for (i = 0; i < jarr1.size(); i++) {
                        JSONObject jobj1 = (JSONObject) jarr1.get(i);
                        us.nvCSinh[((Long) jobj1.get("nv")).byteValue()] = ((Long) jobj1.get("cs")).byteValue();
                    }
                }
                // Ruong Do
                jarr1 = (JSONArray) JSONValue.parse(red.getString("ruongTrangBi"));
                if (jarr1 != null) {
                    for (i = 0; i < jarr1.size(); i++) {
                        JSONObject jobj1 = (JSONObject) jarr1.get(i);
                        ruongDoTBEntry rdtbEntry = new ruongDoTBEntry();
                        int nvId = ((Long) jobj1.get("nvId")).intValue();
                        int equipType = ((Long) jobj1.get("equipType")).intValue();
                        int equipId = ((Long) jobj1.get("id")).intValue();

                        rdtbEntry.index = i;
                        rdtbEntry.entry = NVData.getEquipEntryById(nvId, equipType, equipId);
                        rdtbEntry.dayBuy = Until.getDate((String) jobj1.get("dayBuy"));
                        rdtbEntry.vipLevel = ((Long) jobj1.get("vipLevel")).byteValue();
                        rdtbEntry.isUse = (Boolean) jobj1.get("isUse");
                        rdtbEntry.invAdd = new short[5];
                        rdtbEntry.percenAdd = new short[5];
                        rdtbEntry.slot = new int[3];
                        rdtbEntry.anAdd = new short[5];
                        rdtbEntry.slotNull = 0;
                        if (jobj1.get("cap") != null) {
                            rdtbEntry.cap = ((Long) jobj1.get("cap")).byteValue();
                        } else {
                            rdtbEntry.cap = 0;
                        }
                        JSONArray jarr2 = (JSONArray) jobj1.get("invAdd");
                        for (int l = 0; l < 5; l++) {
                            rdtbEntry.invAdd[l] = ((Long) jarr2.get(l)).shortValue();
                        }
                        jarr2 = (JSONArray) jobj1.get("percenAdd");
                        for (int l = 0; l < 5; l++) {
                            rdtbEntry.percenAdd[l] = ((Long) jarr2.get(l)).shortValue();
                        }
                        jarr2 = (JSONArray) jobj1.get("slot");
                        for (int l = 0; l < 3; l++) {
                            rdtbEntry.slot[l] = ((Long) jarr2.get(l)).shortValue();
                            if (rdtbEntry.slot[l] == -1) {
                                rdtbEntry.slotNull++;
                            }
                        }
                        if (jobj1.get("an") != null) {
                            jarr2 = (JSONArray) jobj1.get("an");
                            for (int l = 0; l < 5; l++) {
                                rdtbEntry.anAdd[l] = ((Long) jarr2.get(l)).shortValue();
                            }
                        } else {
                            for (int l = 0; l < 5; l++) {
                                rdtbEntry.anAdd[l] = 0;
                            }
                        }
                        us.ruongDoTB.add(rdtbEntry);
                    }
                }

                jarr1 = (JSONArray) JSONValue.parse(red.getString("equip2"));
                if (jarr1 != null) {
                    for (i = 0; i < jarr1.size(); i++) {
                        JSONObject jobj1 = (JSONObject) jarr1.get(i);
                        equip2 eq2 = new equip2();
                        eq2.nv = (byte) ((Long) jobj1.get("nv")).intValue();
                        eq2.type = (byte) ((Long) jobj1.get("type")).intValue();
                        eq2.time = Until.getDate((String) jobj1.get("time"));
                        us.Equip2.add(eq2);
                    }
                }

                jarr1 = (JSONArray) JSONValue.parse(red.getString("ruongItem"));
                if (jarr1 != null) {
                    for (i = 0; i < jarr1.size(); i++) {
                        JSONObject jobj1 = (JSONObject) jarr1.get(i);
                        ruongDoItemEntry rdiEntry = new ruongDoItemEntry();
                        rdiEntry.entry = SpecialItemData.getSpecialItemById(((Long) jobj1.get("id")).intValue());
                        rdiEntry.numb = ((Long) jobj1.get("numb")).intValue();
                        us.ruongDoItem.add(rdiEntry);
                    }
                }

                for (i = 0; i < len; i++) {
                    JSONObject jobj = (JSONObject) JSONValue.parse(red.getString("NV" + (i + 1)));
                    /* lever */
                    us.lever[i] = ((Long) jobj.get("lever")).intValue();
                    /* xp % */
                    us.xp[i] = ((Long) jobj.get("xp")).intValue();
                    /* lever % */
                    us.leverPercen[i] = (byte) ((us.xp[i] - (us.lever[i] * (us.lever[i] - 1) * 500)) / us.lever[i] / 10);
                    /* point */
                    us.point[i] = ((Long) jobj.get("point")).shortValue();
                    /* pointAdd */
                    JSONArray jarr = (JSONArray) jobj.get("pointAdd");
                    for (j = 0; j < 5; j++) {
                        us.pointAdd[i][j] = ((Long) jarr.get(j)).shortValue();
                    }
                    /* data nhan vat */
                    jarr = (JSONArray) jobj.get("data");
                    us.NvData[i][5] = ((Long) jarr.get(5)).shortValue();
                    if (us.NvData[i][5] >= 0 && us.NvData[i][j] >= 0 && us.NvData[i][j] < us.ruongDoTB.size()) {
                        us.nvEquip[i][5] = us.ruongDoTB.get(us.NvData[i][j]);
                        ds.writeBoolean(true);
                        ds.writeShort(us.nvEquip[i][5].entry.arraySet[0]);
                        ds.writeShort(us.nvEquip[i][5].entry.arraySet[1]);
                        ds.writeShort(us.nvEquip[i][5].entry.arraySet[2]);
                        ds.writeShort(us.nvEquip[i][5].entry.arraySet[3]);
                        ds.writeShort(us.nvEquip[i][5].entry.arraySet[4]);
                    } else {
                        ds.writeBoolean(false);
                    }
                    for (j = 0; j < 5; j++) {
                        us.NvData[i][j] = ((Long) jarr.get(j)).shortValue();
                        if (us.NvData[i][j] >= 0 && us.NvData[i][j] < us.ruongDoTB.size()) {
                            ruongDoTBEntry rdE = us.ruongDoTB.get(us.NvData[i][j]);
                            if (rdE.entry.hanSD - Until.getNumDay(rdE.dayBuy, new Date()) > 0) {
                                us.nvEquip[i][j] = rdE;
                            } else {
                                rdE.isUse = false;
                            }
                        }
                        if (us.nvEquip[i][j] != null) {
                            ds.writeShort(us.nvEquip[i][j].entry.id);
                        } else if (nvEquipDefault[i][j] != null) {
                            ds.writeShort(nvEquipDefault[i][j].id);
                        } else {
                            ds.writeShort(-1);
                        }
                    }
                }
                // Item
                JSONArray jarr = (JSONArray) JSONValue.parse(red.getString("item"));
                us.item = new byte[jarr.size()];
                for (i = 0; i < jarr.size(); i++) {
                    us.item[i] = ((Long) jarr.get(i)).byteValue();
                    // So luong
                    ds.writeByte(us.item[i]);
                    // Lay item entry
                    ItemEntry itemEntry = ItemData.entrys.get(i);
                    // Gia xu
                    ds.writeInt(itemEntry.buyXu);
                    // Gia luong
                    ds.writeInt(itemEntry.buyLuong);
                }
                // Nv stt va gia mua
                int nvstt = red.getInt("sttnhanvat");
                for (i = 0; i < 10; i++) {
                    us.nvStt[i] = (nvstt & 1) > 0;
                    if (i > 2) {
                        ds.writeByte(us.nvStt[i] ? 1 : 0);
                        NVEntry nvEntry = NVData.entrys.get(i);
                        ds.writeShort(nvEntry.buyXu / 1000);
                        ds.writeShort(nvEntry.buyLuong);
                    }
                    nvstt = nvstt / 2;
                }
                // Thong tin them
                ds.writeUTF(ServerManager.addInfo);
                // Dia chi cua About me
                ds.writeUTF(ServerManager.addInfoURL);
                // Dia chi dang ki doi
                ds.writeUTF(ServerManager.regTeamURL);
                ds.flush();
                cl.sendMessage(ms);
                // Mission
                jarr = (JSONArray) JSONValue.parse(red.getString("mission"));
                us.mission = new int[jarr.size()];
                for (i = 0; i < jarr.size(); i++) {
                    us.mission[i] = ((Long) jarr.get(i)).intValue();
                }
                jarr = (JSONArray) JSONValue.parse(red.getString("missionLevel"));
                us.missionLevel = new byte[jarr.size()];
                for (i = 0; i < jarr.size(); i++) {
                    us.missionLevel[i] = ((Long) jarr.get(i)).byteValue();
                }
                Date newLongin = Until.getDate(red.getString("newOnline"));
                // Neu ko online hon 1 ngay -> gui item ngay
                if (Until.compare_Day(new Date(), newLongin)) {
                    byte idItem = (byte) (Until.nextInt(ItemData.entrys.size() - 2) + 2);
                    int numItem = (new int[]{1, 3, 5})[Until.nextInt(new int[]{500, 300, 200})];
                    us.updateItem(idItem, numItem);
                    us.sendMSSToUser(null, String.format(GameString.dailyReward(), numItem, ItemData.entrys.get(idItem).name));
                    us.updateMission(16, 1);
                    us.sendMSSToUser(null, ServerManager.SEND_THU1);
                    us.sendMSSToUser(null, ServerManager.SEND_THU2);
                    us.sendMSSToUser(null, ServerManager.SEND_THU3);
                    us.sendMSSToUser(null, ServerManager.SEND_THU4);
                    us.sendMSSToUser(null, ServerManager.SEND_THU5);
                }
                red.close();
                // Set online
                SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `online`=1,`newOnline`='" + Until.toDateString(new Date()) + "', `idapp`=" + cl.id + " WHERE `id`=" + us.iddb + " LIMIT 1;");
                // Luyen tap manager
                us.luyentap = new FightManager(us, ServerManager.ltapMap);
                // login true -> dua vao wait
                ServerManager.enterWait(us);
                ms = new Message(46);
                ds = ms.writer();
                ds.writeUTF(ServerManager.SEND_CHAT_LOGIN);
                ds.flush();
                cl.sendMessage(ms);
                Thread.sleep(1000);
                return us;
            } else {
                // Khong ton tai user
                ms = new Message(4);
                ds = ms.writer();
                ds.writeUTF(GameString.loginPassFail());
                ds.flush();
                cl.sendMessage(ms);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected ruongDoTBEntry getEquipNoNgoc(EquipmentEntry eqE, byte level) {
        for (int i = 0; i < this.ruongDoTB.size(); i++) {
            ruongDoTBEntry rdE = this.ruongDoTB.get(i);
            if (rdE != null && rdE.entry == eqE && !rdE.isUse && rdE.vipLevel == level && rdE.slotNull == 3 && rdE.entry.hanSD - Until.getNumDay(rdE.dayBuy, new Date()) > 0) {
                return rdE;
            }
        }
        return null;
    }

    protected int getNumItemRuong(int id) {
        // Kiem tra trong ruong co=>tang so luong. ko co=> tao moi
        for (int i = 0; i < this.ruongDoItem.size(); i++) {
            ruongDoItemEntry spE1 = ruongDoItem.get(i);
            if (spE1.entry.id == id) {
                return spE1.numb;
            }
        }
        return 0;
    }

    public synchronized void updateRuong(ruongDoTBEntry tbUpdate, ruongDoTBEntry addTB, int removeTB, ArrayList<ruongDoItemEntry> addItem, ArrayList<ruongDoItemEntry> removeItem) throws IOException {
        Message ms;
        DataOutputStream ds;
        if (addTB != null) {
            int bestLocation = -1;
            for (int i = 0; i < this.ruongDoTB.size(); i++) {
                ruongDoTBEntry rdtbE = this.ruongDoTB.get(i);
                if (rdtbE == null) {
                    bestLocation = i;
                    break;
                }
            }
            addTB.dayBuy = new Date();
            addTB.isUse = false;
            if (addTB.invAdd == null) {
                addTB.invAdd = new short[addTB.entry.invAdd.length];
                for (int j = 0; j < addTB.entry.invAdd.length; j++) {
                    addTB.invAdd[j] = addTB.entry.invAdd[j];
                }
            }
            if (addTB.percenAdd == null) {
                addTB.percenAdd = new short[addTB.entry.percenAdd.length];
                for (int j = 0; j < addTB.entry.percenAdd.length; j++) {
                    addTB.percenAdd[j] = addTB.entry.percenAdd[j];
                }
            }
            addTB.slotNull = 3;
            addTB.cap = addTB.entry.cap;
            addTB.slot = new int[3];
            for (int i = 0; i < 3; i++) {
                addTB.slot[i] = -1;
            }
            if (bestLocation == -1) {
                addTB.index = ruongDoTB.size();
                ruongDoTB.add(addTB);
            } else {
                addTB.index = bestLocation;
                ruongDoTB.set(bestLocation, addTB);
            }
            ms = new Message(104);
            ds = ms.writer();
            ds.writeByte(0);
            ds.writeInt(addTB.index | 0x10000);
            ds.writeByte(addTB.entry.idNV);
            ds.writeByte(addTB.entry.idEquipDat);
            ds.writeShort(addTB.entry.id);
            ds.writeUTF(addTB.entry.name);
            ds.writeByte(addTB.invAdd.length * 2);
            for (int i = 0; i < addTB.invAdd.length; i++) {
                ds.writeByte(addTB.invAdd[i]);
                ds.writeByte(addTB.percenAdd[i]);
            }
            ds.writeByte(addTB.entry.hanSD);
            ds.writeByte(addTB.entry.isSet ? 1 : 0);
            ds.writeByte(addTB.vipLevel);
            ds.flush();
            sendMessage(ms);
        }
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream ds1 = new DataOutputStream(bas);
        int nUpdate = 0;
        if (tbUpdate != null) {
            nUpdate++;
            ds1.writeByte(2);
            ds1.writeInt(tbUpdate.index | 0x10000);
            ds1.writeByte(tbUpdate.invAdd.length * 2);
            for (int i = 0; i < tbUpdate.invAdd.length; i++) {
                ds1.writeByte(tbUpdate.invAdd[i]);
                ds1.writeByte(tbUpdate.percenAdd[i]);
            }
            ds1.writeByte(tbUpdate.slotNull);
            // Ngay het han
            int hanSD = tbUpdate.entry.hanSD - Until.getNumDay(tbUpdate.dayBuy, new Date());
            if (hanSD < 0) {
                hanSD = 0;
            }
            ds1.writeByte(hanSD);
        }
        if (addItem != null && addItem.size() > 0) {
            for (int i = 0; i < addItem.size(); i++) {
                ruongDoItemEntry spE = addItem.get(i);
                if (spE.numb > 100) {
                    ruongDoItemEntry spE2 = new ruongDoItemEntry();
                    spE2.entry = spE.entry;
                    spE2.numb = spE.numb - 100;
                    spE.numb = 100;
                    addItem.add(spE2);
                }
                if (spE.numb <= 0) {
                    continue;
                }
                nUpdate++;
                // Kiem tra trong ruong co=>tang so luong. ko co=> tao moi
                boolean isHave = false;
                for (ruongDoItemEntry spE1 : ruongDoItem) {
                    if (spE1.entry.id == spE.entry.id) {
                        isHave = true;
                        spE1.numb += spE.numb;
                        break;
                    }
                }
                // ko co=> Tao moi
                if (!isHave) {
                    ruongDoItem.add(spE);
                }
                ds1.writeByte(spE.numb > 1 ? 3 : 1);
                ds1.writeByte(spE.entry.id);
                if (spE.numb > 1) {
                    ds1.writeByte(spE.numb);
                }
                ds1.writeUTF(spE.entry.name);
                ds1.writeUTF(spE.entry.detail);
            }
        }
        if (removeItem != null && removeItem.size() > 0) {
            for (int k = 0; k < removeItem.size(); k++) {
                ruongDoItemEntry spE = removeItem.get(k);
                if (spE.numb > 100) {
                    ruongDoItemEntry spE2 = new ruongDoItemEntry();
                    spE2.entry = spE.entry;
                    spE2.numb = spE.numb - 100;
                    spE.numb = 100;
                    removeItem.add(spE2);
                }
                if (spE.numb <= 0) {
                    continue;
                }
                // Kiem tra trong ruong co=>giam so luong
                for (int i = 0; i < ruongDoItem.size(); i++) {
                    ruongDoItemEntry spE1 = ruongDoItem.get(i);
                    if (spE1.entry.id == spE.entry.id) {
                        if (spE1.numb < spE.numb) {
                            spE.numb = spE1.numb;
                        }
                        spE1.numb -= spE.numb;
                        if (spE1.numb == 0) {
                            ruongDoItem.remove(i);
                        }
                        nUpdate++;
                        ds1.writeByte(0);
                        ds1.writeInt(spE.entry.id);
                        ds1.writeByte(spE.numb);
                        break;
                    }
                }
            }
        }
        if (removeTB >= 0 && removeTB < ruongDoTB.size() && ruongDoTB.get(removeTB) != null) {
            nUpdate++;
            ruongDoTB.set(removeTB, null);
            ds1.writeByte(0);
            ds1.writeInt(removeTB | 0x10000);
            ds1.writeByte(1);
        }
        ds1.flush();
        bas.flush();
        if (nUpdate == 0) {
            return;
        }
        ms = new Message(27);
        ds = ms.writer();
        ds.writeByte(nUpdate);
        ds.write(bas.toByteArray());
        ds.flush();
        sendMessage(ms);
    }

    protected int flushCache() {
        System.out.println("Flush cache in : " + client);
        try {
            // Not online
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `online`=0,`idapp`=-1,`lastOnline`='" + Until.toDateString(new Date()) + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
            JSONArray Jarr1 = new JSONArray();
            for (int i = 0; i < this.item.length; i++) {
                Jarr1.add(item[i]);
            }
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `item`='" + Jarr1.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
            byte nvXPMax = 1;
            int xpMax = 0;
            for (int i = 0; i < this.lever.length; i++) {
                if (!this.nvStt[i]) {
                    continue;
                }
                ResultSet red = SQLManager.getStatement().executeQuery("SELECT `NV" + (i + 1) + "` FROM `armymem` WHERE `id`=" + this.iddb + " LIMIT 1;");
                if ((red != null) && (red.first())) {
                    JSONObject nvdata = (JSONObject) JSONValue.parse(red.getString("NV" + (i + 1)));
                    red.close();
                    nvdata.put("lever", this.lever[i]);
                    nvdata.put("xp", this.xp[i]);
                    nvdata.put("point", this.point[i]);
                    Jarr1.clear();
                    for (int j = 0; j < 5; j++) {
                        Jarr1.add(this.pointAdd[i][j]);
                    }
                    nvdata.put("pointAdd", Jarr1);
                    JSONArray Jarr2 = new JSONArray();
                    for (int j = 0; j < 6; j++) {
                        Jarr2.add(this.NvData[i][j]);
                    }
                    nvdata.put("data", Jarr2);
                    SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `NV" + (i + 1) + "`='" + nvdata.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
                    if (this.xp[i] > xpMax) {
                        nvXPMax = (byte) (i + 1);
                        xpMax = this.xp[i];
                    }
                }
            }
            // Ruong Do
            Jarr1.clear();
            for (ruongDoTBEntry rdtbEntry : ruongDoTB) {
                if (rdtbEntry == null) {
                    continue;
                }
                JSONObject tbEntry = new JSONObject();
                tbEntry.put("nvId", rdtbEntry.entry.idNV);
                tbEntry.put("equipType", rdtbEntry.entry.idEquipDat);
                tbEntry.put("id", rdtbEntry.entry.id);
                tbEntry.put("dayBuy", Until.toDateString(rdtbEntry.dayBuy));
                tbEntry.put("vipLevel", rdtbEntry.vipLevel);
                tbEntry.put("isUse", rdtbEntry.isUse);
                tbEntry.put("cap", rdtbEntry.cap);
                JSONArray Jarr2 = new JSONArray();
                for (int i = 0; i < 5; i++) {
                    Jarr2.add(rdtbEntry.invAdd[i]);
                }
                tbEntry.put("invAdd", Jarr2);
                JSONArray Jarr3 = new JSONArray();
                for (int i = 0; i < 5; i++) {
                    Jarr3.add(rdtbEntry.percenAdd[i]);
                }
                tbEntry.put("percenAdd", Jarr3);
                JSONArray Jarr4 = new JSONArray();
                for (int i = 0; i < 3; i++) {
                    Jarr4.add(rdtbEntry.slot[i]);
                }
                tbEntry.put("slot", Jarr4);
                Jarr1.add(tbEntry);
            }
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `ruongTrangBi`='" + Jarr1.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");

            // trang bi 2
            Jarr1.clear();
            for (equip2 eq : Equip2) {
                if (eq == null) {
                    continue;
                }
                JSONObject ent = new JSONObject();
                ent.put("nv", eq.nv);
                ent.put("type", eq.type);
                ent.put("time", Until.toDateString(eq.time));
                Jarr1.add(ent);
            }
            SQLManager.stat.executeUpdate("UPDATE `armymem` SET `equip2`='" + Jarr1.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");

            Jarr1.clear();
            for (ruongDoItemEntry rdiEntry : ruongDoItem) {
                JSONObject tbEntry = new JSONObject();
                tbEntry.put("id", rdiEntry.entry.id);
                tbEntry.put("numb", rdiEntry.numb);
                Jarr1.add(tbEntry);
            }
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `ruongItem`='" + Jarr1.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");

            // Mission
            Jarr1.clear();
            for (int i = 0; i < this.mission.length; i++) {
                Jarr1.add(this.mission[i]);
            }
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `mission`='" + Jarr1.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");

            Jarr1.clear();
            for (int i = 0; i < this.missionLevel.length; i++) {
                Jarr1.add(this.missionLevel[i]);
            }
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `missionLevel`='" + Jarr1.toJSONString() + "' WHERE `id`=" + this.iddb + " LIMIT 1;");

            int nvstt = 1, pow = 1;
            for (int i = 0; i < this.nvStt.length; i++) {
                nvstt |= this.nvStt[i] ? pow : 0;
                pow <<= 1;
            }
            // su kien
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `point_event`=" + this.eventScore + ", `baodanhsk` = '" + Until.toDateString(this.baodanhsk) + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
            //halloween
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `point_halloween`='" + this.event_halloween + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
            //Chuyen Sinh
            Jarr1.clear();
            for (byte i = 0; i < this.nvCSinh.length; i++) {
                if (this.nvCSinh[i] == 0) {
                    continue;
                }
                JSONObject csnv = new JSONObject();
                csnv.put("nv", i);
                csnv.put("cs", this.nvCSinh[i]);
                Jarr1.add(csnv);
            }
            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `nvCSinh`='" + Jarr1.toJSONString() + "', `CSinh` = '" + this.csinh + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
            // Xu, luong, ...
            //SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `friends` = '" + this.friends.toJSONString() + "', `xu`='" + this.xu + "',`luong`='" + this.luong + "',`dvong`='" + this.dvong + "',`NVused`='" + (this.nv + 1) + "',`sttnhanvat`='" + nvstt + "',`x2XPTime`='" + Until.toDateString(xpX2Time) + "',`x0XPTime`='" + Until.toDateString(xpX0Time) + "',`point_event` = " + this.eventScore + " ,`nvXPMax`='" + nvXPMax + "',`xpMax`='" + xpMax + "' WHERE `id`=" + this.iddb + " LIMIT 1;");

            SQLManager.getStatement().executeUpdate("UPDATE `armymem` SET `friends` = '" + this.friends.toJSONString() + "', `xu`='" + this.xu + "',`luong`='" + this.luong + "',`dvong`='" + this.dvong + "',`NVused`='" + (this.nv + 1) + "',`sttnhanvat`='" + nvstt + "',`x2XPTime`='" + Until.toDateString(xpX2Time) + "',`x6XPTime`='" + Until.toDateString(xpX6Time) + "',`x0XPTime`='" + Until.toDateString(xpX0Time) + "',`point_halloween`='" + this.event_halloween + "',`point_event` = " + this.eventScore + " ,`nvXPMax`='" + nvXPMax + "',`xpMax`='" + xpMax + "' WHERE `id`=" + this.iddb + " LIMIT 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected void close() throws IOException {
        if (this.client.login && (this.state == State.Fighting) && (this.fight != null)) {
            this.fight.leave(this);
        }
        if ((this.state == State.WaitFight) && (this.waitFight != null)) {
            this.waitFight.leave(this);
        }
        if (this.client.login) {
            this.flushCache();
        }
    }

    @Override
    public String toString() {
        return "User " + this.username;
    }

    protected void hopTBMessage(Message ms) throws IOException {
        byte materialId = ms.reader().readByte();
        byte action = ms.reader().readByte();
        byte index = -1;
        if (action == 2) {
            index = ms.reader().readByte();
        }
        DataOutputStream ds;
        ms = new Message(-18);
        ds = ms.writer();
        if (action == 1) {
            ds.writeByte(1);
            FomularDataEntry fDatE = FomularData.getFomularDataEntryById(materialId);
            if (fDatE == null) {
                return;
            }
            NVEntry nvE = NVData.entrys.get(this.nv);
            ds.writeByte(fDatE.ins.id);
            ds.writeByte(fDatE.entrys.size());
            for (int i = 0; i < fDatE.entrys.size(); i++) {
                FomularEntry fE = fDatE.entrys.get(i);
                ds.writeByte(fDatE.equip[this.nv].id);
                ds.writeUTF(fDatE.equip[this.nv].name + " " + nvE.name + " cấp " + fE.level);
                ds.writeByte(fE.levelRequire);
                ds.writeByte(this.nv);
                ds.writeByte(fDatE.equipType);
                ds.writeByte(fE.itemNeed.length);
                boolean isFinish = true;
                for (int j = 0; j < fE.itemNeed.length; j++) {
                    int itemNumHave = getNumItemRuong(fE.itemNeed[j].id);
                    ds.writeByte(fE.itemNeed[j].id);
                    ds.writeUTF(fE.itemNeed[j].name);
                    ds.writeByte(fE.itemNeedNum[j]);
                    ds.writeByte(itemNumHave > fE.itemNeedNum[j] ? fE.itemNeedNum[j] : itemNumHave);
                    if (itemNumHave < fE.itemNeedNum[j]) {
                        isFinish = false;
                    }
                }
                boolean isHave;
                if (fE.level == 1) {
                    ds.writeByte(fDatE.equipNeed[this.nv].id);
                    ds.writeUTF(fDatE.equipNeed[this.nv].name);
                    isHave = getEquipNoNgoc(fDatE.equipNeed[this.nv], (byte) 0) != null;
                } else {
                    ds.writeByte(fDatE.equip[this.nv].id);
                    ds.writeUTF(fDatE.equip[this.nv].name);
                    isHave = getEquipNoNgoc(fDatE.equip[this.nv], (byte) (fE.level - 1)) != null;
                }
                if (!isHave) {
                    isFinish = false;
                }
                ds.writeByte(fE.level - 1);
                ds.writeBoolean(isHave);
                ds.writeBoolean(isFinish);
                ds.writeByte(fE.detail.length);
                for (int j = 0; j < fE.detail.length; j++) {
                    ds.writeUTF(fE.detail[j]);
                }
            }
        }
        if (action == 2) {
            FomularDataEntry fDatE = FomularData.getFomularDataEntryById(materialId);
            if (fDatE == null || index < 0 || index >= fDatE.entrys.size()) {
                return;
            }
            ArrayList<ruongDoItemEntry> arrayI = new ArrayList<>();
            ruongDoTBEntry rdE = new ruongDoTBEntry(), rdE2;
            rdE.entry = fDatE.equip[this.nv];
            FomularEntry fE = fDatE.entrys.get(index);
            boolean isFinish = true;
            for (int j = 0; j < fE.itemNeed.length; j++) {
                int itemNumHave = getNumItemRuong(fE.itemNeed[j].id);
                if (itemNumHave < fE.itemNeedNum[j]) {
                    isFinish = false;
                    break;
                }
                ruongDoItemEntry rdE1 = new ruongDoItemEntry();
                rdE1.entry = fE.itemNeed[j];
                rdE1.numb = fE.itemNeedNum[j];
                arrayI.add(rdE1);
            }
            if (fE.level == 1) {
                rdE2 = getEquipNoNgoc(fDatE.equipNeed[this.nv], (byte) 0);
            } else {
                rdE2 = getEquipNoNgoc(fDatE.equip[this.nv], (byte) (fE.level - 1));
            }
            if (rdE2 == null) {
                isFinish = false;
            }
            int numFomular = getNumItemRuong(materialId);
            if (isFinish && (numFomular > 0 || this.xu >= fDatE.ins.buyXu)) {
                if (numFomular == 0) {
                    this.updateXu(fDatE.ins.buyXu);
                } else {
                    ruongDoItemEntry rdE1 = new ruongDoItemEntry();
                    rdE1.entry = fDatE.ins;
                    rdE1.numb = 1;
                    arrayI.add(rdE1);
                }
                rdE.vipLevel = fE.level;
                rdE.invAdd = new short[5];
                rdE.percenAdd = new short[5];
                for (int i = 0; i < 5; i++) {
                    rdE.invAdd[i] = (short) Until.nextInt(fE.invAddMin[i], fE.invAddMax[i]);
                    rdE.percenAdd[i] = (short) Until.nextInt(fE.percenAddMin[i], fE.percenAddMax[i]);
                }
                updateRuong(null, rdE, rdE2.index, null, arrayI);
                ds.writeByte(0);
                ds.writeUTF(GameString.cheDoSuccess());
            } else {
                ds.writeByte(0);
                ds.writeUTF(GameString.cheDoFail());
            }
        }
        ds.flush();
        sendMessage(ms);
    }

    protected void hopNgocMessage(Message ms) throws IOException {
        DataOutputStream ds;
        byte action = ms.reader().readByte();
        // Set hop ngoc
        if (action == 0) {
            byte lent = ms.reader().readByte();
            this.hopNgocAction = 0;
            this.hopNgocGia = 0;
            this.hopNgocNum = 0;
            this.hopNgocItemArray.clear();
            this.hopNgocItem = null;
            this.hopNgocTB = null;
            int[] items = new int[127];
            int numberItem = 0;
            boolean hasItemNotNgoc = false;
            for (int i = 0; i < lent; i++) {
                int id = ms.reader().readInt();
                int numb = ms.reader().readUnsignedByte();
                if (numb <= 0) {
                    continue;
                }
                if (getNumItemRuong(id) >= numb) {
                    items[id] = numb;
                    numberItem++;
                }
                if ((id & 0x10000) > 0) {
                    id &= 0xFFFF;
                    if (id >= 0 && id < this.ruongDoTB.size() && this.hopNgocTB == null) {
                        this.hopNgocTB = ruongDoTB.get(id);
                    } else {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.hopNgocError());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                } else {
                    if (numb > this.getNumItemRuong(id)) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.hopNgocError());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (this.getNumItemRuong(id) == 0) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.hopNgocError());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    ruongDoItemEntry rdE = new ruongDoItemEntry();
                    rdE.entry = SpecialItemData.getSpecialItemById(id);
                    rdE.numb = numb;
                    hopNgocItemArray.add(rdE);
                    hopNgocNum += numb;
                    if (id < 50 || id > 93 && id < 99 || id > 105 && id < 109 || id > 110 && id < 114) {//ngoc
                        this.hopNgocGia += rdE.entry.buyXu * numb;
                    } else {
                        hasItemNotNgoc = true;
                    }
                }
            }
            ruongDoItemEntry[] rdEArr = new ruongDoItemEntry[hopNgocItemArray.size()];
            for (int i = 0; i < hopNgocItemArray.size(); i++) {
                rdEArr[i] = this.hopNgocItemArray.get(i);
            }
            if (hopNgocTB != null && hopNgocItemArray.size() == 1 && !hasItemNotNgoc) {
                this.hopNgocAction = 3;
                ms = new Message(17);
                ds = ms.writer();
                ds.writeByte(0);
                ds.writeUTF(GameString.hopNgocRequest());
                ds.flush();
                sendMessage(ms);
                return;
            }
            if (hopNgocTB != null) {
                int[] pt = new int[]{90, 85, 80, 70, 60, 55, 50, 45, 40, 35, 30, 25, 20, 18, 15, 8, 5, 3, 2, 1};
                if (hopNgocTB.cap < 20) {
                    ms = new Message(17);
                    ds = ms.writer();
                    ds.writeByte(0);
                    if (rdEArr.length == 2 && (rdEArr[0].entry.id == 80 && rdEArr[1].entry.id == 81) || rdEArr[0].entry.id == 81 && rdEArr[1].entry.id == 80) {
                        this.baoHiem = true;
                        this.hopNgocAction = 7;
                        ds.writeUTF(String.format(GameString.dapDoRequest_1(), (hopNgocTB.cap + 1), pt[hopNgocTB.cap]) + "%)?");
                    } else if (rdEArr.length == 1 && rdEArr[0].entry.id == 80) {
                        this.baoHiem = false;
                        this.hopNgocAction = 7;
                        if (hopNgocTB.cap > 4 && hopNgocTB.cap % 4 != 0) {
                            ds.writeUTF(String.format(GameString.dapDoRequest_2(), (hopNgocTB.cap + 1), pt[hopNgocTB.cap]) + "%)?");
                        } else {
                            ds.writeUTF(String.format(GameString.dapDoRequest_1(), (hopNgocTB.cap + 1), pt[hopNgocTB.cap]) + "%)?");
                        }
                    }
                    ds.flush();
                    sendMessage(ms);
                } else {
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF("Trang bị đã đạt cấp tối đa!.");
                    ds.flush();
                    sendMessage(ms);
                }
                return;
            }
            if (hopNgocTB == null) {
                for (FabricateItem fabricate : FabricateItem.entrys) {
                    if (fabricate.itemRequire.size() == hopNgocItemArray.size()) {
                        boolean isFull = true;
                        for (FabricateItem.Item item : fabricate.itemRequire) {
                            if (items[item.id] != item.quantity) {
                                isFull = false;
                                break;
                            }
                        }
                        if (isFull) {
                            this.hopNgocAction = 20;
                            this.fabricateId = fabricate.id;
                            ms = new Message(17);
                            ds = ms.writer();
                            ds.writeByte(0);
                            ds.writeUTF(fabricate.notification1);
                            ds.flush();
                            sendMessage(ms);
                            return;
                        }
                    }
                }
            }

            if (hopNgocTB == null && hopNgocItemArray.size() == 1) {
                ruongDoItemEntry rdE = hopNgocItemArray.get(0);
                if (rdE.entry.id >= 50) {
                    if (rdE.entry.id == 50) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF(GameString.phucHoiDiemString());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 109) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF("Khi sử dụng bạn sẽ bị thoát, Bạn có muốn sử dụng không?");
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 54) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF(GameString.x2XPRequest());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 105) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF(GameString.x6XPRequest());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id > 73 && rdE.entry.id < 77) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF("Khi sử dụng sẽ mất tác dụng của viên cũ, bạn có chắc chắn muốn sử dụng không ?");
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 50) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF(GameString.phucHoiDiemString());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 51 || rdE.entry.id == 121 || rdE.entry.id == 125 || rdE.entry.id == 126 || rdE.entry.id == 127) {
                        if (rdE.entry.id == 121) {
                            updateEventHalloween(rdE.numb);
                        }
                        String str = "";
                        updateRuong(null, null, -1, null, hopNgocItemArray);
                        byte[] arItem1 = new byte[]{62, 63, 64, 65, 66, 67, 68};
                        byte[] arItem2 = new byte[]{0, 10, 20, 30, 40};
                        byte[] arItem3 = new byte[]{50, 54};
                        byte[] arItem4 = new byte[]{89};
                        for (byte i = 0; i < rdE.numb; i++) {
                            switch (Until.nextInt(100)) {
                                case 0:
                                case 1:
                                case 7:
                                case 11:
                                case 12:
                                case 13:
                                case 34:
                                case 33:
                                case 45:
                                case 25:
                                case 49:
                                    byte iditem = arItem1[Until.nextInt(arItem1.length)];
                                    this.updateSpecialItem(iditem, 1);
                                    str = SpecialItemData.getSpecialItemById(iditem).name;
                                    break;
                                case 5:
                                case 6:
                                case 9:
                                case 18:
                                case 50:
                                case 44:
                                case 47:
                                case 43:
                                case 28:
                                case 31:
                                    iditem = arItem3[Until.nextInt(arItem3.length)];
                                    this.updateSpecialItem(iditem, 1);
                                    str = SpecialItemData.getSpecialItemById(iditem).name;
                                    break;
                                case 17:
                                case 29:
                                case 77:
                                case 65:
                                case 88:
                                    iditem = (byte) (Until.nextInt(4) + arItem2[Until.nextInt(arItem2.length)]);
                                    this.updateSpecialItem(iditem, 1);
                                    str = SpecialItemData.getSpecialItemById(iditem).name;
                                    break;
                                case 8:
                                    switch (Until.nextInt(10)) {
                                        case 0:
                                            iditem = arItem4[Until.nextInt(arItem4.length)];
                                            this.updateSpecialItem(iditem, 1);
                                            str = SpecialItemData.getSpecialItemById(iditem).name;
                                            break;
                                        case 1:
                                            int xuup = new int[]{100000, 200000, 300000}[Until.nextInt(3)];
                                            this.updateXu(xuup);
                                            str = Until.getStringNumber(xuup) + " xu";
                                            break;
                                        default:
                                            int xpup = new int[]{100000, 200000, 300000}[Until.nextInt(3)];
                                            this.updateXP(xpup, false);
                                            str = Until.getStringNumber(xpup) + " Kinh nghiệm";
                                            break;
                                    }
                                    ms = new Message(46);
                                    ds = ms.writer();
                                    ds.writeUTF(this.getUserName().toUpperCase() + " sử dụng " + rdE.entry.name + " nhận được " + str);
                                    ds.flush();
                                    ServerManager.sendToServer(ms);
                                default:
                                    int xpup = new int[]{1000, 2000, 3000, 4000, 5000}[Until.nextInt(5)];
                                    this.updateXP(xpup, false);
                                    str = Until.getStringNumber(xpup) + " Kinh nghiệm";
                                    break;
                            }
                            this.sendMSSToUser(null, String.format(GameString.missionComplete(), "10K Kinh nghiệm, " + str));
                        }
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(String.format(GameString.missionComplete(), "10K Kinh nghiệm, " + str));
                        ds.flush();
                        this.sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 78) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF(GameString.exchangeGift());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 89) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF("Bạn có muốn chuyển sinh từ lv 2000 về lv 1 không!");
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 90) {
                        this.hopNgocAction = 6;
                        ms = new Message(17);
                        ds = ms.writer();
                        ds.writeByte(0);
                        ds.writeUTF(GameString.x0XPRequest());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    if (rdE.entry.id == 93) {
                        updateEventScore(1);//add diểm skien
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF("Bạn nhận 1 điểm sự kiện");
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                } else if (((rdE.entry.id + 1) % 10 != 0) && rdE.numb >= 5) {
                    this.hopNgocAction = 5;
                    ms = new Message(17);
                    ds = ms.writer();
                    ds.writeByte(0);
                    ds.writeUTF(String.format(GameString.hopNgocNC(), (100 - (rdE.entry.id % 10) * 10) + "%"));
                    ds.flush();
                    sendMessage(ms);
                    return;
                }
            }
            if (hopNgocTB == null && !hopNgocItemArray.isEmpty() && !hasItemNotNgoc) {
                this.hopNgocAction = 2;
                ms = new Message(17);
                ds = ms.writer();
                ds.writeByte(0);
                ds.writeUTF(String.format(GameString.hopNgocSell(), hopNgocNum, (hopNgocGia / 2)));
                ds.flush();
                sendMessage(ms);
                return;
            }
            ms = new Message(45);
            ds = ms.writer();
            ds.writeUTF(GameString.hopNgocCantDo());
            ds.flush();
            sendMessage(ms);
            return;
        }
        // Hop ngoc
        if (action == 1) {
            switch (this.hopNgocAction) {
                case 20:
                    int[] items = new int[127];
                    for (ruongDoItemEntry rdE : hopNgocItemArray) {
                        items[rdE.entry.id] = rdE.numb;
                    }
                    boolean isFull = true;
                    FabricateItem fab = FabricateItem.getFabricateById(this.fabricateId);
                    for (FabricateItem.Item item : fab.itemRequire) {
                        if (item.quantity != items[item.id]) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Số lượng không hợp lệ!");
                            ds.flush();
                            sendMessage(ms);
                            return;
                        }
                    }
                    updateXu(fab.rewardXu);
                    updateLuong(fab.rewardLuong);
                    updateXP(fab.rewardExp, false);
                    updateDvong(fab.rewardCup);
                    updateRuong(null, null, -1, null, hopNgocItemArray);
                    for (FabricateItem.Item iitem : fab.rewardItem) {
                        updateSpecialItem(iitem.id, iitem.quantity);
                    }
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(fab.notification2);
                    ds.flush();
                    sendMessage(ms);
                    break;
                case 2:
                    updateRuong(null, null, -1, null, hopNgocItemArray);
                    updateXu(hopNgocGia / 2);
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(GameString.buySuccess());
                    ds.flush();
                    sendMessage(ms);
                    break;
                case 3:
                    if (hopNgocTB != null) {
                        if (hopNgocTB.slotNull >= this.hopNgocNum) {
                            ruongDoItemEntry rdE = this.hopNgocItemArray.get(0);
                            rdE.numb = 1;
                            SpecialItemEntry entry = rdE.entry;
                            hopNgocTB.slot[3 - hopNgocTB.slotNull] = entry.id;
                            for (int j = 0; j < 5; j++) {
                                hopNgocTB.invAdd[j] += entry.ability[j];
                            }
                            hopNgocTB.slotNull--;
                            updateRuong(hopNgocTB, null, -1, null, hopNgocItemArray);
                        } else {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.hopNgocNoSlot());
                            ds.flush();
                            sendMessage(ms);
                        }
                    }
                    break;
                case 5: {
                    ruongDoItemEntry rdE = this.hopNgocItemArray.get(0);
                    ruongDoItemEntry rdE1 = new ruongDoItemEntry();
                    ruongDoItemEntry rdE2 = new ruongDoItemEntry();
                    rdE1.entry = rdE.entry;
                    rdE2.entry = SpecialItemData.getSpecialItemById(rdE.entry.id + 1);
                    rdE1.numb = 0;
                    rdE2.numb = 0;
                    int pt = 100 - (rdE.entry.id % 10) * 10;
                    while (rdE.numb >= 5) {
                        if (Until.nextInt(100) < pt) {
                            rdE2.numb++;
                            rdE1.numb += 5;
                        } else {
                            rdE1.numb++;
                        }
                        rdE.numb -= 5;
                    }
                    ArrayList<ruongDoItemEntry> arrayI1 = new ArrayList<>();
                    ArrayList<ruongDoItemEntry> arrayI2 = new ArrayList<>();
                    arrayI1.add(rdE1);
                    arrayI2.add(rdE2);
                    updateRuong(null, null, -1, arrayI2, arrayI1);
                    ms = new Message(45);
                    ds = ms.writer();
                    if (rdE2.numb > 0) {
                        switch (rdE2.entry.id % 10) {
                            case 7:
                                updateMission(9, rdE2.numb);
                                break;
                            case 8:
                                updateMission(10, rdE2.numb);
                                break;
                            case 9:
                                updateMission(11, rdE2.numb);
                                break;
                            default:
                                break;
                        }
                        ds.writeUTF(String.format(GameString.hopNgocSucess(), rdE1.numb, rdE1.entry.name, rdE2.numb, rdE2.entry.name));
                    } else {
                        ds.writeUTF(String.format(GameString.hopNgocFail(), rdE1.numb, rdE1.entry.name));
                    }
                    ds.flush();
                    sendMessage(ms);
                    break;
                }
                case 6: {
                    ruongDoItemEntry rdE = this.hopNgocItemArray.get(0);
                    if (rdE.entry.id != 86 && rdE.entry.id != 78) {
                        rdE.numb = 1;
                    }
                    // maxitem
                    if (rdE.entry.id == 109) {
                        this.item[1] = this.item[2] = this.item[3] = 99;
                        this.item[4] = this.item[5] = this.item[6] = 99;
                        this.item[7] = this.item[8] = this.item[9] = 99;
                        this.item[10] = this.item[11] = this.item[12] = 99;
                        this.item[13] = this.item[14] = this.item[15] = 99;
                        this.item[16] = this.item[17] = this.item[18] = 99;
                        this.item[19] = this.item[20] = this.item[21] = 99;
                        this.item[22] = this.item[23] = this.item[24] = 99;
                        this.item[25] = this.item[26] = this.item[27] = 99;
                        this.item[28] = this.item[29] = this.item[30] = 99;
                        this.item[31] = this.item[32] = this.item[33] = this.item[34] = this.item[35] = 99;
                        ms = new Message(45);
                        ds = ms.writer();
                        this.client.close();
                        ds.flush();
                        sendMessage(ms);
                    } else if (rdE.entry.id == 50) {
                        if (this.nvCSinh[this.nv] > 12) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("CS của bạn là ( " + this.nvCSinh[this.nv] + " ) Không thể sử dụng vật phẩm này, vui lòng cộng điểm trên website để không bị âm");
                            ds.flush();
                            sendMessage(ms);
                            return;
                        }
                        byte xpoint = 3;
                        this.point[this.nv] = 0;
                        if (this.nvCSinh[this.nv] > 0) {
                            xpoint = 1;
                            this.point[this.nv] = (short) ((2000 * 3) + (this.nvCSinh[this.nv] - 1) * 2000);
                        }
                        this.point[this.nv] += (short) ((this.lever[this.nv] - 1) * xpoint);
                        this.pointAdd[this.nv][0] = 0;
                        this.pointAdd[this.nv][1] = 0;
                        this.pointAdd[this.nv][2] = 10;
                        this.pointAdd[this.nv][3] = 10;
                        this.pointAdd[this.nv][4] = 10;
                        sendPointAddInfo();
                        updateRuong(null, null, -1, null, hopNgocItemArray);
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.phucHoiSuccess());
                        ds.flush();
                        sendMessage(ms);
                    } else if (rdE.entry.id == 54) {
                        Date dat = new Date();
                        if (this.xpX2Time.before(dat)) {
                            xpX2Time = dat;
                        }
                        Until.addNumDay(xpX2Time, 1);
                        updateRuong(null, null, -1, null, hopNgocItemArray);
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.x2XPSuccess());
                        ds.flush();
                        sendMessage(ms);
                    }

//                    if (rdE.entry.id == 77 || rdE.entry.id == 78 || rdE.entry.id == 79 || rdE.entry.id == 80) {
//                        if (rdE.numb == 100) {
//                            byte idItem = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 94, 95, 96, 97, 98}[Until.nextInt(55)];
//                            this.updateSpecialItem(idItem, 1);
//                            updateLuong(5);
//                            updateXu(5000);
//                            updateRuong(null, null, -1, null, hopNgocItemArray);
//                            ms = new Message(45);
//                            ds = ms.writer();
//                            ds.writeUTF(String.format(GameString.missionComplete(), SpecialItemData.getSpecialItemById(idItem).name));
//                            ds.flush();
//                            sendMessage(ms);
//                        } else {
//                            int xp = 200 * rdE.numb;
//                            updateXP(xp, false);
//                            this.updateRuong(null, null, -1, null, hopNgocItemArray);
//                            ms = new Message(45);
//                            ds = ms.writer();
//                            ds.writeUTF(String.format(GameString.missionComplete(), Until.getStringNumber(xp) + "xp"));
//                            ds.flush();
//                            sendMessage(ms);
//                        }
//                    }
//                    if (rdE.entry.id == 81) {
//                        if (rdE.numb == 150 || rdE.numb == 200 || rdE.numb == 250 || rdE.numb == 305 || rdE.numb == 400) {
//                            ruongDoTBEntry rdE2 = new ruongDoTBEntry();
//                            for (byte i = 0; i < 5; i++) {
//                                rdE2 = new ruongDoTBEntry();
//                                rdE2.entry = NVData.getEquipEntryById(this.nv, i, GIFT_DATA_BACH_KIM[this.nv][i]);
//                                rdE2.invAdd = new short[5];
//                                rdE2.percenAdd = new short[5];
//                                switch (rdE.numb) {
//                                    case 150:
//                                        rdE2.vipLevel = 1;
//                                        for (byte j = 0; j < 5; j++) {
//                                            rdE2.invAdd[j] = (short) Until.nextInt(40, 50);
//                                            rdE2.percenAdd[j] = (short) Until.nextInt(20, 23);
//                                        }
//                                        break;
//                                    case 200:
//                                        rdE2.vipLevel = 2;
//                                        for (byte j = 0; j < 5; j++) {
//                                            rdE2.invAdd[j] = (short) Until.nextInt(50, 60);
//                                            rdE2.percenAdd[j] = (short) Until.nextInt(23, 26);
//                                        }
//                                        break;
//                                    case 250:
//                                        rdE2.vipLevel = 3;
//                                        for (byte j = 0; j < 5; j++) {
//                                            rdE2.invAdd[j] = (short) Until.nextInt(60, 70);
//                                            rdE2.percenAdd[j] = (short) Until.nextInt(26, 32);
//                                        }
//                                        break;
//                                    case 300:
//                                        rdE2.vipLevel = 4;
//                                        for (byte j = 0; j < 5; j++) {
//                                            rdE2.invAdd[j] = (short) Until.nextInt(60, 70);
//                                            rdE2.percenAdd[j] = (short) Until.nextInt(26, 32);
//                                        }
//                                        break;
//                                    case 400:
//                                        rdE2.vipLevel = 5;
//                                        for (byte j = 0; j < 5; j++) {
//                                            rdE2.invAdd[j] = (short) Until.nextInt(60, 70);
//                                            rdE2.percenAdd[j] = (short) Until.nextInt(26, 32);
//                                        }
//                                        break;
//                                }
//                                updateRuong(null, rdE2, -1, null, null);
//                            }
//                            updateRuong(null, null, -1, null, hopNgocItemArray);
//                            ms = new Message(45);
//                            ds = ms.writer();
//                            ds.writeUTF(String.format(GameString.missionComplete(), "Bộ trang bị Bạch kim " + rdE2.vipLevel));
//                            ds.flush();
//                            sendMessage(ms);
//                        } else {
//                            int xp = rdE.numb * 100;
//                            updateXP(xp, false);
//                            this.updateRuong(null, null, -1, null, hopNgocItemArray);
//                            ms = new Message(45);
//                            ds = ms.writer();
//                            ds.writeUTF(String.format(GameString.missionComplete(), xp + "xp"));
//                            ds.flush();
//                            sendMessage(ms);
//                        }
//                    } else 
                    if (rdE.entry.id == 78) {
                        if (rdE.numb == 150 || rdE.numb == 200 || rdE.numb == 250) {
                            ruongDoTBEntry rdE2 = new ruongDoTBEntry();
                            for (byte i = 0; i < 5; i++) {
                                rdE2 = new ruongDoTBEntry();
                                rdE2.entry = NVData.getEquipEntryById(this.nv, i, GIFT_DATA_HOANG_KIM[this.nv][i]);
                                rdE2.invAdd = new short[5];
                                rdE2.percenAdd = new short[5];
                                switch (rdE.numb) {
                                    case 150:
                                        rdE2.vipLevel = 3;
                                        for (byte j = 0; j < 5; j++) {
                                            rdE2.invAdd[j] = (short) Until.nextInt(70, 79);
                                            rdE2.percenAdd[j] = (short) Until.nextInt(33, 36);
                                        }
                                        break;
                                    case 200:
                                        rdE2.vipLevel = 4;
                                        for (byte j = 0; j < 5; j++) {
                                            rdE2.invAdd[j] = (short) Until.nextInt(80, 89);
                                            rdE2.percenAdd[j] = (short) Until.nextInt(36, 39);
                                        }
                                        break;
                                    case 250:
                                        rdE2.vipLevel = 5;
                                        for (byte j = 0; j < 5; j++) {
                                            rdE2.invAdd[j] = (short) Until.nextInt(90, 99);
                                            rdE2.percenAdd[j] = (short) Until.nextInt(39, 42);
                                        }
                                        break;
                                }
                                updateRuong(null, rdE2, -1, null, null);
                            }
                            updateRuong(null, null, -1, null, hopNgocItemArray);
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(String.format(GameString.missionComplete(), "Bộ trang bị Thời Kì Hoàng Kim " + rdE2.vipLevel));
                            ds.flush();
                            sendMessage(ms);
                        } else {
                            int xp = rdE.numb * 10000;
                            updateXP(xp, false);
                            this.updateRuong(null, null, -1, null, hopNgocItemArray);
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(String.format(GameString.missionComplete(), xp + "xp"));
                            ds.flush();
                            sendMessage(ms);
                        }
                    }
                    if (rdE.entry.id == 89) {
                        if (this.lever[this.nv] < 2000) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Cấp 2000 mới có thể chuyển sinh!");
                            ds.flush();
                            sendMessage(ms);
                        } else if (this.nvCSinh[this.nv] > 64) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Số lần chuyển sinh đã đạt tối đa!");
                            ds.flush();
                            sendMessage(ms);
                        } else {
                            updateRuong(null, null, -1, null, hopNgocItemArray);
                            int xpnew = this.xp[this.nv] - (2000 * 1999 * 500);
                            this.lever[this.nv] = 1;
                            this.xp[this.nv] = 0;
                            if (this.nvCSinh[this.nv] == 0) {
                                this.point[this.nv] += 3;
                            } else {
                                this.point[this.nv] += 1;
                            }
                            this.nvCSinh[this.nv]++;
                            this.csinh++;
                            this.sendInfo();
                            updateXP(xpnew, false);
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Đã chuyển sinh thành công");
                            ds.flush();
                            sendMessage(ms);
                        }
                    } else if (rdE.entry.id == 90) {
                        Date dat = new Date();
                        if (this.xpX0Time.before(dat)) {
                            xpX0Time = dat;
                        }
                        updateRuong(null, null, -1, null, hopNgocItemArray);
                        if (this.xpX0Time.after(new Date())) {
                            xpX0Time = new Date();
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.x0XPHuy());
                            ds.flush();
                            sendMessage(ms);
                        } else {
                            Until.addNumDay(xpX0Time, 1);
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.x0XPSuccess());
                            ds.flush();
                            sendMessage(ms);
                        }
                    } else if (rdE.entry.id == 105) {
                        Date dat = new Date();
                        if (this.xpX6Time.before(dat)) {
                            xpX6Time = dat;
                        }
                        Until.addNumDay(xpX6Time, 1);
                        updateRuong(null, null, -1, null, hopNgocItemArray);
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.x6XPSuccess());
                        ds.flush();
                        sendMessage(ms);
                    } else if (rdE.entry.id > 73 && rdE.entry.id < 77) {
                        boolean hoanthanh = false;
                        for (int i = 0; i < Equip2.size(); i++) {
                            if (Equip2.get(i).nv == this.nv) {
                                Equip2.get(i).type = (byte) rdE.entry.id;
                                Equip2.get(i).time = new Date();
                                Equip2.get(i).time = Until.getDate(Until.addNumHours(new Date(), 24));
                                hoanthanh = true;
                                System.out.println("hoan thanh");
                            }
                        }
                        if (!hoanthanh) {
                            equip2 eq2 = new equip2();
                            eq2.nv = this.nv;
                            eq2.type = (byte) rdE.entry.id;
                            eq2.time = new Date();
                            eq2.time = Until.getDate(Until.addNumHours(new Date(), 24));
                            this.Equip2.add(eq2);
                            System.out.println("tao moi");
                        }
                        updateRuong(null, null, -1, null, hopNgocItemArray);
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF("Sử dụng thành công " + SpecialItemData.getSpecialItemById(rdE.entry.id).name);
                        ds.flush();
                        sendMessage(ms);
                    }
                    break;

                }
                case 7: {
                    //if (action == 7) {
                    if (hopNgocTB != null) {
                        if (hopNgocTB.cap < 20) {
                            ruongDoItemEntry[] rdEArr = new ruongDoItemEntry[hopNgocItemArray.size()];
                            for (int i = 0; i < hopNgocItemArray.size(); i++) {
                                rdEArr[i] = this.hopNgocItemArray.get(i);
                            }
                            rdEArr[0].numb = 1;
                            ms = new Message(45);
                            ds = ms.writer();
                            int[] pt = new int[]{99, 90, 80, 70, 60, 55, 50, 45, 40, 35, 30, 25, 20, 17, 15, 12, 9, 6, 3, 1};
                            if (Until.nextInt(100) < pt[hopNgocTB.cap]) {
                                hopNgocTB.cap++;
                                String typeCS = "tất cả chỉ số";
                                for (int i = 0; i < 5; i++) {
                                    hopNgocTB.invAdd[i] += hopNgocTB.cap * 3;
                                }
                                if (hopNgocTB.cap != 4 && hopNgocTB.cap % 4 == 0) {
                                    switch (hopNgocTB.cap) {
                                        case 8:
                                            hopNgocTB.anAdd[2] = 5;
                                            ds.writeUTF(String.format(GameString.dapDoDatMuc(), hopNgocTB.cap, 5, "% hồi máu", hopNgocTB.cap * 3));
                                            break;

                                        case 12:
                                            hopNgocTB.anAdd[1] = 5;
                                            ds.writeUTF(String.format(GameString.dapDoDatMuc(), hopNgocTB.cap, 5, "% phản sát thương", hopNgocTB.cap * 3));
                                            break;

                                        case 16:
                                            hopNgocTB.anAdd[2] = 10;
                                            ds.writeUTF(String.format(GameString.dapDoDatMuc(), hopNgocTB.cap, 10, "% hồi máu", hopNgocTB.cap * 3));
                                            break;

                                        case 20:
                                            hopNgocTB.anAdd[0] = 10;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.percenAdd[i] += 10;
                                            }
                                            ds.writeUTF(String.format(GameString.dapDoMax(), (hopNgocTB.cap * 3), 10, "% hút máu", 10, "%"));
                                            break;
                                    }
//                                    byte[] ab = SerArmy.getFile("Dap_Do.log");
//                                    if (ab != null) {
//                                        String data = new String(ab, "UTF-8");
//                                        ab = (data + String.format("\r\n- User: %s (id: %d) đã đập đồ lên +%d (%s)!.", username, iddb, hopNgocTB.cap, hopNgocTB.entry.name)).getBytes();
//                                        SerArmy.saveFile("Dap_Do.log", ab);
//                                    }
                                } else {
                                    ds.writeUTF(String.format(GameString.dapDoSuccess(), hopNgocTB.cap, (hopNgocTB.cap * 3), typeCS));
                                }
                            } else {
                                if (!this.baoHiem) {
                                    switch (hopNgocTB.cap) {
                                        case 5:
                                            hopNgocTB.cap = 4;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= 5 * 3;
                                            }
                                            break;

                                        case 6:
                                            hopNgocTB.cap = 4;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (5 + 6) * 3;
                                            }
                                            break;

                                        case 7:
                                            hopNgocTB.cap = 4;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (5 + 6 + 7) * 3;
                                            }
                                            break;

                                        case 9:
                                            hopNgocTB.cap = 8;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= 9 * 3;
                                            }
                                            break;

                                        case 10:
                                            hopNgocTB.cap = 8;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (9 + 10) * 3;
                                            }
                                            break;

                                        case 11:
                                            hopNgocTB.cap = 8;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (9 + 10 + 11) * 3;
                                            }
                                            break;

                                        case 13:
                                            hopNgocTB.cap = 12;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= 13 * 3;
                                            }
                                            break;

                                        case 14:
                                            hopNgocTB.cap = 12;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (13 + 14) * 3;
                                            }
                                            break;

                                        case 15:
                                            hopNgocTB.cap = 12;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (13 + 14 + 15) * 3;
                                            }
                                            break;

                                        case 17:
                                            hopNgocTB.cap = 16;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= 17 * 3;
                                            }
                                            break;

                                        case 18:
                                            hopNgocTB.cap = 16;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (17 + 18) * 3;
                                            }
                                            break;

                                        case 19:
                                            hopNgocTB.cap = 16;
                                            for (int i = 0; i < 5; i++) {
                                                hopNgocTB.invAdd[i] -= (17 + 18 + 19) * 3;
                                            }
                                            break;
                                    }
                                }
                                ds.writeUTF(GameString.dapDoFail());
                            }

                            updateRuong(hopNgocTB, null, -1, null, hopNgocItemArray);
                            this.sendRuongDoInfo();
                            ds.flush();
                            sendMessage(ms);
                        } else {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Trang bị đã đạt cấp tối đa!.");
                            ds.flush();
                            sendMessage(ms);
                        }
                    }
                    //}
                }
                default:
                    break;
            }
            this.hopNgocAction = 0;
        }
    }

    public void sendMSSToUser(User us, String s) {
        try {
            Message ms = new Message(5);
            DataOutputStream ds = ms.writer();
            if (us != null) {
                ds.writeInt(us.iddb);
                ds.writeUTF(us.username);
            } else {
                ds.writeInt(1);
                ds.writeUTF("Admin");
            }
            ds.writeUTF(s);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void selectNVMessage(Message ms) throws IOException {
        byte idnv = ms.reader().readByte();
        if (idnv >= NVData.entrys.size() || idnv < 0 || !this.nvStt[idnv]) {
            return;
        }
        this.nv = idnv;
        ms = new Message(69);
        DataOutputStream ds = ms.writer();
        ds.writeInt(this.iddb);
        ds.writeByte(idnv);
        ds.flush();
        sendMessage(ms);
        sendInfo();
        sendTBInfo();
    }

    protected void buyNVMessage(Message ms) throws IOException {
        byte idnv = ms.reader().readByte();
        idnv += 3;
        if (this.nvStt[idnv]) {
            return;
        }
        NVEntry nventry = NVData.entrys.get(idnv);
        byte buyLuong = ms.reader().readByte();
        boolean buyOK = false;
        if (buyLuong == 1) {
            if (this.luong >= nventry.buyLuong && nventry.buyLuong >= 0) {
                this.updateLuong(-nventry.buyLuong);
                buyOK = true;
            }
        } else {
            if (this.xu >= nventry.buyXu && nventry.buyXu >= 0) {
                this.updateXu(-nventry.buyXu);
                buyOK = true;
            }
        }
        if (buyOK) {
            nvStt[idnv] = true;
            ms = new Message(74);
            DataOutputStream ds = ms.writer();
            ds.writeByte(idnv - 3);
            ds.flush();
            sendMessage(ms);
        } else {
            ms = new Message(45);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(GameString.xuNotEnought());
            ds.flush();
            sendMessage(ms);
        }
    }

    protected void changePassMessage(Message ms) throws IOException {
        String oldpass = ms.reader().readUTF().replaceAll(" ", "").trim();
        String newpass = ms.reader().readUTF().replaceAll(" ", "").trim();

        DataOutputStream ds;
        Pattern p = Pattern.compile("^[a-zA-Z0-9]+$");
        Matcher m1 = p.matcher(oldpass);
        Matcher m2 = p.matcher(newpass);
        if (!m1.find() || !m2.find()) {
            ms = new Message(45);
            ds = ms.writer();
            ds.writeUTF(GameString.changPassError1());
            ds.flush();
            sendMessage(ms);
            return;

        }
        try {
            ResultSet red = SQLManager.getStatement().executeQuery("SELECT `user` FROM `user` WHERE `user_id`='" + this.iddb + "' AND `password`='" + oldpass + "' LIMIT 1;");
            if (red == null || !red.first()) {
                ms = new Message(45);
                ds = ms.writer();
                ds.writeUTF(GameString.changPassError2());
                ds.flush();
                sendMessage(ms);
                red.close();
                return;
            }
            red.close();
            SQLManager.getStatement().executeUpdate("UPDATE `user` SET `password`='" + newpass + "' WHERE `user_id`=" + this.iddb + " LIMIT 1;");
            ms = new Message(45);
            ds = ms.writer();
            ds.writeUTF(GameString.changPassSuccess());
            ds.flush();
            sendMessage(ms);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void sendPointAddInfo() throws IOException {
        Message ms = new Message(99);
        DataOutputStream ds = ms.writer();
        int lvS = this.lever[this.nv];
        ds.writeByte("2.2.3".equals(client.version) ? (lvS > 127 ? 127 : lvS) : (lvS > 255 ? 255 : lvS));
        ds.writeByte(this.leverPercen[this.nv]);
        ds.writeShort(this.point[this.nv]);
        for (int i = 0; i < 5; i++) {
            ds.writeShort(this.pointAdd[nv][i]);
        }
        ds.writeInt(this.xp[this.nv]);
        ds.writeInt(this.lever[this.nv] * (this.lever[this.nv] + 1) * 500);
        ds.writeInt(this.dvong);
        ds.flush();
        sendMessage(ms);
    }

    protected void nangcapOkMessage(Message ms) throws IOException {
        short[] pointA = new short[5];
        short pointTong = 0;
        for (int i = 0; i < 5; i++) {
            pointA[i] = ms.reader().readShort();
            if (pointA[i] < 0) {
                return;
            }
            pointTong += pointA[i];
        }
        if (pointTong <= this.point[this.nv]) {
            for (int i = 0; i < 5; i++) {
                this.pointAdd[this.nv][i] += pointA[i];
            }
            this.point[this.nv] -= pointTong;
        }
        sendPointAddInfo();
    }

    protected void quaysoMessage(Message ms) throws IOException {
        byte typeQ = ms.reader().readByte();
        DataOutputStream ds;
        switch (typeQ) {
            case 0:
                if (this.xu < 1000) {
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(GameString.xuNotEnought());
                    ds.flush();
                    sendMessage(ms);
                    return;
                }
                this.updateXu(-1000);
                break;
            case 1:
                if (this.luong < 1) {
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(GameString.xuNotEnought());
                    ds.flush();
                    sendMessage(ms);
                    return;
                }
                this.updateLuong(-1);
                break;
            default:
                return;
        }
        ms = new Message(110);
        ds = ms.writer();
        int lucKyNum = Until.nextInt(10);
        for (int i = 0; i < 10; i++) {
            int type = Until.nextInt(new int[]{300, 150, 450, 100});
            byte idItem = 0;
            int numb = 0;
            if (type == 0) {
                idItem = (byte) Until.nextInt(ItemData.entrys.size());
                numb = (new int[]{1, 5, 10, 15})[Until.nextInt(new int[]{400, 300, 200, 100})];
                if (i == lucKyNum) {
                    this.updateItem(idItem, numb);
                }
            }
            if (type == 1) {
                numb = (new int[]{500, 1000, 5000, 10000})[Until.nextInt(new int[]{400, 300, 200, 100})];
                if (i == lucKyNum) {
                    this.updateXu(numb);
                }
            }
            if (type == 2) {
                numb = (new int[]{1, 50, 100, 500})[Until.nextInt(new int[]{400, 300, 200, 100})];
                if (i == lucKyNum) {
                    this.updateXP(numb, true);
                }
            }
            ds.writeByte(type);
            ds.writeByte(idItem);
            ds.writeInt(numb);
        }
        ds.writeByte(lucKyNum);
        ds.flush();
        sendMessage(ms);
    }

    protected void buyItemMessage(Message ms) throws IOException {
        // don vi mua 0: xu, 1: luong
        byte donvi = ms.reader().readByte();
        // id item
        byte iditem = ms.reader().readByte();
        // So luong mua
        byte soluong = ms.reader().readByte();
        if (iditem < 0 || iditem >= ItemData.entrys.size()) {
            return;
        }
        // Mua main
        if (this.item[iditem] + soluong > ServerManager.max_item) {
            return;
        }
        int moneyitem;
        switch (donvi) {
            case 0:
                moneyitem = soluong * (ItemData.entrys.get(iditem)).buyXu;
                if ((this.xu < moneyitem) || (moneyitem < 0)) {
                    return;
                }
                this.updateXu(-moneyitem);
                this.updateItem(iditem, soluong);
                break;
            case 1:
                moneyitem = soluong * (ItemData.entrys.get(iditem)).buyLuong;
                if ((this.luong < moneyitem) || (moneyitem < 0)) {
                    return;
                }
                this.updateLuong(-moneyitem);
                this.updateItem(iditem, soluong);
                break;
            default:
                return;
        }
        ms = new Message(72);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeByte(iditem);
        ds.writeByte(this.item[iditem]);
        ds.writeInt(this.xu);
        ds.writeInt(this.luong);
        ds.flush();
        sendMessage(ms);
    }

    protected void buyEquipMessage(Message ms) throws IOException {
        byte type = ms.reader().readByte();
        DataOutputStream ds;
        // Mua trang bi shop
        if (type == 0) {
            if (this.ruongDoTB.size() == ServerManager.max_ruong_tb) {
                ms = new Message(45);
                ds = ms.writer();
                ds.writeUTF(GameString.ruongNoSlot());
                ds.flush();
                sendMessage(ms);
                return;
            }
            short indexSale = ms.reader().readShort();
            byte buyLuong = ms.reader().readByte();
            EquipmentEntry eqEntry = NVData.getEquipEntryByIndexSale(indexSale);
            if (!eqEntry.onSale || (buyLuong == 0 ? eqEntry.giaXu : eqEntry.giaLuong) < 0) {
                return;
            }
            // Mua bang xu
            switch (buyLuong) {
                case 0:
                    if (this.xu < eqEntry.giaXu) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.xuNotEnought());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    this.updateXu(-eqEntry.giaXu);
                    break;
                case 1:
                    if (this.luong < eqEntry.giaLuong) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.xuNotEnought());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    updateLuong(-eqEntry.giaLuong);
                    break;
                default:
                    return;
            }
            ruongDoTBEntry rdE = new ruongDoTBEntry();
            rdE.entry = eqEntry;
            this.updateRuong(null, rdE, -1, null, null);
            ms = new Message(45);
            ds = ms.writer();
            ds.writeUTF(GameString.buySuccess());
            ds.flush();
            sendMessage(ms);
            return;
        }
        // Ban trang bi
        if (type == 1) {
            this.hopNgocAction = 0;
            this.hopNgocGia = 0;
            this.hopNgocNum = 0;
            byte lent = ms.reader().readByte();
            for (int i = 0; i < lent; i++) {
                int id = ms.reader().readInt();
                if ((id & 0x10000) > 0) {
                    id &= 0xFFFF;
                    if (id >= 0 && id < this.ruongDoTB.size()) {
                        this.hopNgocNum++;
                        ruongDoTBEntry rdE = this.ruongDoTB.get(id);
                        int hanSD = rdE.entry.hanSD - Until.getNumDay(rdE.dayBuy, new Date());
                        if (hanSD < 0) {
                            hanSD = 0;
                        }
                        hopNgocTB = rdE;
                        if (rdE.entry.giaXu > 0) {
                            this.hopNgocGia += rdE.entry.giaXu / 2 * hanSD / rdE.entry.hanSD;
                        } else if (rdE.entry.giaLuong > 0) {
                            this.hopNgocGia += rdE.entry.giaLuong * 500 * hanSD / rdE.entry.hanSD;
                        }
                        break;
                    }
                }
            }
            if (hopNgocTB != null && hopNgocTB.slotNull < 3) {
                this.hopNgocAction = 4;
                ms = new Message(104);
                ds = ms.writer();
                ds.writeByte(1);
                ds.writeUTF(String.format(GameString.thaoNgocRequest(), hopNgocGia));
                ds.flush();
                sendMessage(ms);
                return;
            }
            if (hopNgocTB != null) {
                this.hopNgocAction = 1;
                ms = new Message(104);
                ds = ms.writer();
                ds.writeByte(1);
                ds.writeUTF(String.format(GameString.sellTBRequest(), hopNgocGia));
                ds.flush();
                sendMessage(ms);
                return;
            }
        }
        // Ban trang bi confirm
        if (type == 2) {
            if (this.hopNgocAction == 1) {
                if (hopNgocTB != null) {
                    if (hopNgocTB.isUse) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.sellTBError1());
                        ds.flush();
                        sendMessage(ms);
                    } else {
                        for (byte i = 0; i < 10; i++) {
                            for (byte j = 0; j < 6; j++) {
                                if (this.NvData[i][j] > hopNgocTB.index) {
                                    this.NvData[i][j] -= 1;
                                }
                            }
                        }
                        updateRuong(null, null, hopNgocTB.index, null, null);
                        updateXu(hopNgocGia);
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.buySuccess());
                        ds.flush();
                        sendMessage(ms);
                    }
                }
            } else if (this.hopNgocAction == 4) {
                if (hopNgocTB != null) {
                    if (hopNgocTB.isUse) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.thaoNgocError1());
                        ds.flush();
                        sendMessage(ms);
                    } else {
                        hopNgocGia = 0;
                        hopNgocItemArray.clear();
                        for (int i = 0; i < 3; i++) {
                            SpecialItemEntry entry = SpecialItemData.getSpecialItemById(hopNgocTB.slot[i]);
                            if (hopNgocTB.slot[i] > -1) {
                                hopNgocGia += entry.buyXu;
                            }
                        }
                        hopNgocGia = hopNgocGia / 4;
                        if (this.xu < hopNgocGia) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(String.format(GameString.thaoNgocError2(), hopNgocGia));
                            ds.flush();
                            sendMessage(ms);
                        } else {
                            hopNgocTB.slotNull = 3;
                            this.updateXu(-hopNgocGia);
                            for (int i = 0; i < 3; i++) {
                                ruongDoItemEntry rdE = new ruongDoItemEntry();
                                rdE.entry = SpecialItemData.getSpecialItemById(hopNgocTB.slot[i]);
                                if (hopNgocTB.slot[i] > -1) {
                                    rdE.numb = 1;
                                    hopNgocTB.slot[i] = -1;
                                    for (int j = 0; j < 5; j++) {
                                        hopNgocTB.invAdd[j] -= rdE.entry.ability[j];
                                    }

                                    hopNgocItemArray.add(rdE);
                                    hopNgocGia += rdE.entry.buyXu;
                                }
                            }
                            updateRuong(hopNgocTB, null, -1, hopNgocItemArray, null);
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.thaoNgocSuccess());
                            ds.flush();
                            sendMessage(ms);
                        }
                    }
                }
            }
            this.hopNgocAction = 0;
            ms = new Message(104);
            ds = ms.writer();
            ds.writeByte(2);
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 5; j++) {
                    if (nvEquip[i][j] != null) {
                        ds.writeShort(nvEquip[i][j].entry.id);
                    } else if (nvEquipDefault[i][j] != null) {
                        ds.writeShort(nvEquipDefault[i][j].id);
                    } else {
                        ds.writeShort(-1);
                    }
                }
            }
            ds.flush();
            sendMessage(ms);
        }
    }

    protected void specialShopMessage(Message ms) throws IOException {
        byte type = ms.reader().readByte();
        DataOutputStream ds;
        // Mo special item shop
        if (type == 0) {
            ms = new Message(-3);
            ds = ms.writer();
            for (SpecialItemEntry spEntry : SpecialItemData.entrys) {
                if (!spEntry.onSale) {
                    continue;
                }
                ds.writeByte(spEntry.id);
                ds.writeUTF(spEntry.name);
                ds.writeUTF(spEntry.detail);
                ds.writeInt(spEntry.buyXu);
                ds.writeInt(spEntry.buyLuong);
                ds.writeByte(spEntry.hanSD);
                ds.writeByte(spEntry.showChon ? 0 : 1);
            }
            ds.flush();
            sendMessage(ms);
        }
        // Buy item
        if (type == 1) {
            byte buyLuong = ms.reader().readByte();
            byte idS = ms.reader().readByte();
            int numb = ms.reader().readUnsignedByte();
            if (getNumItemRuong(idS) == 0 && this.ruongDoItem.size() > ServerManager.max_ruong_item) {
                ms = new Message(45);
                ds = ms.writer();
                ds.writeUTF(GameString.ruongNoSlot());
                ds.flush();
                sendMessage(ms);
                return;
            }
            if (getNumItemRuong(idS) + numb > ServerManager.max_ruong_itemslot) {
                ms = new Message(45);
                ds = ms.writer();
                ds.writeUTF(GameString.ruongMaxSlot());
                ds.flush();
                sendMessage(ms);
                return;
            }
            SpecialItemEntry spE = SpecialItemData.getSpecialItemById(idS);
            if (numb < 1 || !spE.onSale || (buyLuong == 0 ? spE.buyXu : spE.buyLuong) < 0) {
                return;
            }
            if (spE.id == 88) {
                for (byte nnv = 0; nnv < 10; nnv++) {
                    for (byte i = 0; i < 6; i++) {
                        this.NvData[nnv][i] = -1;
                        this.nvEquip[nnv][i] = null;
                    }
                }
                for (short i = 0; i < ruongDoTB.size(); i++) {
                    this.ruongDoTB.get(i).isUse = false;
                }
                ms = new Message(45);
                ds = ms.writer();
                ds.writeUTF("Đã tháo tất cả trang bị đang mặc !");
                ds.flush();
                sendMessage(ms);
                return;
            }
            if (spE.id == 110) {
                for (short i = 0; i < ruongDoTB.size(); i++) {
                    this.ruongDoTB.get(i).isUse = false;
                }
                switch (buyLuong) {
                    case 0: {//xu ra luong
                        int gia = ServerManager.XU_BI_TRU;
                        int traodoi = ServerManager.LUONG_NHAN_DUOC;
                        if (this.xu < gia) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Không đủ xu để trao đổi");
                            ds.flush();
                            sendMessage(ms);
                            return;
                        }
                        updateXu(-gia);
                        updateLuong(traodoi);
                        break;
                    }
                    case 1: {//luong ra xu
                        int gia = ServerManager.LUONG_BI_TRU;
                        int traodoi = ServerManager.XU_NHAN_DUOC;
                        if (this.luong < gia) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF("Không đủ lượng để trao đổi");
                            ds.flush();
                            sendMessage(ms);
                            return;
                        }
                        updateLuong(-gia);
                        updateXu(traodoi);
                        break;
                    }
                    default:
                        return;
                }
            }
            // Mua bang xu
            switch (buyLuong) {
                case 0: {
                    int gia = numb * spE.buyXu;
                    if (this.xu < gia) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.xuNotEnought());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    updateXu(-gia);
                    break;
                }
                case 1: {
                    int gia = numb * spE.buyLuong;
                    if (this.luong < gia) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.xuNotEnought());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    updateLuong(-gia);
                    break;
                }
                default:
                    return;
            }
            ArrayList<ruongDoItemEntry> arrayI = new ArrayList<>();
            ruongDoItemEntry rdE = new ruongDoItemEntry();
            rdE.entry = spE;
            rdE.numb = numb;
            arrayI.add(rdE);
            updateRuong(null, null, -1, arrayI, null);
            ms = new Message(45);
            ds = ms.writer();
            ds.writeUTF(GameString.buySuccess());
            ds.flush();
            sendMessage(ms);
        }
    }

    protected void giaHanMessage(Message ms) throws IOException {
        byte action = ms.reader().readByte();
        int idKey = ms.reader().readInt();
        DataOutputStream ds;
        if (action == 0) {
            int gia = 0;
            if ((idKey & 0x10000) > 0) {
                idKey &= 0xFFFF;
                if (idKey >= 0 && idKey < this.ruongDoTB.size()) {
                    ruongDoTBEntry rdE = this.ruongDoTB.get(idKey);
                    for (int i = 0; i < 3; i++) {
                        if (rdE.slot[i] >= 0) {
                            SpecialItemEntry spE = SpecialItemData.getSpecialItemById(rdE.slot[i]);
                            gia += spE.buyXu;
                        }
                    }
                    gia = gia / 20;
                    if (rdE.entry.giaXu > 0) {
                        gia += rdE.entry.giaXu;
                    } else if (rdE.entry.giaLuong > 0) {
                        gia += rdE.entry.giaLuong * 1000;
                    }
                    ms = new Message(-25);
                    ds = ms.writer();
                    ds.writeInt(rdE.index | 0x10000);
                    ds.writeUTF(String.format(GameString.giaHanRequest(), gia));
                    ds.flush();
                    sendMessage(ms);
                }
            }
        }
        if (action == 1) {
            int gia = 0;
            if ((idKey & 0x10000) > 0) {
                idKey &= 0xFFFF;
                if (idKey >= 0 && idKey < this.ruongDoTB.size()) {
                    ruongDoTBEntry rdE = this.ruongDoTB.get(idKey);
                    for (int i = 0; i < 3; i++) {
                        if (rdE.slot[i] >= 0) {
                            SpecialItemEntry spE = SpecialItemData.getSpecialItemById(rdE.slot[i]);
                            gia += spE.buyXu;
                        }
                    }
                    gia = gia / 20;
                    if (rdE.entry.giaXu > 0) {
                        gia += rdE.entry.giaXu;
                    } else if (rdE.entry.giaLuong > 0) {
                        gia += rdE.entry.giaLuong * 1000;
                    }
                    if (this.xu < gia) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.xuNotEnought());
                        ds.flush();
                        sendMessage(ms);
                        return;
                    }
                    updateXu(-gia);
                    rdE.dayBuy = new Date();
                    this.updateRuong(rdE, null, -1, null, null);
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(GameString.giaHanSucess());
                    ds.flush();
                    sendMessage(ms);
                }
            }
        }
    }

    protected void setSetMessage(Message ms) throws IOException {
        byte action = ms.reader().readByte();
        int dbKey = ms.reader().readInt();
        if ((dbKey & 0x10000) == 0) {
            return;
        }
        dbKey = dbKey & 0xFFFF;
        if (dbKey < 0 || dbKey >= this.ruongDoTB.size()) {
            return;
        }
        ruongDoTBEntry rdE = this.ruongDoTB.get(dbKey);
        if (!rdE.entry.isSet || rdE.isUse) {
            return;
        }
        if (rdE.entry.lvRequire > this.getLevel()) {
            return;
        }
        DataOutputStream ds;
        ms = new Message(-2);
        ds = ms.writer();
        if (this.NvData[this.nv][5] >= 0) {
            ruongDoTBEntry rdE2 = this.ruongDoTB.get(this.NvData[this.nv][5]);
            rdE2.isUse = false;
        }
        if (action == 0) {
            ds.writeByte(0);
            this.NvData[this.nv][5] = -1;
            this.nvEquip[this.nv][5] = null;
        } else {
            rdE.isUse = true;
            this.nvEquip[this.nv][5] = rdE;
            this.NvData[this.nv][5] = rdE.index;
            ds.writeByte(1);
            ds.writeShort(rdE.entry.arraySet[0]);
            ds.writeShort(rdE.entry.arraySet[1]);
            ds.writeShort(rdE.entry.arraySet[2]);
            ds.writeShort(rdE.entry.arraySet[3]);
            ds.writeShort(rdE.entry.arraySet[4]);
        }
        ds.flush();
        sendMessage(ms);
    }

    protected void setEquipMessage(Message ms) {
        try {
            int[] dbKey = new int[5];
            for (int i = 0; i < 5; i++) {
                dbKey[i] = ms.reader().readInt();
            }
            short[] equip = new short[5];
            for (int i = 0; i < 5; i++) {
                if ((dbKey[i] & 0x10000) == 0) {
                    continue;
                }
                dbKey[i] = dbKey[i] & 0xFFFF;
                if (dbKey[i] < 0 || dbKey[i] >= this.ruongDoTB.size()) {
                    continue;
                }
                ruongDoTBEntry rdE = this.ruongDoTB.get(dbKey[i]);
                if (rdE.entry.isSet || rdE.isUse) {
                    continue;
                }
                if (rdE.entry.lvRequire > this.getLevel()) {
                    continue;
                }
                if (rdE.entry.idNV == this.nv && rdE.entry.idEquipDat == i) {
                    if (this.NvData[this.nv][i] >= 0) {
                        ruongDoTBEntry rdE2 = this.ruongDoTB.get(this.NvData[this.nv][i]);
                        rdE2.isUse = false;
                    }
                    rdE.isUse = true;
                    this.nvEquip[this.nv][i] = rdE;
                    this.NvData[this.nv][i] = rdE.index;
                }
            }
            ms = new Message(102);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.flush();
            sendMessage(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void viewFriendsMessage() throws IOException {
        try {
            Message ms = new Message(29);
            DataOutputStream ds = ms.writer();
            // Get user detals
            JSONArray jarr = this.friends;
            if (jarr != null) {
                for (int i = jarr.size() - 1; i >= 0; i--) {
                    int iddbFR = ((Long) jarr.get(i)).intValue(), nv = 1;
                    ResultSet red = SQLManager.getStatement().executeQuery("SELECT `user` FROM `user` WHERE user_id=\"" + iddbFR + "\" LIMIT 1;");
                    if (!red.first()) {
                        red.close();
                        continue;
                    }
                    String nameFR = red.getString("user");
                    red.close();
                    red = SQLManager.getStatement().executeQuery("SELECT `xu`,`NVused`,`clan`,`online` FROM `armymem` WHERE id=\"" + iddbFR + "\" LIMIT 1;");
                    if (!red.first()) {
                        red.close();
                        continue;
                    }
                    ds.writeInt(iddbFR);
                    ds.writeUTF(nameFR);
                    ds.writeInt(red.getInt("xu"));
                    ds.writeByte((nv = red.getByte("NVUsed")) - 1);
                    ds.writeShort(red.getShort("clan"));
                    ds.writeByte(red.getByte("online"));
                    red.close();
                    red = SQLManager.getStatement().executeQuery("SELECT `NV" + nv + "` FROM `armymem` WHERE id=\"" + iddbFR + "\" LIMIT 1;");
                    red.first();
                    JSONObject jobj = (JSONObject) JSONValue.parse(red.getString("NV" + nv));
                    red.close();
                    /* lever */
                    int lvS = ((Long) jobj.get("lever")).intValue();
                    ds.writeByte("2.2.3".equals(client.version) ? (lvS > 127 ? 127 : lvS) : (lvS > 255 ? 255 : lvS));

                    /* lever % */
                    int xp = ((Long) jobj.get("xp")).intValue();
                    // lever %
                    xp -= (lvS) * (lvS - 1) * 500;
                    ds.writeByte((byte) (xp / lvS / 10));
                    /* data nhan vat */
                    short[] data = ServerManager.data(iddbFR, (byte) nv);
                    for (byte j = 0; j < 5; j++) {
                        ds.writeShort(data[j]);
                    }
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (Exception e) {
            e.printStackTrace();
            Message ms = new Message(29);
            DataOutputStream ds = ms.writer();
            ds.flush();
            sendMessage(ms);
        }
    }

    protected void findUserMessage(Message ms) throws IOException {
        String user = ms.reader().readUTF().trim();
        Pattern p = Pattern.compile("^[a-zA-Z0-9]+$");
        if (!p.matcher(user).find()) {
            ms = new Message(4);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(GameString.addFrienvError1());
            ds.flush();
            this.sendMessage(ms);
            return;
        }
        ms = new Message(36);
        DataOutputStream ds = ms.writer();
        try {
            ResultSet red = SQLManager.getStatement().executeQuery("SELECT `user_id`,`user` FROM `user` WHERE user=\"" + user + "\" LIMIT 1;");
            if (red.first()) {
                ds.writeInt(red.getInt("user_id"));
                ds.writeUTF(red.getString("user"));
            }
            red.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ds.flush();
        sendMessage(ms);
    }

    protected void addFriendsMessage(Message ms) throws IOException {
        int ids = ms.reader().readInt();
        try {
            ms = new Message(32);
            DataOutputStream ds = ms.writer();
            // Get user detals
            JSONArray jarr = this.friends;
            int jSize = 0;
            if (jarr != null) {
                jSize = jarr.size();
            }
            if (jSize > ServerManager.max_friends) {
                ds.writeByte(2);
            } else {
                boolean found = false;
                if (jSize > 0) {
                    for (int i = 0; i < jSize; i++) {
                        int iddb1 = ((Long) jarr.get(i)).intValue();
                        if (iddb1 == ids) {
                            found = true;
                            break;
                        }
                    }
                } else {
                    jarr.add(ids);
                }
                if (found) {
                    ds.writeByte(1);
                } else {
                    if (jSize > 0) {
                        jarr.add(ids);
                    }
                    ds.writeInt(0);
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (Exception e) {
            // Loi them
            ms = new Message(32);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.flush();
            sendMessage(ms);
            e.printStackTrace();
        }
    }

    protected void deleteFriendsMessage(Message ms) throws IOException {
        int iddb = ms.reader().readInt();
        try {
            ms = new Message(33);
            DataOutputStream ds = ms.writer();
            // Get user detals
            JSONArray jarr = this.friends;
            int index = -1;
            for (int i = 0; i < jarr.size(); i++) {
                int iddb1 = ((Long) jarr.get(i)).intValue();
                if (iddb1 == iddb) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                jarr.remove(index);
            }
            ds.writeInt(0);
            ds.flush();
            sendMessage(ms);
        } catch (Exception e) {
            // Loi them
            ms = new Message(33);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.flush();
            sendMessage(ms);
            e.printStackTrace();
        }
    }

    protected void missionMessage(Message ms) throws IOException {
        byte action = ms.reader().readByte();
        byte indexNV = -1;
        if (action == 1) {
            indexNV = ms.reader().readByte();
        }
        DataOutputStream ds;
        if (action == 0) {
            sendMissionInfo();
        }
        if (action == 1) {
            ms = new Message(10);
            ds = ms.writer();
            MissionEntry me = MissionData.getMissionData(indexNV);
            MissDataEntry mDatE = me.mDatE;
            byte id = (byte) (mDatE.id - 1);
            if (id < 0 || id >= this.mission.length) {
                ds.writeUTF(GameString.missionError1());
            } else {
                if (this.missionLevel[id] > me.level) {
                    ds.writeUTF(GameString.missionError2());
                } else if (this.missionLevel[id] < me.level) {
                    ds.writeUTF(GameString.missionError3());
                } else if (this.mission[mDatE.idNeed - 1] < me.require) {
                    ds.writeUTF(GameString.missionError2());
                } else {
                    this.missionLevel[id]++;
                    if (me.rewardXu > 0) {
                        this.updateXu(me.rewardXu);
                    }
                    if (me.rewardLuong > 0) {
                        this.updateLuong(me.rewardLuong);
                    }
                    if (me.rewardXP > 0) {
                        this.updateXP(me.rewardXP, false);
                    }
                    if (me.rewardCUP > 0) {
                        this.updateDvong(me.rewardCUP);
                    }
                    sendMissionInfo();
                    ds.writeUTF(String.format(GameString.missionComplete(), me.reward));
                }
            }
            ds.flush();
            sendMessage(ms);
        }
    }

    protected void viewTTMessage(Message ms) throws IOException {
        int ids = ms.reader().readInt();
        try {
            ms = new Message(34);
            DataOutputStream ds = ms.writer();
            ds.writeInt(ids);
            ResultSet red = SQLManager.getStatement().executeQuery("SELECT `user` FROM `user` WHERE user_id=\"" + ids + "\" LIMIT 1;");
            red.first();
            ds.writeUTF(red.getString("user"));
            red.close();
            red = SQLManager.getStatement().executeQuery("SELECT `xu`,`luong`,`NVused`,`dvong`,`top` FROM `armymem` WHERE id=\"" + ids + "\" LIMIT 1;");
            red.first();
            ds.writeInt(red.getInt("xu"));
            byte nvS = red.getByte("NVUsed");
            int luongS = red.getInt("luong");
            int dvongS = red.getInt("dvong");
            int top = red.getInt("top");
            red.close();
            red = SQLManager.getStatement().executeQuery("SELECT `NV" + nvS + "` FROM `armymem` WHERE id=\"" + ids + "\" LIMIT 1;");
            red.first();
            JSONObject jobj = (JSONObject) JSONValue.parse(red.getString("NV" + nvS));
            red.close();
            /* lever */
            int lvS = ((Long) jobj.get("lever")).intValue();
            ds.writeByte("2.2.3".equals(client.version) ? (lvS > 127 ? 127 : lvS) : lvS);
            /* lever % */
            int xpS = ((Long) jobj.get("xp")).intValue();
            // lever %
            xpS -= (lvS) * (lvS - 1) * 500;
            ds.writeByte((byte) (xpS / lvS / 10));
            // Luong
            ds.writeInt(luongS);
            // XP
            ds.writeInt(xpS);
            // XP Level
            ds.writeInt(lvS * 1000);
            // Danh vong
            ds.writeInt(dvongS);
            // Top ?+
            if (top > 0) {
                ds.writeUTF("Top " + (top < 10000 ? top : ((top / 1000) + "k+")));
            } else {
                ds.writeUTF("Chưa có hạng");
            }
            ds.flush();
            sendMessage(ms);

        } catch (Exception e) {
            ms = new Message(34);
            DataOutputStream ds = ms.writer();
            ds.writeInt(-1);
            ds.flush();
            sendMessage(ms);
            e.printStackTrace();
        }
    }

    public void notifyNetWaitMessage() throws IOException {
        synchronized (this.client.obj) {
            this.client.obj.notifyAll();
        }
    }

    public synchronized void updateSpecialItem(int id, int numb) {
        try {
            if (getNumItemRuong(id) + numb > ServerManager.max_ruong_itemslot && getNumItemRuong(id) == 0 && this.ruongDoItem.size() >= ServerManager.max_ruong_item) {
                return;
            }
            SpecialItemEntry spE = SpecialItemData.getSpecialItemById(id);
            ArrayList<ruongDoItemEntry> arrayI = new ArrayList<>();
            ruongDoItemEntry rdE = new ruongDoItemEntry();
            rdE.entry = spE;
            rdE.numb = numb;
            arrayI.add(rdE);
            updateRuong(null, null, -1, arrayI, null);
        } catch (IOException e) {

        }
    }

    public void updateQua_Start(int addnQua, int time, boolean start) throws IOException {
        this.moQua += addnQua;
        if (this.moQua > 8) {
            this.moQua = 8;
        }
        if (start && !this.startQua) {
            this.startQua = true;
            this.timeQua = time;
            Message ms = new Message(-17);
            DataOutputStream ds = ms.writer();
            ds.writeByte(-1);
            ds.writeByte(time);
            ds.writeUTF("Bạn có " + this.moQua + " lượt chọn quà miễn phí. Cần 2.000 xu để mở thêm các hộp quà khác!");
            ds.flush();
            this.sendMessage(ms);
            this.Gift_finish = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            byte n_open = 0;
                            for (byte i = 0; i < 12; i++) {
                                if (dataQua[i]) {
                                    n_open++;
                                }
                            }
                            if (timeQua <= 0 || !startQua || n_open > 4) {
                                byte id = 0;
                                String name;
                                Message ms = new Message(-17);
                                DataOutputStream ds = ms.writer();
                                ds.writeByte(-2);
                                for (byte index = 0; index < 12; index++) {
                                    name = "";
                                    if (dataQua[index]) {
                                        ds.writeByte(-1);
                                        continue;
                                    }
                                    byte type = 2;
                                    switch (Until.nextInt(5)) {
                                        case 0:
                                            id = 55;
                                            type = 2;
                                            int[] nextXu = new int[]{1000, 2000, 5000, 10000, 12000, 15000, 20000, 25000, 30000, 40000, 50000, 70000, 100000};
                                            int xuUp = nextXu[Until.nextInt(nextXu.length)];
                                            name = Until.getStringNumber(xuUp) + " xu";
                                            break;

                                        case 1:
                                            id = 56;
                                            type = 2;
                                            int[] nextXP = new int[]{1000, 10000, 15000, 20000, 25000};
                                            int xpUp = nextXP[Until.nextInt(nextXP.length)];
                                            name = Until.getStringNumber(xpUp) + " xp";
                                            break;

                                        case 2:
                                            type = 2;
                                            id = (byte) Until.nextInt(50);
                                            break;

                                        case 3:
                                            type = 3;
                                            id = (byte) Until.nextInt(2, ItemData.entrys.size() - 1);
                                            byte numb = (byte) Until.nextInt(1, 5);
                                            name = numb + "x";
                                            break;

                                        case 4:
                                            type = 2;
                                            byte[] aritem = new byte[]{50, 51, 54};
                                            id = aritem[Until.nextInt(aritem.length)];
                                            break;
                                    }
                                    ds.writeByte(type);
                                    ds.writeByte(id);
                                    ds.writeUTF(name);
                                }
                                ds.flush();
                                sendMessage(ms);
                                dataQua = new boolean[12];
                                startQua = false;
                                moQua = 0;
                                break;
                            }
                            timeQua--;
                            Thread.sleep(1000L);
                        }
                    } catch (InterruptedException | IOException e) {
                    }
                }
            });
            this.Gift_finish.start();
        }
    }

    protected void giftAfterFight(Message ms) throws IOException {
        DataOutputStream ds;
        byte index = ms.reader().readByte();
        //hoan thanh
        if (index == -2) {
            this.startQua = false;
        } else if (index > -1 && !this.dataQua[index]) {
            if (this.moQua > 0) {
                this.moQua--;
            } else if (this.getXu() >= 2000) {
                this.updateXu(-2000);
            } else {
                this.startQua = false;
                return;
            }
            byte id = 0;
            String name = "";
            byte type = 2;
            switch (Until.nextInt(8)) {

                case 0:
                    id = 55;
                    type = 2;
                    int[] nextXu = new int[]{1000, 2000, 5000, 10000, 12000, 15000, 20000, 25000, 30000, 40000};
                    int xuUp = nextXu[Until.nextInt(nextXu.length)];
                    this.updateXu(xuUp);
                    name = Until.getStringNumber(xuUp) + " xu";
                    break;

                case 1:
                case 2:
                    id = 56;
                    type = 2;
                    int[] nextXP = new int[]{1000, 10000, 15000, 20000, 25000};
                    int xpUp = nextXP[Until.nextInt(nextXP.length)];
                    this.updateXP(xpUp, false);
                    name = Until.getStringNumber(xpUp) + " xp";
                    break;

                case 3:
                    type = 2;
                    byte[] nextItem = new byte[]{0, 10, 20, 30, 40};
                    byte idItem = (byte) (Until.nextInt(5) == 4 ? Until.nextInt(6, 9) : Until.nextInt(6));
                    id = (byte) (idItem + nextItem[Until.nextInt(nextItem.length)]);
                    this.updateSpecialItem(id, 1);
                    name = "+1";
                    break;

                case 4:
                case 5:
                case 6:
                    type = 3;
                    id = (byte) Until.nextInt(2, ItemData.entrys.size() - 1);
                    byte numb = (byte) Until.nextInt(1, 5);
                    name = numb + "x";
                    this.updateItem(id, (short) numb);
                    break;
                case 7:
                    type = 2;
                    byte[] aritem = new byte[]{50, 51, 54};
                    id = aritem[Until.nextInt(aritem.length)];
                    this.updateSpecialItem(id, 1);
                    name = "+1";
                    break;
            }
            this.dataQua[index] = true;
            ms = new Message(-17);
            ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(index);
            ds.writeByte(type);
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.flush();
            this.sendMessage(ms);
        }
    }

    public boolean getisNHTGAMEItem2(int type) {
        for (int i = 0; i < Equip2.size(); i++) {
            if (Equip2.get(i).nv == this.nv && Equip2.get(i).type == type && Equip2.get(i).time.after(new Date())) {
                return true;
            }
        }
        return false;
    }
}
