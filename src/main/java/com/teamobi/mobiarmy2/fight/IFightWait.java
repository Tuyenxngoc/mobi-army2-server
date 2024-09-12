package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;

public interface IFightWait {
    void leaveTeam(int playerId);

    void chatMessage(int playerId, String message);

    void kickPlayer(int playerId, int playerId1);

    void setReady(boolean ready, int playerId);

    void setPassRoom(String password, int playerId);

    void setMoney(int xu, int playerId);

    void startGame(int playerId);

    FightManager getFightManager();

    User getUserByPlayerId(int playerId);

    void setRoomName(int playerId, String name);

    void setMaxPlayers(int playerId, byte maxPlayers);

    void setItems(int playerId, byte[] items);

    void changeTeam(User user);

    void setMap(int playerId, byte mapId);

    void findPlayer(int playerId);

    void inviteToRoom(int playerId);

    void decreaseContinuousLevel();

    boolean isContinuous();

    byte getMapId();

    int getNumPlayers();

    byte getContinuousLevel();

    void setContinuousLevel(byte b);

    void setMapId(byte continuousMap);

    int getMoney();

    void setStarted(boolean b);

    void fightComplete();

    byte getType();

    User[] getUsers();

    byte[][] getItems();

    boolean isStarted();

    void sendToTeam(IMessage message);
}
