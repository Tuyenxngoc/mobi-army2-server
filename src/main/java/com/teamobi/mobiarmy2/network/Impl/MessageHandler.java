package com.teamobi.mobiarmy2.network.Impl;

import com.teamobi.mobiarmy2.constant.CmdClient;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;

public class MessageHandler implements IMessageHandler {

    private final IUserService userService;

    public MessageHandler(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void onMessage(Message ms) {
        try {
            switch (ms.getCommand()) {
                case CmdClient.GET_KEY-> userService.sendKeys();
                case CmdClient.GET_MORE_DAY -> userService.giaHanDo(ms);
                case CmdClient.MISSISON -> userService.nhiemVuView(ms);
                default -> ServerManager.getInstance().logger().logWarning("Command " + ms.getCommand() + " is not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
