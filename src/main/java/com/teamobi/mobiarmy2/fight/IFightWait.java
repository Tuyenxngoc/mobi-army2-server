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

    User getUserByUserId(int userId);

    byte[] getItems(byte i);

    User[] getUsers();

    IFightManager getFightManager();

    void fightComplete();

    void startGame(int userId);

    void sendToTeam(IMessage message);

    void leaveTeam(int userId);

    void chatMessage(int userId, String message);

    void kickPlayer(int userId, int targetUserId);

    void handleKickPlayer(int targetUserId, int index, String message);

    void decreaseContinuousLevel();

    void setReady(boolean ready, int userId);

    void setPassRoom(String password, int userId);

    void setMoney(int xu, int userId);

    void setRoomName(int userId, String name);

    void setMaxPlayers(int userId, byte maxPlayers);

    void setItems(int userId, byte[] items);

    void changeTeam(User user);

    void setMap(int userId, byte mapId);

    void findPlayer(int userId);

    void inviteToRoom(int userId);

    void sendInfo(User user);

    void addUser(User user) throws IOException;
}
