package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.CmdClient;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.BangXHManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserService implements IUserService {

    private User user;
    private final UserDao userDao = new UserDao();

    public UserService(User user) {
        this.user = user;
    }

    private void handleUserNotLoggedIn() {
        user.getSession().close();
    }

    private void sendMessageLoginFail(String message) {
        try {
            Message ms = new Message(4);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendKeys() throws IOException {
        user.getSession().sendKeys();
    }

    @Override
    public void sendServerMessage(String message) {
        try {
            Message ms = new Message(CmdClient.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void giaHanDo(Message ms) {

    }

    @Override
    public void nhiemVuView(Message ms) {

    }

    @Override
    public void login(Message ms) {
        if (user.isLogged()) {
            return;
        }

        if (!BangXHManager.getInstance().isComplete) {
            sendMessageLoginFail(GameString.getNotFinishedLoadingRanking());
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            String username = dis.readUTF().trim();
            String password = dis.readUTF().trim();
            String version = dis.readUTF();

            if (!username.matches(CommonConstant.ALPHANUMERIC_PATTERN) || !password.matches(CommonConstant.ALPHANUMERIC_PATTERN)) {
                sendMessageLoginFail(GameString.reg_Error1());
                return;
            }

            ServerManager.getInstance().logger().logMessage("Client: " + user.getSession().getSessionId() + " name: " + username + " pass: " + password + " version: " + version);

            User userFound = userDao.findByUsernameAndPassword(username, password);
            if (userFound == null) {
                sendServerMessage(GameString.loginPassFail());
                return;
            }
            if (userFound.isLock()) {
                sendServerMessage(GameString.loginLock());
                return;
            }
            if (userFound.isActive()) {
                sendServerMessage(GameString.loginActive());
                return;
            }

            ISession session = user.getSession();
            session.setVersion(version);

            user = userFound;
            user.setSession(session);
            user.setLogged(true);

            sendLoginSuccess();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLoginSuccess() {
        try {
            Message ms = new Message(CmdClient.LOGIN_SUCESS);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getId());
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.writeByte(user.getNhanVat());
            ds.writeShort(user.getClanId());
            ds.writeByte(0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gopClan(Message ms) {

    }

    @Override
    public void getVersionCode(Message ms) {
        try {
            String platform = ms.reader().readUTF();
            user.getSession().setPlatform(platform);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getProvider(Message ms) {
        try {
            byte provider = ms.reader().readByte();
            user.getSession().setProvider(provider);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hopTrangBi(Message ms) {

    }

    @Override
    public void buyItem(Message ms) {

    }
}
