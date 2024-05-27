package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.CaptionData;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.equip.EquipmentData;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.equip.NVEntry;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.team.TeamImageOutput;
import com.teamobi.mobiarmy2.util.Until;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author tuyen
 */
public class GameService implements IGameService {

    private final IGameDao gameDao;

    public GameService(IGameDao gameDao) {
        this.gameDao = gameDao;
    }

    @Override
    public void setCacheMaps() {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
             DataOutputStream ds = new DataOutputStream(bas)) {
            int size = MapData.MAPS.size();
            ds.writeByte(size);
            for (int i = 0; i < size; i++) {
                MapData.Map mapEntry = MapData.MAPS.get(i);
                ds.writeByte(mapEntry.id);
                ds.writeShort(mapEntry.data.length);
                ds.write(mapEntry.data);
                ds.writeShort(mapEntry.bg);
                ds.writeShort(mapEntry.mapAddY);
                ds.writeShort(mapEntry.bullEffShower);
                ds.writeShort(mapEntry.inWaterAddY);
                ds.writeShort(mapEntry.cl2AddY);
                ds.writeUTF(mapEntry.name);
                ds.writeUTF(mapEntry.fileName);
            }
            byte[] ab = bas.toByteArray();
            Until.saveFile(CommonConstant.mapCacheName, ab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheCharacters() {
        try {
            ByteArrayOutputStream bas1 = new ByteArrayOutputStream();
            DataOutputStream ds1 = new DataOutputStream(bas1);
            int numChamp = NVData.entrys.size();
            ds1.writeByte(numChamp);
            for (int i = 0; i < numChamp; i++) {
                NVEntry nvEntry = NVData.entrys.get(i);
                ds1.writeByte(nvEntry.id);
                ds1.writeShort(nvEntry.sat_thuong);
                int numEquipData = nvEntry.trangbis.size();
                ds1.writeByte(numEquipData);
                for (int j = 0; j < numEquipData; j++) {
                    EquipmentData equipDataEntry = nvEntry.trangbis.get(j);
                    ds1.writeByte(equipDataEntry.id);
                    int numEquip = equipDataEntry.entrys.size();
                    ds1.writeByte(numEquip);
                    for (int k = 0; k < numEquip; k++) {
                        EquipmentEntry equipEntry = equipDataEntry.entrys.get(k);
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
                            ds1.writeByte(equipEntry.percentAdd[l]);
                        }
                    }
                }
            }

            byte[] dat = Until.getFile("res/itemSpecial.png");
            if (dat == null) {
                System.exit(1);
            }
            ds1.writeShort(dat.length);
            ds1.write(dat);
            for (int i = 0; i < numChamp; i++) {
                dat = Until.getFile("res/bullet/bullet" + i + ".png");
                if (dat == null) {
                    System.exit(1);
                }
                ds1.writeShort(dat.length);
                ds1.write(dat);
            }

            byte[] ab1 = bas1.toByteArray();
            Until.saveFile(CommonConstant.equipCacheName, ab1);
            bas1.close();
            ds1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheCaptionLevels() {
        try {
            ByteArrayOutputStream bas2 = new ByteArrayOutputStream();
            DataOutputStream ds2 = new DataOutputStream(bas2);
            int numCaption = CaptionData.captions.size();
            ds2.writeByte(numCaption);
            for (int i = numCaption - 1; i >= 0; i--) {
                CaptionData.Caption capEntry = CaptionData.captions.get(i);
                ds2.writeUTF(capEntry.caption);
                ds2.writeByte(capEntry.level);
            }
            byte[] ab2 = bas2.toByteArray();
            Until.saveFile(CommonConstant.levelCacheName, ab2);
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
            if (playerFiles == null) {
                System.exit(1);
            }
            for (File f : playerFiles) {
                tos.addFile(f.getName(), f.getPath());
            }
            byte[] ab3 = tos.output();
            Until.saveFile(CommonConstant.playerCacheName, ab3);
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
            if (mapFiles == null) {
                System.exit(1);
            }
            for (File f : mapFiles) {
                tos2.addFile(f.getName(), f.getPath());
            }
            byte[] ab4 = tos2.output();
            Until.saveFile(CommonConstant.iconCacheName, ab4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDefaultNvData() {
        short[][] defaultNvData = new short[NVData.entrys.size()][5];
        User.nvEquipDefault = new EquipmentEntry[NVData.entrys.size()][5];
        for (int i = 0; i < NVData.entrys.size(); i++) {
            NVEntry nvdat = NVData.entrys.get(i);
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
    public void getFormulaData() {
        gameDao.getAllFormula();
    }

    @Override
    public void getPaymentData() {
        gameDao.getAllPayment();
    }

    @Override
    public void getMissionData() {
        gameDao.getAllMissions();
    }

    @Override
    public void getMapData() {
        gameDao.getAllMapData();
    }

    @Override
    public void getCharacterData() {
        gameDao.getAllCharacterData();

    }

    @Override
    public void getEquipData() {
        gameDao.getAllEquip();
    }

    @Override
    public void getLvXpData() {
        gameDao.getAllXpData();
    }

    @Override
    public void setCaptionLevelData() {
        gameDao.getAllCaptionLevel();
    }

    @Override
    public void getItemData() {
        gameDao.getAllItem();
    }

    @Override
    public void getClanShopData() {
        gameDao.getAllItemClan();
    }

    @Override
    public void getSpecialItemData() {
        gameDao.getAllSpecialItem();
    }
}
