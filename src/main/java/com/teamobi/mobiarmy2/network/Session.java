package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.handler.*;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private final MessageSender messageSender = new MessageSender();
    private final MessageRouter messageRouter;
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
    @Getter
    private String version;
    @Setter
    @Getter
    private byte provider = -1;
    @Setter
    @Getter
    private String agent;

    public Session(long sessionId, Socket socket) throws IOException {
        this.sessionId = sessionId;
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.IPAddress = socket.getInetAddress().getHostAddress();
        this.sessionKey = generateSessionKey();
        ApplicationContext context = ApplicationContext.getInstance();
        AuthMessageHandler loginService = new AuthMessageHandler(
                this,
                context.getBean(LoginRateLimiterService.class),
                context.getBean(UserDAO.class),
                context.getBean(AccountDAO.class),
                context.getBean(UserCharacterDAO.class)
        );
        this.messageRouter = new MessageRouter(loginService);
        this.sendThread = new Thread(messageSender, sessionId + "_send");
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
        messageSender.enqueue(message);
    }

    public void close() {
        try {
            if (user != null && user.isLogged()) {
                messageRouter.getAuthMessageHandler().handleUserLogoutCleanup();
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
        if (user != null && user.getUsername() != null) {
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

    public void initMessageHandlers() {
        ApplicationContext context = ApplicationContext.getInstance();

        ClanMessageHandler clanMessageHandler = new ClanMessageHandler(this, context.getBean(ClanDAO.class));
        FriendMessageHandler friendMessageHandler = new FriendMessageHandler(this, context.getBean(UserDAO.class));
        ShopMessageHandler shopMessageHandler = new ShopMessageHandler(this, context.getBean(UserCharacterDAO.class));
        ResourceMessageHandler resourceService = new ResourceMessageHandler(this);
        MissionMessageHandler missionMessageHandler = new MissionMessageHandler(this);
        FormulaMessageHandler formulaMessageHandler = new FormulaMessageHandler(this);
        RoomMessageHandler roomMessageHandler = new RoomMessageHandler(this);
        FightWaitMessageHandler fightWaitMessageHandler = new FightWaitMessageHandler(this, context.getBean(UserDAO.class));
        FightManagerMessageHandler fightManagerMessageHandler = new FightManagerMessageHandler(this);
        InventoryMessageHandler inventoryMessageHandler = new InventoryMessageHandler(this);
        LeaderboardMessageHandler leaderboardMessageHandler = new LeaderboardMessageHandler(this, context.getBean(LeaderboardService.class));
        GiftBoxMessageHandler giftBoxMessageHandler = new GiftBoxMessageHandler(this);
        SpinMessageHandler spinMessageHandler = new SpinMessageHandler(this);
        PaymentMessageHandler paymentMessageHandler = new PaymentMessageHandler(this, context.getBean(GiftCodeDAO.class), context.getBean(UserGiftCodeDAO.class));
        CharacterMessageHandler characterMessageHandler = new CharacterMessageHandler(this);

        messageRouter.setClanMessageHandler(clanMessageHandler);
        messageRouter.setFriendMessageHandler(friendMessageHandler);
        messageRouter.setShopMessageHandler(shopMessageHandler);
        messageRouter.setResourceMessageHandler(resourceService);
        messageRouter.setMissionMessageHandler(missionMessageHandler);
        messageRouter.setFormulaMessageHandler(formulaMessageHandler);
        messageRouter.setRoomMessageHandler(roomMessageHandler);
        messageRouter.setFightWaitMessageHandler(fightWaitMessageHandler);
        messageRouter.setFightManagerMessageHandler(fightManagerMessageHandler);
        messageRouter.setInventoryMessageHandler(inventoryMessageHandler);
        messageRouter.setLeaderboardMessageHandler(leaderboardMessageHandler);
        messageRouter.setGiftBoxMessageHandler(giftBoxMessageHandler);
        messageRouter.setSpinMessageHandler(spinMessageHandler);
        messageRouter.setPaymentMessageHandler(paymentMessageHandler);
        messageRouter.setCharacterMessageHandler(characterMessageHandler);
    }

    class MessageSender implements Runnable {
        private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

        public void enqueue(Message message) {
            if (message == null) {
                return;
            }
            queue.offer(message);
        }

        @Override
        public void run() {
            try {
                while (isSendKeyComplete()) {
                    if (dos != null) {
                        Message message = queue.take();
                        log.info("   Send mss {} to {}", Cmd.getCmdNameByValue(message.getCommand()), Session.this);
                        doSendMessage(message);
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
                while (dis != null) {
                    socket.setSoTimeout(TIMEOUT_DURATION);
                    Message message = readMessage();
                    if (message == null) {
                        break;
                    }
                    log.info("{} send mss {}", Session.this, Cmd.getCmdNameByValue(message.getCommand()));
                    if ((user == null || !user.isLogged()) && requiresAuthentication(message)) {
                        message.cleanup();
                        break;
                    }
                    messageRouter.onMessage(message);
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
                byte cmd = dis.readByte();
                if (sendKeyComplete) {
                    cmd = readKey(cmd);
                }
                int size;
                if (sendKeyComplete) {
                    byte b1 = dis.readByte();
                    byte b2 = dis.readByte();
                    size = ((readKey(b1) & 0xff) << 8) | (readKey(b2) & 0xff);
                } else {
                    size = dis.readUnsignedShort();
                }
                byte[] data = new byte[size];
                int len = 0;
                int byteRead = 0;
                while (len != -1 && byteRead < size) {
                    len = dis.read(data, byteRead, size - byteRead);
                    if (len > 0) {
                        byteRead += len;
                    }
                }
                if (sendKeyComplete) {
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
