package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.model.User;

public interface IGameService {

    void setCacheMaps();

    void setCacheCharacters();

    void setCacheCaptionLevels();

    void setCachePlayerImages();

    void setCacheMapIcons();

    void getItemData();

    void setDefaultNvData();

    void getClanShopData();

    void getSpecialItemData();

    void sendNVData(User user, IServerConfig config);

    void sendRoomInfo(User user, IServerConfig config);

    void sendMapCollisionInfo(User user, IServerConfig config);

    void getFormulaData();

    void getPaymentData();

    void getMissionData();
}
