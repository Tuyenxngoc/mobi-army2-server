package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.CaptionEntry;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.map.MapEntry;
import com.teamobi.mobiarmy2.repository.*;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.util.TeamImageOutput;
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
            Utils.saveFile(GameConstants.MAP_CACHE_NAME, ab);

            ServerManager.getInstance().getLog().success("Cache file created successfully: " + GameConstants.MAP_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().getLog().logException(GameService.class, e);
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

            byte[] bytes = Utils.getFile(GameConstants.IMAGE_BASE_URL + "/itemSpecial.png");
            if (bytes == null) {
                System.exit(1);
            }
            ds.writeShort(bytes.length);
            ds.write(bytes);
            for (int i = 0; i < CharacterRepository.CHARACTER_ENTRIES.size(); i++) {
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

            ServerManager.getInstance().getLog().success("Cache file created successfully: " + GameConstants.EQUIP_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().getLog().logException(GameService.class, e);
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
            Utils.saveFile(GameConstants.LEVEL_CACHE_NAME, data);
            bas.close();
            ds.close();

            ServerManager.getInstance().getLog().success("Cache file created successfully: " + GameConstants.LEVEL_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().getLog().logException(GameService.class, e);
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

            ServerManager.getInstance().getLog().success("Cache file created successfully: " + GameConstants.PLAYER_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().getLog().logException(GameService.class, e);
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

            ServerManager.getInstance().getLog().success("Cache file created successfully: " + GameConstants.ICON_CACHE_NAME);
        } catch (IOException e) {
            ServerManager.getInstance().getLog().logException(GameService.class, e);
        }
    }

    @Override
    public void getFormulaData() {
        gameDao.getAllFormula();
        ServerManager.getInstance().getLog().success("Loaded " + FormulaRepository.FORMULA.size() + " formula entries successfully.");
    }

    @Override
    public void getPaymentData() {
        gameDao.getAllPayment();
        ServerManager.getInstance().getLog().success("Loaded " + PaymentRepository.PAYMENT_ENTRY_MAP.size() + " payment entries successfully.");
    }

    @Override
    public void getMissionData() {
        gameDao.getAllMissions();
        ServerManager.getInstance().getLog().success("Loaded " + MissionRepository.MISSION_LIST.size() + " mission entries successfully.");
    }

    @Override
    public void getMapData() {
        gameDao.getAllMapData();
        ServerManager.getInstance().getLog().success("Loaded " + MapRepository.MAP_ENTRIES.size() + " map entries successfully.");
    }

    @Override
    public void getCharacterData() {
        gameDao.getAllCharacterData();
        ServerManager.getInstance().getLog().success("Loaded " + CharacterRepository.CHARACTER_ENTRIES.size() + " character entries successfully.");
    }

    @Override
    public void getEquipData() {
        gameDao.getAllEquip();
        ServerManager.getInstance().getLog().success("Loaded " + CharacterRepository.EQUIPMENT_ENTRIES.size() + " equipment entries successfully.");
    }

    @Override
    public void getLvXpData() {
        gameDao.getAllXpData();
        ServerManager.getInstance().getLog().success("Loaded " + PlayerXpRepository.LEVEL_XP_REQUIRED_ENTRIES.size() + " player XP entries successfully.");
        ServerManager.getInstance().getLog().success("Loaded " + ClanXpRepository.LEVEL_XP_REQUIRED_ENTRIES.size() + " clan XP entries successfully.");
    }

    @Override
    public void getFabricateItemData() {
        gameDao.getAllFabricateItems();
        ServerManager.getInstance().getLog().success("Loaded " + FabricateItemRepository.FABRICATE_ITEM_ENTRIES.size() + " fabricate items successfully.");
    }

    @Override
    public void setCaptionLevelData() {
        gameDao.getAllCaptionLevel();
        ServerManager.getInstance().getLog().success("Loaded " + CaptionRepository.CAPTION_ENTRIES.size() + " caption level entries successfully.");
    }

    @Override
    public void getItemData() {
        gameDao.getAllItem();
        ServerManager.getInstance().getLog().success("Loaded " + FightItemRepository.FIGHT_ITEM_ENTRIES.size() + " fight item entries successfully.");
    }

    @Override
    public void getClanShopData() {
        gameDao.getAllItemClan();
        ServerManager.getInstance().getLog().success("Loaded " + ClanItemRepository.CLAN_ITEM_ENTRY_MAP.size() + " clan shop items successfully.");
    }

    @Override
    public void getSpecialItemData() {
        gameDao.getAllSpecialItem();
        ServerManager.getInstance().getLog().success("Loaded " + SpecialItemRepository.SPECIAL_ITEM_ENTRIES.size() + " special items successfully.");
    }
}
