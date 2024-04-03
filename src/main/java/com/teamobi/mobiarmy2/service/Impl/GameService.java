package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.service.IGameService;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

            int numMap = MapData.entries.size();
            ds.writeByte(numMap);
            System.out.println("Init map entry numMap=" + numMap);
            for (int i = 0; i < numMap; i++) {
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
            Utils.saveFile("cache/valuesdata2", ab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheCharacters() {
        gameDao.getAllCharacterData();

    }

    @Override
    public void setCacheCaptionLevels() {

    }

    @Override
    public void setCachePlayerImages() {

    }

    @Override
    public void setCacheMapIcons() {

    }

}
