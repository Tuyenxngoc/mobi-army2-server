package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.CaptionData;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.team.TeamImageOutput;
import com.teamobi.mobiarmy2.util.Until;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            ds.writeByte(NVData.characterEntries.size());

            for (CharacterEntry characterEntry : NVData.characterEntries) {
                ds.writeByte(characterEntry.getId());
                ds.writeShort(characterEntry.getDamage());
                ds.writeByte(characterEntry.equips.size());

                for (byte i = 0; i < characterEntry.equips.size(); i++) {
                    List<EquipmentEntry> equipmentEntries = characterEntry.equips.get(i);
                    ds.writeByte(i);
                    ds.writeByte(equipmentEntries.size());

                    for (EquipmentEntry equipEntry : equipmentEntries) {
                        ds.writeShort(equipEntry.getIndex());
                        if (i == 0) {
                            ds.writeByte(equipEntry.getBulletId());
                        }
                        ds.writeShort(equipEntry.getFrameCount());
                        ds.writeByte(equipEntry.getLevelRequirement());

                        for (int j = 0; j < 6; j++) {
                            ds.writeShort(equipEntry.getBigImageCutX()[j]);
                            ds.writeShort(equipEntry.getBigImageCutY()[j]);
                            ds.writeByte(equipEntry.getBigImageSizeX()[j]);
                            ds.writeByte(equipEntry.getBigImageSizeY()[j]);
                            ds.writeByte(equipEntry.getBigImageAlignX()[j]);
                            ds.writeByte(equipEntry.getBigImageAlignY()[j]);
                        }

                        for (int j = 0; j < 5; j++) {
                            ds.writeByte(equipEntry.getAdditionalPoints()[j]);
                            ds.writeByte(equipEntry.getAdditionalPercent()[j]);
                        }
                    }
                }
            }

            byte[] bytes = Until.getFile("res/itemSpecial.png");
            if (bytes == null) {
                System.exit(1);
            }
            ds.writeShort(bytes.length);
            ds.write(bytes);
            for (int i = 0; i < NVData.characterEntries.size(); i++) {
                bytes = Until.getFile("res/bullet/bullet" + i + ".png");
                if (bytes == null) {
                    System.exit(1);
                }
                ds.writeShort(bytes.length);
                ds.write(bytes);
            }

            byte[] data = bas.toByteArray();
            Until.saveFile(CommonConstant.equipCacheName, data);
            bas.close();
            ds.close();
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
        short[][] defaultNvData = new short[NVData.characterEntries.size()][5];
        User.nvEquipDefault = new EquipmentEntry[NVData.characterEntries.size()][5];
        for (byte i = 0; i < NVData.characterEntries.size(); i++) {
            CharacterEntry characterEntry = NVData.characterEntries.get(i);
            for (byte j = 0; j < 3; j++) {
                defaultNvData[i][j] = characterEntry.equips.get(j).get(0).getIndex();
            }
            defaultNvData[i][3] = 0;
            defaultNvData[i][4] = 0;
        }
        for (int i = 0; i < NVData.characterEntries.size(); i++) {
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
