package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.ArmyMap;
import com.teamobi.mobiarmy2.model.Caption;
import com.teamobi.mobiarmy2.model.Character;
import com.teamobi.mobiarmy2.model.Equipment;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.util.TeamImageOutput;
import com.teamobi.mobiarmy2.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author tuyen
 */
public class GameService implements IGameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final IGameDao gameDao;

    public GameService(IGameDao gameDao) {
        this.gameDao = gameDao;
    }

    @Override
    public void setCacheMaps() {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
             DataOutputStream ds = new DataOutputStream(bas)) {
            Map<Byte, ArmyMap> sortedMaps = new TreeMap<>(MapManager.ARMY_MAPS);

            int size = sortedMaps.size();
            ds.writeByte(size);

            for (ArmyMap armyMap : sortedMaps.values()) {
                ds.writeByte(armyMap.getId());
                ds.writeShort(armyMap.getData().length);
                ds.write(armyMap.getData());
                ds.writeShort(armyMap.getBg());
                ds.writeShort(armyMap.getMapAddY());
                ds.writeShort(armyMap.getBullEffShower());
                ds.writeShort(armyMap.getInWaterAddY());
                ds.writeShort(armyMap.getCl2AddY());
                ds.writeUTF(armyMap.getName());
                ds.writeUTF(armyMap.getFileName());
            }

            byte[] ab = bas.toByteArray();
            Utils.saveFile(GameConstants.MAP_CACHE_NAME, ab);

            logger.info("Cache file created successfully: {}", GameConstants.MAP_CACHE_NAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void setCacheCharacters() {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            ds.writeByte(CharacterManager.CHARACTERS.size());

            for (Character character : CharacterManager.CHARACTERS) {
                ds.writeByte(character.getId());
                ds.writeShort(character.getDamage());
                ds.writeByte(character.getEquips().size());

                for (byte i = 0; i < character.getEquips().size(); i++) {
                    List<Equipment> equipmentEntries = character.getEquips().get(i);
                    ds.writeByte(i);
                    ds.writeByte(equipmentEntries.size());

                    for (Equipment equipEntry : equipmentEntries) {
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

            byte[] bytes = Utils.getFile(GameConstants.IMAGE_BASE_URL + "/itemSpecial.png");
            if (bytes == null) {
                System.exit(1);
            }
            ds.writeShort(bytes.length);
            ds.write(bytes);
            for (int i = 0; i < CharacterManager.CHARACTERS.size(); i++) {
                bytes = Utils.getFile(String.format(GameConstants.BULLET_IMAGE_PATH, i));
                if (bytes == null) {
                    System.exit(1);
                }
                ds.writeShort(bytes.length);
                ds.write(bytes);
            }

            byte[] data = bas.toByteArray();
            Utils.saveFile(GameConstants.EQUIP_CACHE_NAME, data);
            bas.close();
            ds.close();

            logger.info("Cache file created successfully: {}", GameConstants.EQUIP_CACHE_NAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void setCacheCaptionLevels() {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            int size = CaptionManager.CAPTIONS.size();
            ds.writeByte(size);
            for (int i = size - 1; i >= 0; i--) {
                Caption capEntry = CaptionManager.CAPTIONS.get(i);
                ds.writeUTF(capEntry.getCaption());
                ds.writeByte(capEntry.getLevel());
            }
            byte[] data = bas.toByteArray();
            Utils.saveFile(GameConstants.LEVEL_CACHE_NAME, data);
            bas.close();
            ds.close();

            logger.info("Cache file created successfully: {}", GameConstants.LEVEL_CACHE_NAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void setCachePlayerImages() {
        try {
            TeamImageOutput tio = new TeamImageOutput();
            File playerDir = new File(GameConstants.PLAYER_PATH);
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
            Utils.saveFile(GameConstants.PLAYER_CACHE_NAME, data);

            logger.info("Cache file created successfully: {}", GameConstants.PLAYER_CACHE_NAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void setCacheMapIcons() {
        try {
            TeamImageOutput tio = new TeamImageOutput();
            File mapDir = new File(GameConstants.MAP_LOGO_PATH);
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
            Utils.saveFile(GameConstants.ICON_CACHE_NAME, data);

            logger.info("Cache file created successfully: " + GameConstants.ICON_CACHE_NAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void getFormulaData() {
        gameDao.getAllFormula();
        logger.info("Loaded {} formula entries successfully.", FormulaManager.FORMULA.size());
    }

    @Override
    public void getPaymentData() {
        gameDao.getAllPayment();
        logger.info("Loaded {} payment entries successfully.", PaymentManager.PAYMENT_MAP.size());
    }

    @Override
    public void getMissionData() {
        gameDao.getAllMissions();
        logger.info("Loaded {} mission entries successfully.", MissionManager.MISSION_LIST.size());
    }

    @Override
    public void getMapData() {
        gameDao.getAllMapData();
        logger.info("Loaded {} map entries successfully.", MapManager.ARMY_MAPS.size());
    }

    @Override
    public void getCharacterData() {
        gameDao.getAllCharacterData();
        logger.info("Loaded {} character entries successfully.", CharacterManager.CHARACTERS.size());
    }

    @Override
    public void getEquipData() {
        gameDao.getAllEquip();
        logger.info("Loaded {} equipment entries successfully.", CharacterManager.EQUIPMENTS.size());
    }

    @Override
    public void getLvXpData() {
        gameDao.getAllXpData();
        logger.info("Loaded {} player XP entries successfully.", PlayerXpManager.LEVEL_XP_REQUIRED_LIST.size());
        logger.info("Loaded {} clan XP entries successfully.", ClanXpManager.LEVEL_XP_REQUIRED_LIST.size());
    }

    @Override
    public void getFabricateItemData() {
        gameDao.getAllFabricateItems();
        logger.info("Loaded {} fabricate items successfully.", FabricateItemManager.FABRICATE_ITEMS.size());
    }

    @Override
    public void setCaptionLevelData() {
        gameDao.getAllCaptionLevel();
        logger.info("Loaded {} caption level entries successfully.", CaptionManager.CAPTIONS.size());
    }

    @Override
    public void getItemData() {
        gameDao.getAllItem();
        logger.info("Loaded {} fight item entries successfully.", FightItemManager.FIGHT_ITEMS.size());
    }

    @Override
    public void getClanShopData() {
        gameDao.getAllItemClan();
        logger.info("Loaded {} clan shop items successfully.", ClanItemManager.CLAN_ITEM_MAP.size());
    }

    @Override
    public void getSpecialItemData() {
        gameDao.getAllSpecialItem();
        logger.info("Loaded {} special items successfully.", SpecialItemManager.getSpecialItems().size());
    }
}
