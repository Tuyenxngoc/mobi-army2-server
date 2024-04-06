package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.CaptionData;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.team.TeamImageOutput;
import com.teamobi.mobiarmy2.util.Until;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GameService implements IGameService {

    private final IGameDao gameDao;

    public GameService(IGameDao gameDao) {
        this.gameDao = gameDao;
    }

    @Override
    public void setCacheMaps() {
        gameDao.getAllMapData();

        try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
             DataOutputStream ds = new DataOutputStream(bas)) {
            int size = MapData.entries.size();
            ds.writeByte(size);
            System.out.println("Init map numMap=" + size);
            for (int i = 0; i < size; i++) {
                MapData.MapDataEntry mapEntry = MapData.entries.get(i);
                ds.writeByte(mapEntry.id);
                ds.writeShort(mapEntry.data.length);
                ds.write(mapEntry.data);
                ds.writeShort(mapEntry.bg);
                ds.writeShort(mapEntry.mapAddY);
                ds.writeShort(mapEntry.bullEffShower);
                ds.writeShort(mapEntry.inWaterAddY);
                ds.writeShort(mapEntry.cl2AddY);
                ds.writeUTF(mapEntry.name);
                ds.writeUTF(mapEntry.file);
                System.out.println("   - id= " + mapEntry.id + " name= " + mapEntry.name + " file= " + mapEntry.file);
            }
            byte[] ab = bas.toByteArray();
            Until.saveFile(CommonConstant.mapCacheName, ab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheCharacters() {
        gameDao.getAllCharacterData();
        gameDao.getAllEquip();

        try {
            ByteArrayOutputStream bas1 = new ByteArrayOutputStream();
            DataOutputStream ds1 = new DataOutputStream(bas1);
            int numChamp = NVData.entrys.size();
            ds1.writeByte(numChamp);
            System.out.println("Init nhan vat numNV= " + numChamp);
            for (int i = 0; i < numChamp; i++) {
                NVData.NVEntry nvEntry = NVData.entrys.get(i);
                ds1.writeByte(nvEntry.id);
                ds1.writeShort(nvEntry.sat_thuong);
                int numEquipData = nvEntry.trangbis.size();
                ds1.writeByte(numEquipData);
                for (int j = 0; j < numEquipData; j++) {
                    NVData.EquipmentData equipDataEntry = nvEntry.trangbis.get(j);
                    ds1.writeByte(equipDataEntry.id);
                    int numEquip = equipDataEntry.entrys.size();
                    ds1.writeByte(numEquip);
                    for (int k = 0; k < numEquip; k++) {
                        NVData.EquipmentEntry equipEntry = equipDataEntry.entrys.get(k);
                        ds1.writeShort(equipEntry.id);
                        if (equipDataEntry.id == 0) {
                            ds1.writeByte(equipEntry.bullId);
                        }
                        ds1.writeShort(equipEntry.frame);
                        ds1.writeByte(equipEntry.lvRequire);
                        for (int l = 0; l < 6; l++) {
                            ds1.writeShort(equipEntry.bigImageCutX[l]);
                            ds1.writeShort(equipEntry.bigImageCutY[l]);
                            ds1.writeByte(equipEntry.bigImageSizeX[l]);
                            ds1.writeByte(equipEntry.bigImageSizeY[l]);
                            ds1.writeByte(equipEntry.bigImageAlignX[l]);
                            ds1.writeByte(equipEntry.bigImageAlignY[l]);
                        }
                        for (int l = 0; l < 5; l++) {
                            ds1.writeByte(equipEntry.invAdd[l]);
                            ds1.writeByte(equipEntry.percenAdd[l]);
                        }
                    }
                }
            }

            byte[] dat = Until.getFile("res/itemSpecial.png");
            if (dat == null) {
                System.out.println("File item_special.png not found!");
                System.exit(0);
            }
            System.out.println("[coreLG/a] " + "Lent Icon= " + dat.length);
            ds1.writeShort(dat.length);
            ds1.write(dat);
            for (int i = 0; i < numChamp; i++) {
                dat = Until.getFile("res/bullet/bullet" + i + ".png");
                if (dat == null) {
                    System.out.println("File bullet" + i + ".png not found!");
                    System.exit(0);
                }
                ds1.writeShort(dat.length);
                ds1.write(dat);
            }

            byte[] ab1 = bas1.toByteArray();
            Until.saveFile("cache/equipdata2", ab1);
            bas1.close();
            ds1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheCaptionLevels() {
        gameDao.getAllCaptionLevel();

        try {
            ByteArrayOutputStream bas2 = new ByteArrayOutputStream();
            DataOutputStream ds2 = new DataOutputStream(bas2);
            int numCaption = CaptionData.entrys.size();
            ds2.writeByte(numCaption);
            System.out.println("Init caption entry numCaption= " + numCaption);
            for (int i = numCaption - 1; i >= 0; i--) {
                CaptionData.CaptionEntry capEntry = CaptionData.entrys.get(i);
                ds2.writeUTF(capEntry.caption);
                ds2.writeByte(capEntry.level);
                System.out.println("  lvl= " + capEntry.level + " str= " + capEntry.caption);
            }
            byte[] ab2 = bas2.toByteArray();
            Until.saveFile("cache/levelCData2", ab2);
            bas2.close();
            ds2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCachePlayerImages() {
        try {
            TeamImageOutput tos = new TeamImageOutput();
            File playerDir = new File("res/player");
            if (!playerDir.exists()) {
                throw new IOException("Folder player not found!");
            }
            File[] playerFiles = playerDir.listFiles();
            for (File f : playerFiles) {
                tos.addFile(f.getName(), f.getPath());
            }
            byte[] ab3 = tos.output();
            Until.saveFile("cache/playerdata2", ab3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheMapIcons() {
        try {
            TeamImageOutput tos2 = new TeamImageOutput();
            File mapDir = new File("res/map/icon");
            if (!mapDir.exists()) {
                throw new IOException("Folder map icon not found!");
            }
            File[] mapFiles = mapDir.listFiles();
            for (File f : mapFiles) {
                tos2.addFile(f.getName(), f.getPath());
            }
            byte[] ab4 = tos2.output();
            Until.saveFile("cache/icondata2", ab4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getItemData() {
        gameDao.getAllItem();
    }

    @Override
    public void setDefaultNvData() {
        short[][] defaultNvData = new short[NVData.entrys.size()][5];
        User.nvEquipDefault = new NVData.EquipmentEntry[NVData.entrys.size()][5];
        for (int i = 0; i < NVData.entrys.size(); i++) {
            NVData.NVEntry nvdat = NVData.entrys.get(i);
            for (int j = 0; j < 3; j++) {
                defaultNvData[i][j] = nvdat.trangbis.get(j).entrys.get(0).id;
            }
            defaultNvData[i][3] = 0;
            defaultNvData[i][4] = 0;
        }
        for (int i = 0; i < NVData.entrys.size(); i++) {
            for (int j = 0; j < 3; j++) {
                User.nvEquipDefault[i][j] = NVData.getEquipEntryById(i, j, defaultNvData[i][j]);
            }
        }
    }

    @Override
    public void getClanShopData() {
        gameDao.getAllItemClan();
    }

    @Override
    public void getSpecialItemData() {
        gameDao.getAllSpecialItem();
    }

    @Override
    public void sendNVData(User user, IServerConfig config) {
        try {
            // Send mss 64
            Message ms = new Message(64);
            DataOutputStream ds = ms.writer();
            ArrayList<NVData.NVEntry> nvdatas = NVData.entrys;
            int len = nvdatas.size();
            ds.writeByte(len);
            // Ma sat gio cac nv
            for (NVData.NVEntry entry : nvdatas) {
                ds.writeByte(entry.ma_sat_gio);
            }
            // Goc cuu tieu
            ds.writeByte(len);
            for (NVData.NVEntry entry : nvdatas) {
                ds.writeShort(entry.goc_min);
            }
            // Sat thuong 1 vien dan
            ds.writeByte(len);
            for (NVData.NVEntry nvEntry : nvdatas) {
                ds.writeByte(nvEntry.sat_thuong_dan);
            }
            // So dan
            ds.writeByte(len);
            for (NVData.NVEntry nvdata : nvdatas) {
                ds.writeByte(nvdata.so_dan);
            }
            // Max player
            ds.writeByte(config.getMaxElementFight());
            // Map boss
            ds.writeByte(config.getNumMapBoss());
            for (int i = 0; i < config.getNumMapBoss(); i++) {
                ds.writeByte(config.getStartMapBoss() + i);
            }
            // Type map boss
            for (int i = 0; i < config.getNumMapBoss(); i++) {
                ds.writeByte(config.getMapIdBoss()[i]);
            }
            // NUMB Player
            ds.writeByte(config.getNumbPlayers());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendRoomInfo(User user, IServerConfig config) {
        sendRoomCaption(user, config);
        sendRoomName(user, config);
    }

    private void sendRoomName(User user, IServerConfig config) {
        try {
            // Cap nhat ten khu vuc
            Message ms = new Message(-19);
            DataOutputStream ds = ms.writer();
            // Size
            ds.writeByte(config.getNameRooms().length);
            for (int i = 0; i < config.getNameRooms().length; i++) {
                // He so cong
                int namen = config.getNameRoomNumbers()[i];
                int typen = config.getNameRoomTypes()[i];
                if (namen > (config.getNRoom()[typen] + config.getRoomTypeStartNum()[typen])) {
                    continue;
                }
                int notRoom = 0;
                for (int j = 0; j < typen; j++) {
                    if (config.getNRoom()[j] > 0) {
                        notRoom++;
                    }
                }
                ds.writeByte(config.getRoomTypeStartNum()[typen] + notRoom);
                // Ten cho phong viet hoa
                ds.writeUTF("Phòng " + (config.getRoomTypeStartNum()[typen] + namen) + ": " + config.getNameRooms()[i]);
                // So
                ds.writeByte(namen);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRoomCaption(User user, IServerConfig config) {
        try {
            // Send mss 88
            Message ms = new Message(88);
            DataOutputStream ds = ms.writer();
            // Size
            ds.writeByte(config.getRoomTypes().length);
            for (int i = 0; i < config.getRoomTypes().length; i++) {
                // Ten viet hoa
                ds.writeUTF(config.getRoomTypes()[i]);
                // Ten tieng anh
                ds.writeUTF(config.getRoomTypesEng()[i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMapCollisionInfo(User user, IServerConfig config) {
        try {
            // Send mss 92
            Message ms = new Message(92);
            DataOutputStream ds = ms.writer();
            ds.writeShort(MapData.idNotCollisions.length);
            for (int i = 0; i < MapData.idNotCollisions.length; i++) {
                ds.writeShort(MapData.idNotCollisions[i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getFormulaData() {
        gameDao.getAllFomular();
    }

    @Override
    public void getPaymentData() {
        gameDao.getAllPayment();
    }

    @Override
    public void getMissionData() {
        gameDao.getAllMissions();
    }

}
