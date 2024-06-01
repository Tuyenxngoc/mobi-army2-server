package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.CaptionData;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.entry.CaptionEntry;
import com.teamobi.mobiarmy2.model.entry.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;
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
            int size = MapData.MAP_ENTRIES.size();
            ds.writeByte(size);
            for (int i = 0; i < size; i++) {
                MapEntry mapEntry = MapData.MAP_ENTRIES.get(i);
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
            ds.writeByte(NVData.CHARACTER_ENTRIES.size());

            for (CharacterEntry characterEntry : NVData.CHARACTER_ENTRIES) {
                ds.writeByte(characterEntry.getId());
                ds.writeShort(characterEntry.getDamage());
                ds.writeByte(characterEntry.getEquips().size());

                for (byte i = 0; i < characterEntry.getEquips().size(); i++) {
                    List<EquipmentEntry> equipmentEntries = characterEntry.getEquips().get(i);
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
            for (int i = 0; i < NVData.CHARACTER_ENTRIES.size(); i++) {
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
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            int size = CaptionData.CAPTION_ENTRIES.size();
            ds.writeByte(size);
            for (int i = size - 1; i >= 0; i--) {
                CaptionEntry capEntry = CaptionData.CAPTION_ENTRIES.get(i);
                ds.writeUTF(capEntry.getCaption());
                ds.writeByte(capEntry.getLevel());
            }
            byte[] data = bas.toByteArray();
            Until.saveFile(CommonConstant.levelCacheName, data);
            bas.close();
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCachePlayerImages() {
        try {
            TeamImageOutput tio = new TeamImageOutput();
            File playerDir = new File("res/player");
            if (!playerDir.exists()) {
                throw new IOException("Folder player not found!");
            }
            File[] playerFiles = playerDir.listFiles();
            if (playerFiles == null) {
                System.exit(1);
            }
            for (File f : playerFiles) {
                tio.addFile(f.getName(), f.getPath());
            }
            byte[] data = tio.output();
            Until.saveFile(CommonConstant.playerCacheName, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheMapIcons() {
        try {
            TeamImageOutput tio = new TeamImageOutput();
            File mapDir = new File("res/map/icon");
            if (!mapDir.exists()) {
                throw new IOException("Folder map icon not found!");
            }
            File[] mapFiles = mapDir.listFiles();
            if (mapFiles == null) {
                System.exit(1);
            }
            for (File f : mapFiles) {
                tio.addFile(f.getName(), f.getPath());
            }
            byte[] data = tio.output();
            Until.saveFile(CommonConstant.iconCacheName, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDefaultNvData() {
        short[][] defaultNvData = new short[NVData.CHARACTER_ENTRIES.size()][5];
        User.nvEquipDefault = new EquipmentEntry[NVData.CHARACTER_ENTRIES.size()][5];
        for (byte i = 0; i < NVData.CHARACTER_ENTRIES.size(); i++) {
            CharacterEntry characterEntry = NVData.CHARACTER_ENTRIES.get(i);
            for (byte j = 0; j < 3; j++) {
                defaultNvData[i][j] = characterEntry.equips.get(j).get(0).getIndex();
            }
            defaultNvData[i][3] = 0;
            defaultNvData[i][4] = 0;
        }
        for (byte i = 0; i < NVData.CHARACTER_ENTRIES.size(); i++) {
            for (byte j = 0; j < 3; j++) {
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
