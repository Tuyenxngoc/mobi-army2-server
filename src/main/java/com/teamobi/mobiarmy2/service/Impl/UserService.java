package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.CmdClient;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ClanManager;
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
        if (user.isNotWaiting()) {
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
        if (user.isNotWaiting()) {
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
                ClanManager.getInstance().getClanDao().gopLuong(user.getClanId(), soluong);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (user.isNotWaiting()) {
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            dis.readByte();
            dis.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
