package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.CaptionEntry;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.map.MapEntry;
import com.teamobi.mobiarmy2.repository.*;
import com.teamobi.mobiarmy2.server.ServerManager;
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
            int size = MapRepository.MAP_ENTRIES.size();
            ds.writeByte(size);
            for (int i = 0; i < size; i++) {
                MapEntry mapEntry = MapRepository.MAP_ENTRIES.get(i);
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
            Utils.saveFile(CommonConstant.MAP_CACHE_NAME, ab);

            ServerManager.getInstance().logger().success("Cache file created successfully: " + CommonConstant.MAP_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(GameService.class, e);
        }
    }

    @Override
    public void setCacheCharacters() {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            ds.writeByte(CharacterRepository.CHARACTER_ENTRIES.size());

            for (CharacterEntry characterEntry : CharacterRepository.CHARACTER_ENTRIES) {
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
            for (int i = 0; i < CharacterRepository.CHARACTER_ENTRIES.size(); i++) {
                bytes = Utils.getFile("res/bullet/bullet" + i + ".png");
                if (bytes == null) {
                    System.exit(1);
                }
                ds.writeShort(bytes.length);
                ds.write(bytes);
            }

            byte[] data = bas.toByteArray();
            Utils.saveFile(CommonConstant.EQUIP_CACHE_NAME, data);
            bas.close();
            ds.close();

            ServerManager.getInstance().logger().success("Cache file created successfully: " + CommonConstant.EQUIP_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(GameService.class, e);
        }
    }

    @Override
    public void setCacheCaptionLevels() {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            int size = CaptionRepository.CAPTION_ENTRIES.size();
            ds.writeByte(size);
            for (int i = size - 1; i >= 0; i--) {
                CaptionEntry capEntry = CaptionRepository.CAPTION_ENTRIES.get(i);
                ds.writeUTF(capEntry.getCaption());
                ds.writeByte(capEntry.getLevel());
            }
            byte[] data = bas.toByteArray();
            Utils.saveFile(CommonConstant.LEVEL_CACHE_NAME, data);
            bas.close();
            ds.close();

            ServerManager.getInstance().logger().success("Cache file created successfully: " + CommonConstant.LEVEL_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(GameService.class, e);
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
            Utils.saveFile(CommonConstant.PLAYER_CACHE_NAME, data);

            ServerManager.getInstance().logger().success("Cache file created successfully: " + CommonConstant.PLAYER_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(GameService.class, e);
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
            Utils.saveFile(CommonConstant.ICON_CACHE_NAME, data);

            ServerManager.getInstance().logger().success("Cache file created successfully: " + CommonConstant.ICON_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(GameService.class, e);
        }
    }

    @Override
    public void getFormulaData() {
        gameDao.getAllFormula();
        ServerManager.getInstance().logger().success("Loaded " + FormulaRepository.FORMULA.size() + " formula entries successfully.");
    }

    @Override
    public void getPaymentData() {
        gameDao.getAllPayment();
        ServerManager.getInstance().logger().success("Loaded " + PaymentRepository.PAYMENT_ENTRY_MAP.size() + " payment entries successfully.");
    }

    @Override
    public void getMissionData() {
        gameDao.getAllMissions();
        ServerManager.getInstance().logger().success("Loaded " + MissionRepository.MISSION_LIST.size() + " mission entries successfully.");
    }

    @Override
    public void getMapData() {
        gameDao.getAllMapData();
        ServerManager.getInstance().logger().success("Loaded " + MapRepository.MAP_ENTRIES.size() + " map entries successfully.");
    }

    @Override
    public void getCharacterData() {
        gameDao.getAllCharacterData();
        ServerManager.getInstance().logger().success("Loaded " + CharacterRepository.CHARACTER_ENTRIES.size() + " character entries successfully.");
    }

    @Override
    public void getEquipData() {
        gameDao.getAllEquip();
        ServerManager.getInstance().logger().success("Loaded " + CharacterRepository.EQUIPMENT_ENTRIES.size() + " equipment entries successfully.");
    }

    @Override
    public void getLvXpData() {
        gameDao.getAllXpData();
        ServerManager.getInstance().logger().success("Loaded " + PlayerXpRepository.LEVEL_XP_REQUIRED_ENTRIES.size() + " player XP entries successfully.");
        ServerManager.getInstance().logger().success("Loaded " + ClanXpRepository.LEVEL_XP_REQUIRED_ENTRIES.size() + " clan XP entries successfully.");
    }

    @Override
    public void getFabricateItemData() {
        gameDao.getAllFabricateItems();
        ServerManager.getInstance().logger().success("Loaded " + FabricateItemRepository.FABRICATE_ITEM_ENTRIES.size() + " fabricate items successfully.");
    }

    @Override
    public void setCaptionLevelData() {
        gameDao.getAllCaptionLevel();
        ServerManager.getInstance().logger().success("Loaded " + CaptionRepository.CAPTION_ENTRIES.size() + " caption level entries successfully.");
    }

    @Override
    public void getItemData() {
        gameDao.getAllItem();
        ServerManager.getInstance().logger().success("Loaded " + FightItemRepository.FIGHT_ITEM_ENTRIES.size() + " fight item entries successfully.");
    }

    @Override
    public void getClanShopData() {
        gameDao.getAllItemClan();
        ServerManager.getInstance().logger().success("Loaded " + ClanItemRepository.CLAN_ITEM_ENTRY_MAP.size() + " clan shop items successfully.");
    }

    @Override
    public void getSpecialItemData() {
        gameDao.getAllSpecialItem();
        ServerManager.getInstance().logger().success("Loaded " + SpecialItemRepository.SPECIAL_ITEM_ENTRIES.size() + " special items successfully.");
    }
}
