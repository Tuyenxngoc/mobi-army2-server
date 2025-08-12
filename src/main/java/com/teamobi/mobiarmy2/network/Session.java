package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Session {
    private static final int TIMEOUT_DURATION = 180_000;
    private static final List<Byte> WHITE_LIST_CMD = List.of(
            (byte) -27,
            (byte) 1,
            (byte) 58,
            (byte) 114,
            (byte) 121,
            (byte) 127
    );

    private final byte[] sessionKey;
    private final Sender sender = new Sender();
    private MessageHandler messageHandler;
    private final long sessionId;
    @Getter
    private final String IPAddress;
    @Getter
    @Setter
    private User user;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    @Getter
    private boolean sendKeyComplete;
    private byte curR;
    private byte curW;
    private Thread collectorThread;
    private Thread sendThread;
    @Setter
    private String platform;
    @Setter
    private String version;
    @Setter
    private byte provider;

    public Session(long sessionId, Socket socket) throws IOException {
        this.sessionId = sessionId;
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.IPAddress = socket.getInetAddress().getHostAddress();
        this.sessionKey = generateSessionKey();
        ApplicationContext context = ApplicationContext.getInstance();
        LoginService loginService = new LoginService(
                this,
                context.getBean(LoginRateLimiterService.class),
                context.getBean(UserDAO.class),
                context.getBean(AccountDAO.class),
                context.getBean(UserCharacterDAO.class)
        );
        this.messageHandler = new MessageHandler(loginService);
        initializeThreads();
    }

    private void initializeThreads() {
        this.sendThread = new Thread(sender, sessionId + "_send");
        this.collectorThread = new Thread(new MessageCollector(), sessionId + "_collector");
        this.collectorThread.start();
    }

    private byte[] generateSessionKey() {
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return key;
    }

    public void sendMessage(Message message) {
        sender.addMessage(message);
    }

    public void close() {
        try {
            if (user.isLogged()) {
                user.getUserService().handleLogout();
            }

            ApplicationContext.getInstance()
                    .getBean(ServerManager.class)
                    .disconnect(this);
            cleanNetwork();

            log.info("Close {}", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendKeys() {
        try {
            Message ms = new Message(Cmd.GET_KEY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(sessionKey.length);
            ds.writeByte(sessionKey[0]);
            for (int i = 1; i < sessionKey.length; i++) {
                ds.writeByte(sessionKey[i] ^ sessionKey[i - 1]);
            }
            ds.flush();
            doSendMessage(ms);
            sendKeyComplete = true;
            sendThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        if (user.getUsername() != null) {
            return user.getUsername();
        }
        return "Client " + sessionId;
    }

    protected synchronized void doSendMessage(Message message) {
        byte[] data = message.getData();
        try {
            if (sendKeyComplete) {
                dos.writeByte(writeKey(message.getCommand()));
            } else {
                dos.writeByte(message.getCommand());
            }
            if (data != null) {
                int size = data.length;
                if (message.getCommand() == 90) {
                    dos.writeInt(size);
                } else {
                    if (sendKeyComplete) {
                        dos.writeByte(writeKey((byte) (size >> 8)));
                        dos.writeByte(writeKey((byte) (size & 0xFF)));
                    } else {
                        dos.writeShort(size);
                    }
                    if (sendKeyComplete) {
                        for (int i = 0; i < data.length; i++) {
                            data[i] = writeKey(data[i]);
                        }
                    }
                }
                dos.write(data);
            } else {
                dos.writeShort(0);
            }
            dos.flush();
            message.cleanup();
        } catch (Exception e) {
            closeMessage();
        }
    }

    private byte readKey(byte b) {
        byte i = (byte) ((sessionKey[curR++] & 0xff) ^ (b & 0xff));
        if (curR >= sessionKey.length) {
            curR %= (byte) sessionKey.length;
        }
        return i;
    }

    private byte writeKey(byte b) {
        byte i = (byte) ((sessionKey[curW++] & 0xff) ^ (b & 0xff));
        if (curW >= sessionKey.length) {
            curW %= (byte) sessionKey.length;
        }
        return i;
    }

    private void cleanNetwork() {
        curR = 0;
        curW = 0;
        try {
            sendKeyComplete = false;
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (dos != null) {
                dos.close();
                dos = null;
            }
            if (dis != null) {
                dis.close();
                dis = null;
            }
            sendThread = null;
            collectorThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeMessage() {
        if (isSendKeyComplete()) {
            close();
        }
    }

    public void initService() {
        ApplicationContext context = ApplicationContext.getInstance();

        UserService userService = new UserService(
                this,
                context.getBean(ServerConfig.class),
                context.getBean(LeaderboardService.class),
                context.getBean(LoginRateLimiterService.class),
                context.getBean(UserDAO.class),
                context.getBean(AccountDAO.class),
                context.getBean(GiftCodeDAO.class),
                context.getBean(UserGiftCodeDAO.class),
                context.getBean(UserCharacterDAO.class)
        );

        ClanService clanService = new ClanService(
                this,
                context.getBean(ClanDAO.class)
        );

        FriendService friendService = new FriendService(
                this,
                context.getBean(UserDAO.class)
        );

        ShopService shopService = new ShopService(
                this,
                context.getBean(UserCharacterDAO.class)
        );

        ResourceService resourceService = new ResourceService(
                this
        );

        this.messageHandler.setUserService(userService);
        this.messageHandler.setClanService(clanService);
        this.messageHandler.setFriendService(friendService);
        this.messageHandler.setShopService(shopService);
        this.messageHandler.setResourceService(resourceService);
    }

    class Sender implements Runnable {
        private final ArrayList<Message> sendingMessage = new ArrayList<>();

        public void addMessage(Message message) {
            sendingMessage.add(message);
        }

        @Override
        public void run() {
            try {
                while (Session.this.isSendKeyComplete()) {
                    while (!sendingMessage.isEmpty() && Session.this.dis != null) {
                        Message message = sendingMessage.removeFirst();
                        log.info("   Send mss {} to {}", Cmd.getCmdNameByValue(message.getCommand()), Session.this);
                        Session.this.doSendMessage(message);
                    }
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MessageCollector implements Runnable {
        @Override
        public void run() {
            try {
                while (Session.this.dis != null) {
                    Session.this.socket.setSoTimeout(TIMEOUT_DURATION);
                    Message message = readMessage();
                    if (message == null) {
                        break;
                    }
                    log.info("{} send mss {}", Session.this, Cmd.getCmdNameByValue(message.getCommand()));
                    if (!Session.this.user.isLogged() && requiresAuthentication(message)) {
                        message.cleanup();
                        break;
                    }
                    Session.this.messageHandler.onMessage(message);
                    message.cleanup();
                }
                closeMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean requiresAuthentication(Message message) {
            Byte cmd = message.getCommand();
            return !WHITE_LIST_CMD.contains(cmd);
        }

        private Message readMessage() {
            try {
                byte cmd = Session.this.dis.readByte();
                if (Session.this.sendKeyComplete) {
                    cmd = Session.this.readKey(cmd);
                }
                int size;
                if (Session.this.sendKeyComplete) {
                    byte b1 = Session.this.dis.readByte();
                    byte b2 = Session.this.dis.readByte();
                    size = ((Session.this.readKey(b1) & 0xff) << 8) | (Session.this.readKey(b2) & 0xff);
                } else {
                    size = Session.this.dis.readUnsignedShort();
                }
                byte[] data = new byte[size];
                int len = 0;
                int byteRead = 0;
                while (len != -1 && byteRead < size) {
                    len = Session.this.dis.read(data, byteRead, size - byteRead);
                    if (len > 0) {
                        byteRead += len;
                    }
                }
                if (Session.this.sendKeyComplete) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = readKey(data[i]);
                    }
                }
                return new Message(cmd, data);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
