package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.service.IUserService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserService implements IUserService {

    private final User user;
    private final UserDao userDao;

    public UserService(User user, UserDao userDao) {
        this.user = user;
        this.userDao = userDao;
    }

    @Override
    public void sendKeys() throws IOException {
        user.getSession().sendKeys();
    }

    @Override
    public void sendServerMessage(String message) {
        try {
            Message ms = new Message(45);
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
        if (!user.isWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int idKey = dis.readInt();
            if (action == 0) {

            } else if (action == 1) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nhiemVuView(Message ms) {
        if (!user.isWaiting()) {
            return;
        }
    }

    @Override
    public void login(Message ms) {
        if (user.isLogged()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            String username = dis.readUTF();
            String password = dis.readUTF();
            String version = dis.readUTF();

            User userFound = userDao.findByUsernameAndPassword(username, password);
            if (userFound == null) {
                sendServerMessage(GameString.loginPassFail());
                return;
            }
            this.user.setLogged(true);
            this.user.setId(userFound.getId());
            this.user.setUsername(username);
            this.user.setPassword(password);

            this.user.getSession().setVersion(version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gopClan(Message ms) {
        if (user.getClanId() <= 0) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            int soluong = dis.readInt();

            if (soluong <= 0) {
                return;
            }

            if (type == 0) {
                if (soluong > user.getXu()) {
                    return;
                }
                ClanManager.getInstance().getClanDao().gopXu(user.getClanId(), soluong);
            } else if (type == 1) {
                if (soluong > user.getLuong()) {
                    return;
                }
                ClanManager.getInstance().getClanDao().gopXu(user.getClanId(), soluong);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getVersionCode(Message ms) {
        try {
            user.getSession().setPlatform(ms.reader().readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
