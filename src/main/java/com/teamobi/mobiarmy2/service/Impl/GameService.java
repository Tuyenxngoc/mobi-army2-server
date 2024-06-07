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
import com.teamobi.mobiarmy2.util.Utils;

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
                ds.writeByte(mapEntry.getId());
                ds.writeShort(mapEntry.getData().length);
                ds.write(mapEntry.getData());
                ds.writeShort(mapEntry.getBg());
                ds.writeShort(mapEntry.getMapAddY());
                ds.writeShort(mapEntry.getBullEffShower());
                ds.writeShort(mapEntry.getInWaterAddY());
                ds.writeShort(mapEntry.getCl2AddY());
                ds.writeUTF(mapEntry.getName());
                ds.writeUTF(mapEntry.getFileName());
            }
            byte[] ab = bas.toByteArray();
            Utils.saveFile(CommonConstant.mapCacheName, ab);
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
                        ds.writeShort(equipEntry.getEquipIndex());
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
                            ds.writeByte(equipEntry.getAddPoints()[j]);
                            ds.writeByte(equipEntry.getAddPercents()[j]);
                        }
                    }
                }
            }

            byte[] bytes = Utils.getFile("res/itemSpecial.png");
            if (bytes == null) {
                System.exit(1);
            }
            ds.writeShort(bytes.length);
            ds.write(bytes);
            for (int i = 0; i < NVData.CHARACTER_ENTRIES.size(); i++) {
                bytes = Utils.getFile("res/bullet/bullet" + i + ".png");
                if (bytes == null) {
                    System.exit(1);
                }
                ds.writeShort(bytes.length);
                ds.write(bytes);
            }

            byte[] data = bas.toByteArray();
            Utils.saveFile(CommonConstant.equipCacheName, data);
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
            Utils.saveFile(CommonConstant.levelCacheName, data);
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
            Utils.saveFile(CommonConstant.playerCacheName, data);
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
            Utils.saveFile(CommonConstant.iconCacheName, data);
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
            if (i != 3) {
                for (byte j = 0; j < 3; j++) {
                    defaultNvData[i][j] = characterEntry.getEquips().get(j).get(0).getEquipIndex();
                }
                defaultNvData[i][3] = -1;
                defaultNvData[i][4] = -1;
            } else {  //Trang bị mặc định đặc biệt cho kingkong
                defaultNvData[3][0] = characterEntry.getEquips().get((byte) 0).get(0).getEquipIndex();
                defaultNvData[3][1] = characterEntry.getEquips().get((byte) 1).get(0).getEquipIndex();
                defaultNvData[3][2] = -1;
                defaultNvData[3][3] = -1;
                defaultNvData[3][4] = characterEntry.getEquips().get((byte) 4).get(0).getEquipIndex();
            }
        }

        for (byte i = 0; i < NVData.CHARACTER_ENTRIES.size(); i++) {
            for (byte j = 0; j < 5; j++) {
                User.nvEquipDefault[i][j] = NVData.getEquipEntry(i, j, defaultNvData[i][j]);
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
