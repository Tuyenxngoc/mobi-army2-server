package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.impl.ClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.ItemClanData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.util.Until;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanManager {

    @Getter
    @Setter
    public static class ClanInfo {
        private short id;
        private String name;
        private byte memberCount;
        private byte maxMemberCount;
        private String masterName;
        private int xu;
        private int luong;
        private int cup;
        private int exp;
        private int xpUpLevel;
        private byte level;
        private byte levelPercentage;
        private String description;
        private String dateCreated;
        private List<ClanItem> items;
    }

    @Getter
    @Setter
    public static class ClanItem {
        private String name;
        private int time;
    }

    @Getter
    @Setter
    public static class ClanEntry {

        int id;
        int master;
        String name;
        int icon;
        String thongBao;
        String item;
        int xu;
        int luong;
        int xp;
        int cup;
        int mem;
        int memMax;
        int level;
        String dateCreat;
        String masterName;
    }

    @Getter
    @Setter
    public static class ClanMemEntry {
        int id;
        int clan;
        Date timeJoin;
        int xu;
        int luong;
        int cup;
        int n_contribute;
        String contribute_time;
        String contribute_text;
        byte right;
        byte nv;
        byte online;
        int lever;
        byte levelPt;
        int xp;
        short[] dataEquip;
        String name;
    }

    private static ClanManager instance;

    private final ClanDao clanDao;

    public ClanManager() {
        clanDao = new ClanDao();
    }

    public static ClanManager getInstance() {
        if (instance == null) {
            instance = new ClanManager();
        }
        return instance;
    }

    public byte[] getClanIcon(short clanId) {
        byte[] data;
        Short index = clanDao.getClanIcon(clanId);
        if (index == null) {
            data = new byte[0];
        } else {
            data = Until.getFile("res/icon/clan/" + index + ".png");
        }
        return data;
    }

    public void contributeClan(short clanId, int userId, int quantity, boolean isXu) {
        String txtContribute;
        if (isXu) {
            clanDao.gopXu(clanId, quantity);
            txtContribute = Until.getStringNumber(quantity) + " xu";
        } else {
            clanDao.gopLuong(clanId, quantity);
            txtContribute = Until.getStringNumber(quantity) + " lượng";
        }
        clanDao.gopClanContribute(txtContribute, userId);
    }

    public static ArrayList<ClanMemEntry> member;

    public void getClanInfoMessage(User us, Message ms) {
        try {
            short clan = ms.reader().readShort();
            ResultSet red;
            red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM `clan` WHERE `id` = '" + clan + "' LIMIT 1;");
            if (!red.first()) {
                ms = new Message(4);
                DataOutputStream ds = ms.writer();
                ds.writeUTF(GameString.clanNull());
                ds.flush();
                us.sendMessage(ms);
                red.close();
                return;
            }
            DataOutputStream ds;
            ms = new Message(117);
            ds = ms.writer();
            ds.writeShort(red.getShort("id")); // id
            ds.writeUTF(red.getString("name")); // tên clan
            ds.writeByte(red.getInt("mem")); // thành viên
            ds.writeByte(red.getInt("memmax")); // 
            ds.writeUTF(red.getString("masterName")); // chủ clan
            ds.writeInt(red.getInt("xu")); // xu
            ds.writeInt(red.getInt("luong")); // lượng
            ds.writeInt(red.getInt("cup")); // cúp
            int level = ((int) Math.sqrt(1 + red.getInt("xp") / 6250) + 1) >> 1;
            level = level > 127 ? 127 : level;
            int xp = red.getInt("xp");
            int maxXP = 25000 * 127 * 128;
            ds.writeInt(red.getInt("xp")); // exp
            int xpUpLv = 25000 * level * (level + 1);
            xpUpLv = xpUpLv > maxXP ? maxXP : xpUpLv;
            ds.writeInt(xpUpLv); // exp to up level
            ds.writeByte(level); // lever
            xp -= (level) * (level - 1) * 25000;
            ds.writeByte((byte) (xp / level / 500)); // phần trăm lv
            ds.writeUTF(red.getString("desc"));//giới thiệu
            ds.writeUTF(red.getString("dateCreat"));
            JSONArray itemClan = (JSONArray) JSONValue.parse(red.getString("Item"));
            int lentItem = itemClan.size();
            Date[] itemClanArray = new Date[lentItem];
            boolean[] isItem = new boolean[lentItem];
            int[] idItem = new int[lentItem];
            byte count = 0;
            for (int i = 0; i < lentItem; i++) {
                JSONObject jobj = (JSONObject) itemClan.get(i);
                itemClanArray[i] = Until.getDate(jobj.get("time").toString());
                idItem[i] = ((Long) jobj.get("id")).intValue();
                if (itemClanArray[i].after(new Date())) {
                    isItem[i] = true;
                    count++;
                }
            }
            red.close();
            ds.writeByte(count);
            for (byte i = 0; i < lentItem; i++) {
                if (isItem[i]) {
                    if (ItemClanData.getItemClanById(idItem[i]) != null) {
                        ds.writeUTF(ItemClanData.getItemClanById(idItem[i]).name);
                        ds.writeInt((int) (itemClanArray[i].getTime() / 1000) - (int) (new Date().getTime() / 1000));
                    } else {
                        ds.writeUTF("ITEM: " + idItem[i]);
                        ds.writeInt((int) (itemClanArray[i].getTime() / 1000) - (int) (new Date().getTime() / 1000));
                    }
                }
            }
            ds.flush();
            us.sendMessage(ms);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<ClanMemEntry> getMemberClan(short clanId, byte page) {
        return clanDao.getClanMember(clanId, page);
    }

    public void clanItemMessage(User us, Message ms) {
        try {
            int idClan = us.getClanId();
            DataOutputStream ds;
            byte type = ms.reader().readByte();
            if (idClan > 0) {
                ResultSet red;
                red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM `clan` WHERE `id` = '" + idClan + "' LIMIT 1;");
                if (!red.first()) {
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(GameString.clanNull());
                    ds.flush();
                    us.sendMessage(ms);
                    red.close();
                    return;
                }
                int level = red.getInt("level");
                red.close();
                if (type == 0) {
                    ms = new Message(-12);
                    ds = ms.writer();
                    ArrayList<ItemClanData.ItemClan> Item = new ArrayList<>();
                    for (int i = 0; i < ItemClanData.itemClans.size(); i++) {
                        ItemClanData.ItemClan idtEntry = ItemClanData.itemClans.get(i);
                        if (idtEntry == null || idtEntry.onSale == 0) {
                            continue;
                        }
                        Item.add(idtEntry);
                    }
                    ds.writeByte(Item.size());
                    for (ItemClanData.ItemClan idtEntry : Item) {
                        ds.writeByte(idtEntry.id);
                        ds.writeUTF(idtEntry.name);
                        ds.writeInt(idtEntry.xu);
                        ds.writeInt(idtEntry.luong);
                        ds.writeByte(idtEntry.time);
                        ds.writeByte(idtEntry.level);
                    }
                    ds.flush();
                    us.sendMessage(ms);
                } else if (type == 1) {
                    byte buyType = ms.reader().readByte();
                    byte idS = ms.reader().readByte();
                    ItemClanData.ItemClan spE = ItemClanData.getItemClanById(idS);
                    if (spE.level > level) {
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.clanLevelNotEnought());
                        ds.flush();
                        us.sendMessage(ms);
                        return;
                    } else if (buyType == 0) {
                        int gia = spE.xu;
                        if (gia < 0 || spE.onSale < 1) {
                            return;
                        }
                        if (getXuClan(us) < gia) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.clanXuNotEnought());
                            ds.flush();
                            us.sendMessage(ms);
                            return;
                        }
                        updateXu(us, -gia);
                    } else if (buyType == 1) {
                        int gia = spE.luong;
                        if (gia < 0 || spE.onSale < 1) {
                            return;
                        }
                        if (getLuongClan(us) < gia) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.clanLuongNotEnought());
                            ds.flush();
                            us.sendMessage(ms);
                            return;
                        }
                        updateLuong(us, -gia);
                    } else {
                        return;
                    }
                    ItemClanData.ItemClan newIt = ItemClanData.getItemClanById(idS);
                    updateItemClan(us, idS, newIt.time);
                    ms = new Message(45);
                    ds = ms.writer();
                    ds.writeUTF(GameString.buySuccess());
                    ds.flush();
                    us.sendMessage(ms);
                }
            } else {
                ms = new Message(45);
                ds = ms.writer();
                ds.writeUTF(GameString.notClan());
                ds.flush();
                us.sendMessage(ms);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getXuClan(User us) {
        int xuclan = 0;
        if (us.getClanId() > 0) {
            try {
                ResultSet red;
                red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM `clan` WHERE `id` = '" + us.getClanId() + "' LIMIT 1;");
                red.first();
                xuclan = red.getInt("xu");
                red.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return xuclan;
    }

    public static int getLuongClan(User us) {
        int luongclan = 0;
        if (us.getClanId() > 0) {
            try {
                ResultSet red;
                red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM `clan` WHERE `id` = '" + us.getClanId() + "' LIMIT 1;");
                red.first();
                luongclan = red.getInt("luong");
                red.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return luongclan;
    }

    public static void updateXP(User us, int xp) {
        if (xp <= 0) {
            return;
        }
        if (us.getClanId() > 0) {
            if (xp > (25000 * 127 * 128)) {
                xp = 25000 * 127 * 128;
            }
            try {
                ResultSet red;
                red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT `xp` FROM `clan` WHERE `id` = '" + us.getClanId() + "' LIMIT 1;");
                if (red.first()) {
                    int level = ((int) Math.sqrt(1 + red.getInt("xp") / 6250) + 1) >> 1;
                    level = level > 127 ? 127 : level;
                    HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clan` SET `level` = " + level + ",`xp` = `xp` + " + xp + "  WHERE `id` = " + us.getClanId() + ";");
                    HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `armymem` SET `clanpoint` = `clanpoint` + " + xp + " WHERE `id` = " + us.getId() + ";");
                }
                red.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateXu(User us, int xuUp) {
        if (xuUp == 0) {
            return;
        }
        try {
            HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clan` SET  `xu` = `xu` + " + xuUp + " WHERE `id` = " + us.getClanId() + ";");
            if (xuUp > 0) {
                HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clanmem` SET `xu` = `xu` + " + xuUp + " WHERE `user` = " + us.getId() + ";");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateLuong(User us, int luongUp) {
        if (luongUp == 0) {
            return;
        }
        try {
            HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clan` SET  `luong` = `luong` + " + luongUp + " WHERE `id` = " + us.getClanId() + ";");
            if (luongUp > 0) {
                HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clanmem` SET `luong` = `luong` + " + luongUp + " WHERE `user` = " + us.getId() + ";");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateItemClan(User us, int Ids, int time) {
        if (us.getClanId() > 0) {
            Date Hours = new Date();
            try {
                ResultSet red;
                red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT `Item` FROM `clan` WHERE `id` = " + us.getClanId() + " LIMIT 1;");
                red.first();
                JSONArray itemClan = (JSONArray) JSONValue.parse(red.getString("item"));
                //neu item con thoi gian sd -> tang gio cong don thoi gian
                if (getItemClan(us, Ids)) {
                    for (int i = 0; i < itemClan.size(); i++) {
                        JSONObject item = (JSONObject) JSONValue.parse(itemClan.get(i).toString());
                        if (((Long) item.get("id")).intValue() == Ids) {
                            Hours = Until.getDate(item.get("time").toString());
                            item.put("time", Until.addNumHours(Hours, time));
                            itemClan.set(i, item);
                        }
                    }
                    //tao moi item
                } else {
                    //con co trong ruong xoa item
                    for (int i = 0; i < itemClan.size(); i++) {
                        JSONObject item = (JSONObject) JSONValue.parse(itemClan.get(i).toString());
                        if (itemClan.get(i) != null && !getItemClan(us, ((Long) item.get("id")).intValue())) {
                            itemClan.remove(i);
                        }
                    }
                    //tao
                    JSONObject item = new JSONObject();
                    item.put("id", Ids);
                    item.put("time", Until.addNumHours(new Date(), time));
                    itemClan.add(item);
                }
                red.close();
                HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clan` SET `item`='" + itemClan.toJSONString() + "' WHERE `id`=" + us.getClanId() + " LIMIT 1;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean getItemClan(User us, int id) {
        if (us.getClanId() > 0) {
            try {
                ResultSet red;
                red = HikariCPManager.getInstance().getConnection().createStatement().executeQuery("SELECT `item` FROM `clan` WHERE `id` = " + us.getClanId() + " LIMIT 1;");
                red.first();
                Date itemClanArray = new Date();
                Date DateBayGio = new Date();
                JSONArray itemClan = (JSONArray) JSONValue.parse(red.getString("item"));
                red.close();
                for (int i = 0; i < itemClan.size(); i++) {
                    JSONObject item = (JSONObject) JSONValue.parse(itemClan.get(i).toString());
                    if (item != null && id == ((Long) item.get("id")).intValue()) {
                        itemClanArray = Until.getDate(item.get("time").toString());
                        return itemClanArray.after(DateBayGio);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void getTopTeam(User us, Message ms) {
        try {
            byte page = ms.reader().readByte();
            if (page > BangXHManager.topTeam.size() / 10 || page >= 10) {
                page = 0;
            }
            ClanEntry[] topClan = BangXHManager.getTopTeam(page);
            DataOutputStream ds;
            ms = new Message(116);
            ds = ms.writer();
            int level = 0;
            int xp = 0;
            ds.writeByte(page);
            for (ClanEntry clan : topClan) {
                level = ((int) Math.sqrt(1 + clan.xp / 6250) + 1) >> 1;
                xp = clan.xp;
                xp -= (level) * (level - 1) * 25000;
                ds.writeShort(clan.id);
                ds.writeUTF(clan.name);
                ds.writeByte(clan.mem);
                ds.writeByte(clan.memMax);
                ds.writeUTF(clan.masterName);
                ds.writeInt(clan.xu);
                ds.writeInt(clan.luong);
                ds.writeInt(clan.cup); // cúp
                ds.writeByte(level > 127 ? 127 : level);
                ds.writeByte((byte) (xp / level / 500));
                ds.writeUTF(clan.thongBao);
            }
            ds.flush();
            us.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void contributeClan(User us, Message ms) throws IOException {
        if (us.getClanId() > 0) {
            try {
                byte type = ms.reader().readByte();
                int soluong = ms.reader().readInt();
                if (soluong <= 0) {
                    return;
                }
                if (type == 0) {
                    if (soluong > us.getXu()) {
                        return;
                    }
                    updateXu(us, soluong);
                    us.updateXu(-soluong);
                    HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clanmem` SET `n_contribute` = `n_contribute` + 1, `contribute_time` = '" + Until.toDateString(new Date()) + "', `contribute_text` = '" + Until.getStringNumber(soluong) + " xu" + "' WHERE `user` = " + us.getId() + ";");
                } else if (type == 1) {
                    if (soluong > us.getLuong()) {
                        return;
                    }
                    updateLuong(us, soluong);
                    us.updateLuong(-soluong);
                    HikariCPManager.getInstance().getConnection().createStatement().executeUpdate("UPDATE `clanmem` SET `n_contribute` = `n_contribute` + 1, `contribute_time` = '" + Until.toDateString(new Date()) + "', `contribute_text` = '" + Until.getStringNumber(soluong) + " lượng" + "' WHERE `user` = " + us.getId() + ";");
                }
                ms = new Message(45);
                DataOutputStream ds = ms.writer();
                ds.writeUTF("Góp thành công");
                ds.flush();
                us.sendMessage(ms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ClanInfo getClanInfo(short clanId) {
        return clanDao.getClanInfo(clanId);
    }
}
