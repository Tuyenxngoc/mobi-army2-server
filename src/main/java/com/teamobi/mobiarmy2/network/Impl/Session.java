package com.teamobi.mobiarmy2.network.Impl;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.server.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Session implements ISession {

    private static final byte[] KEY = "bth.army2.ml".getBytes();
    private static final int TIMEOUT_DURATION = 180_000;
    private static final List<Byte> WHITE_LIST_CMD = List.of(
            (byte) -27,
            (byte) 1,
            (byte) 58,
            (byte) 114,
            (byte) 127
    );

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private boolean sendKeyComplete;
    private byte curR;
    private byte curW;

    private final Sender sender = new Sender();
    private final IMessageHandler messageHandler;

    private Thread collectorThread;
    private Thread sendThread;

    private final long sessionId;
    private final String IPAddress;
    private String platform;
    private String version;
    private byte provider;
    private final User user;

    public Session(long sessionId, Socket socket) throws IOException {
        this.sessionId = sessionId;
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.IPAddress = socket.getInetAddress().getHostName();

        this.user = new User(this);
        this.messageHandler = new MessageHandler(user.getUserService());

        this.sendThread = new Thread(sender);
        this.collectorThread = new Thread(new MessageCollector());
        this.collectorThread.start();
    }

    private boolean isSendKeyComplete() {
        return sendKeyComplete;
    }

    @Override
    public void sendMessage(Message message) {
        sender.addMessage(message);
    }

    @Override
    public void close() {
        try {
            ServerManager serverManager = ServerManager.getInstance();
            serverManager.logger().logMessage("Close " + this);
            serverManager.disconnect(this);
            cleanNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendKeys() {
        try {
            Message ms = new Message(-27);
            DataOutputStream ds = ms.writer();
            ds.writeByte(KEY.length);
            ds.writeByte(KEY[0]);
            for (int i = 1; i < KEY.length; i++) {
                ds.writeByte(KEY[i] ^ KEY[i - 1]);
            }
            ds.flush();
            doSendMessage(ms);
            sendKeyComplete = true;
            sendThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void doSendMessage(Message m) {
        byte[] data = m.getData();
        try {
            if (sendKeyComplete) {
                dos.writeByte(writeKey(m.getCommand()));
            } else {
                dos.writeByte(m.getCommand());
            }
            if (data != null) {
                int size = data.length;
                if (m.getCommand() == 90) {
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
            m.cleanup();
        } catch (Exception e) {
            closeMessage();
        }
    }

    private byte readKey(byte b) {
        byte i = (byte) ((KEY[curR++] & 0xff) ^ (b & 0xff));
        if (curR >= KEY.length) {
            curR %= KEY.length;
        }
        return i;
    }

    private byte writeKey(byte b) {
        byte i = (byte) ((KEY[curW++] & 0xff) ^ (b & 0xff));
        if (curW >= KEY.length) {
            curW %= KEY.length;
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
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeMessage() {
        if (isSendKeyComplete()) {
            close();
        }
    }

    private class Sender implements Runnable {

        private final ArrayList<Message> sendingMessage = new ArrayList<>();

        public void addMessage(Message message) {
            sendingMessage.add(message);
        }

        @Override
        public void run() {
            try {
                while (Session.this.isSendKeyComplete()) {
                    while (sendingMessage.size() > 0 && Session.this.dis != null) {
                        Message message = sendingMessage.remove(0);
                        ServerManager.getInstance().logger().logMessage("Send mss " + message.getCommand() + " to " + Session.this);
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
                    ServerManager.getInstance().logger().logMessage(Session.this + " send mss " + message.getCommand());
                    if (!Session.this.user.isLogged() && requiresAuthentication(message)) {
                        ServerManager.getInstance().logger().logMessage("Unauthorized access attempted. Command requires authentication.");
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
