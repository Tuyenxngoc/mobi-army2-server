package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.Room;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;

import java.io.IOException;

/**
 * @author tuyen
 */
public interface IFightWait {

    int getMaxSetPlayers();

    boolean isStarted();

    boolean isContinuous();

    boolean isPassSet();

    boolean isFightWaitInvalid();

    byte getNumPlayers();

    byte getId();

    byte getMapId();

    byte getRoomType();

    String getName();

    String getPassword();

    int getMoney();

    Room getRoom();

    User getUserByPlayerId(int playerId);

    byte[] getItems(byte i);

    User[] getUsers();

    IFightManager getFightManager();

    void fightComplete();

    void startGame(int playerId);

    void sendToTeam(IMessage message);

    void leaveTeam(int playerId);

    void chatMessage(int playerId, String message);

    void kickPlayer(int playerId, int playerId1);

    void decreaseContinuousLevel();

    void setReady(boolean ready, int playerId);

    void setPassRoom(String password, int playerId);

    void setMoney(int xu, int playerId);

    void setRoomName(int playerId, String name);

    void setMaxPlayers(int playerId, byte maxPlayers);

    void setItems(int playerId, byte[] items);

    void changeTeam(User user);

    void setMap(int playerId, byte mapId);

    void findPlayer(int playerId);

    void inviteToRoom(int playerId);

    void sendInfo(User user);

    void addUser(User user) throws IOException;
}
