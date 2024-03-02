package com.teamobi.mobiarmy2.army2.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FabricateItem {

    public static ArrayList<FabricateItem> entrys;
    public int id;
    public int xuRequire;
    public int luongRequire;
    public ArrayList<Item> itemRequire;
    public int rewardXu;
    public int rewardLuong;
    public int rewardCup;
    public int rewardExp;
    public ArrayList<Item> rewardItem;
    public String notification1, notification2;

    public static class Item {

        public int id;
        public int quantity;
    }

    public static FabricateItem getFabricateById(int id) {
        for (FabricateItem fab : entrys) {
            if (fab.id == id) {
                return fab;
            }
        }
        return null;
    }

    public static void init() {
        try {
            entrys = new ArrayList<>();
            ResultSet result = SQLManager.stat.executeQuery("SELECT * FROM `fabricate_item`;");
            while (result.next()) {
                FabricateItem fabricate = new FabricateItem();
                fabricate.id = result.getInt("id");
                fabricate.xuRequire = result.getInt("xuRequire");
                fabricate.luongRequire = result.getInt("luongRequire");
                fabricate.rewardXu = result.getInt("rewardXu");
                fabricate.rewardLuong = result.getInt("rewardLuong");
                fabricate.rewardCup = result.getInt("rewardCup");
                fabricate.rewardExp = result.getInt("rewardExp");
                fabricate.notification1 = result.getString("notification1");
                fabricate.notification2 = result.getString("notification2");
                JSONArray jArr = (JSONArray) JSONValue.parse(result.getString("itemRequire"));
                int size = jArr.size();
                fabricate.itemRequire = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    JSONObject obj = (JSONObject) jArr.get(i);
                    Item item = new Item();
                    item.id = ((Long) obj.get("id")).intValue();
                    item.quantity = ((Long) obj.get("quantity")).intValue();
                    fabricate.itemRequire.add(item);
                }
                jArr = (JSONArray) JSONValue.parse(result.getString("rewardItem"));
                size = jArr.size();
                fabricate.rewardItem = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    JSONObject obj = (JSONObject) jArr.get(i);
                    Item item = new Item();
                    item.id = ((Long) obj.get("id")).intValue();
                    item.quantity = ((Long) obj.get("quantity")).intValue();
                    fabricate.rewardItem.add(item);
                }
                entrys.add(fabricate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
