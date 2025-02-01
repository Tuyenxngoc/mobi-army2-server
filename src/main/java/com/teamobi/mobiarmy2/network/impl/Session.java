package com.teamobi.mobiarmy2.network.impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class Session implements ISession {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);

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
    private final IMessageHandler messageHandler;
    private final long sessionId;
    private final String IPAddress;
    private final User user;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean sendKeyComplete;
    private byte curR;
    private byte curW;
    private Thread collectorThread;
    private Thread sendThread;
    private String platform;
    private String version;
    private byte provider;

    public Session(long sessionId, Socket socket) throws IOException {
        this.sessionId = sessionId;
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.IPAddress = socket.getInetAddress().getHostAddress();

        this.user = new User(this);
        this.messageHandler = new MessageHandler(user.getUserService());

        this.sessionKey = generateSessionKey();
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

    private boolean isSendKeyComplete() {
        return sendKeyComplete;
    }

    @Override
    public void sendMessage(IMessage message) {
        sender.addMessage(message);
    }

    @Override
    public void close() {
        try {
            if (user.isLogged()) {
                user.getUserService().handleLogout();
            }

            ServerManager.getInstance().disconnect(this);
            cleanNetwork();

            logger.info("Close {}", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getIPAddress() {
        return IPAddress;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public byte getProvider() {
        return provider;
    }

    @Override
    public void setProvider(byte provider) {
        this.provider = provider;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void sendKeys() {
        try {
            IMessage ms = new Message(Cmd.GET_KEY);
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

    protected synchronized void doSendMessage(IMessage message) {
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

    class Sender implements Runnable {

        private final ArrayList<IMessage> sendingMessage = new ArrayList<>();

        public void addMessage(IMessage message) {
            sendingMessage.add(message);
        }

        @Override
        public void run() {
            try {
                while (Session.this.isSendKeyComplete()) {
                    while (!sendingMessage.isEmpty() && Session.this.dis != null) {
                        IMessage message = sendingMessage.removeFirst();
                        logger.info("   Send mss {} to {}", Cmd.getCmdNameByValue(message.getCommand()), Session.this);
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
                    IMessage message = readMessage();
                    if (message == null) {
                        break;
                    }
                    logger.info("{} send mss {}", Session.this, Cmd.getCmdNameByValue(message.getCommand()));
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

        private boolean requiresAuthentication(IMessage message) {
            Byte cmd = message.getCommand();
            return !WHITE_LIST_CMD.contains(cmd);
        }

        private IMessage readMessage() {
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
