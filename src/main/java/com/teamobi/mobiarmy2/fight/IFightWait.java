package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;

/**
 * @author tuyen
 */
public interface IFightWait {

    void sendToTeam(IMessage message);

    boolean isStarted();

    byte getNumPlayers();

    User[] getUsers();

    void leaveTeam(int playerId);

    void chatMessage(int playerId, String message);

    void kickPlayer(int playerId, int playerId1);

    void setReady(boolean ready, int playerId);

    void setPassRoom(String password, int playerId);

    void setMoney(int xu, int playerId);

    void startGame(int playerId);

    byte getMapId();

    User getUserByPlayerId(int playerId);

    IFightManager getFightManager();

    void setRoomName(int playerId, String name);

    void setMaxPlayers(int playerId, byte maxPlayers);

    void setItems(int playerId, byte[] items);

    void changeTeam(User user);

    void setMap(int playerId, byte mapId);

    void findPlayer(int playerId);

    void inviteToRoom(int playerId);

    byte[] getItems(byte i);

    byte getRoomType();

    int getMoney();

}
