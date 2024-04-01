package com.teamobi.mobiarmy2.network.Impl;

import com.teamobi.mobiarmy2.constant.CmdClient;
import com.teamobi.mobiarmy2.constant.CmdServer;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;

public class MessageHandler implements IMessageHandler {

    private final IUserService userService;

    public MessageHandler(IUserService userService) {
        this.userService = userService;
    }

    public MessageHandler(User user) {
        this(new UserService(user));
    }

    @Override
    public void onMessage(Message ms) {
        try {
            switch (ms.getCommand()) {
                case CmdClient.GET_KEY -> userService.sendKeys();

                case CmdClient.GET_MORE_DAY -> userService.giaHanDo(ms);

                case CmdClient.MISSISON -> userService.nhiemVuView(ms);

                case CmdClient.CLAN_MONEY -> userService.gopClan(ms);

                case CmdClient.FOMULA -> userService.hopTrangBi(ms);

                case CmdServer.LOGIN -> userService.login(ms);

                case CmdServer.SET_PROVIDER -> userService.getProvider(ms);

                case CmdClient.VERSION_CODE -> userService.getVersionCode(ms);

                default -> ServerManager.getInstance().logger().logWarning("Command " + ms.getCommand() + " is not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
