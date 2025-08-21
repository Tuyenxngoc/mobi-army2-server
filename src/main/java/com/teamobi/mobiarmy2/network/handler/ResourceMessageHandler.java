package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResourceMessageHandler extends BaseMessageHandler {
    public ResourceMessageHandler(Session session) {
        super(session);
    }

    public void getMoreGame() throws IOException {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);

        Message ms = new Message(Cmd.MORE_GAME);
        DataOutputStream ds = ms.writer();
        ds.writeUTF(serverConfig.getDownloadTitle());
        ds.writeUTF(serverConfig.getDownloadInfo());
        ds.writeUTF(serverConfig.getDownloadUrl());
        ds.flush();
        sendMessage(ms);
    }

    public void getBigImage(Message ms) throws IOException {
        int id = ms.reader().readByte();
        ms = new Message(Cmd.GET_BIG_IMAGE);
        DataOutputStream ds = ms.writer();
        ds.writeByte(id);
        byte[] file = Utils.getFile(String.format(GameConstants.BIG_IMAGE_PATH, id));
        if (file != null) {
            ds.writeShort(file.length);
            ds.write(file);
        } else {
            ds.writeShort(0);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void getMaterialIconMessage(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte typeIcon = dis.readByte();
        byte iconId = dis.readByte();

        byte indexIcon = 0;
        byte[] data = null;
        switch (typeIcon) {
            case 0, 1 -> data = Utils.getFile(String.format(GameConstants.ITEM_ICON_PATH, iconId));
            case 2 -> data = Utils.getFile(String.format(GameConstants.MAP_ICON_PATH, iconId));
            case 3, 4 -> {
                indexIcon = dis.readByte();
                data = Utils.getFile(String.format(GameConstants.ITEM_ICON_PATH, iconId));
            }
        }
        if (data == null) {
            data = new byte[0];
        }

        ms = new Message(Cmd.MATERIAL_ICON);
        DataOutputStream ds = ms.writer();
        ds.writeByte(typeIcon);
        ds.writeByte(iconId);
        ds.writeShort(data.length);
        ds.write(data);
        if (typeIcon == 3 || typeIcon == 4) {
            ds.writeByte(indexIcon);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void getFilePack(Message ms) throws IOException {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        byte version = dis.readByte();

        switch (type) {
            case 1 -> {
                ms = new Message(Cmd.GET_FILEPACK);
                DataOutputStream ds = ms.writer();
                ds.writeByte(type);
                ds.writeByte(serverConfig.getIconVersion2());
                if (version != serverConfig.getIconVersion2()) {
                    byte[] ab = Utils.getFile(GameConstants.ICON_CACHE_NAME);
                    if (ab == null) {
                        return;
                    }
                    ds.writeShort(ab.length);
                    ds.write(ab);
                }
                ds.flush();
                sendMessage(ms);
            }

            case 2 -> {
                ms = new Message(Cmd.GET_FILEPACK);
                DataOutputStream ds = ms.writer();
                ds.writeByte(type);
                ds.writeByte(serverConfig.getValuesVersion2());
                if (version != serverConfig.getValuesVersion2()) {
                    byte[] ab = Utils.getFile(GameConstants.MAP_CACHE_NAME);
                    if (ab == null) {
                        return;
                    }
                    ds.writeShort(ab.length);
                    ds.write(ab);
                }
                ds.flush();
                sendMessage(ms);
            }
            case 3 -> {
                ms = new Message(Cmd.GET_FILEPACK);
                DataOutputStream ds = ms.writer();
                ds.writeByte(type);
                ds.writeByte(serverConfig.getPlayerVersion2());
                if (version != serverConfig.getPlayerVersion2()) {
                    byte[] ab = Utils.getFile(GameConstants.PLAYER_CACHE_NAME);
                    if (ab == null) {
                        return;
                    }
                    ds.writeShort(ab.length);
                    ds.write(ab);
                }
                ds.flush();
                sendMessage(ms);
            }
            case 4 -> {
                ms = new Message(Cmd.GET_FILEPACK);
                DataOutputStream ds = ms.writer();
                ds.writeByte(type);
                ds.writeByte(serverConfig.getEquipVersion2());
                if (version != serverConfig.getEquipVersion2()) {
                    byte[] ab = Utils.getFile(GameConstants.EQUIP_CACHE_NAME);
                    if (ab == null) {
                        return;
                    }
                    ds.writeInt(ab.length);
                    ds.write(ab);
                }
                ds.flush();
                sendMessage(ms);
            }
            case 5 -> {
                ms = new Message(Cmd.GET_FILEPACK);
                DataOutputStream ds = ms.writer();
                ds.writeByte(type);
                ds.writeByte(serverConfig.getLevelCVersion2());
                if (version != serverConfig.getLevelCVersion2()) {
                    byte[] ab = Utils.getFile(GameConstants.LEVEL_CACHE_NAME);
                    if (ab == null) {
                        return;
                    }
                    ds.writeShort(ab.length);
                    ds.write(ab);
                }
                ds.flush();
                sendMessage(ms);
            }
            case 6 -> {
                sendCharacterInfo();
                sendInventoryInfo();
            }
        }
    }
}
