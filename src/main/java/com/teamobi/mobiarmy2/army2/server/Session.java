package com.teamobi.mobiarmy2.army2.server;

import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Session implements ISession {

    private static final byte[] KEY = "bth.army2.ml".getBytes();
    private static final int TIMEOUT_DURATION = 180000;

    public Socket socket;
    public DataInputStream dis;
    public DataOutputStream dos;

    public int id;
    public User user;

    protected boolean sendKeyComplete, login;
    private byte curR;
    private byte curW;

    protected final Object obj = new Object();
    private final Sender sender = new Sender();
    private final IMessageHandler messageHandler;

    private Thread collectorThread;
    protected Thread sendThread;

    protected String platform;
    public String version;
    public String IPAddress;

    public Session(Socket socket, int id) throws IOException {
        this.socket = socket;
        this.id = id;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());

        this.IPAddress = socket.getInetAddress().getHostName();
        this.messageHandler = new MessageHandler(this);

        this.sendThread = new Thread(sender);
        this.collectorThread = new Thread(new MessageCollector());
        this.collectorThread.start();
    }

    @Override
    public boolean isSendKeyComplete() {
        return sendKeyComplete;
    }

    @Override
    public void sendMessage(Message message) {
        sender.addMessage(message);
    }

    @Override
    public void close() {
        try {
            if (user != null) {
                user.close();
            }
            ServerManager.disconnect(this);
            cleanNetwork();
            user = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        if (user != null) {
            return user.toString();
        }
        return "Client " + id;
    }

    @Override
    public void sendKeys() throws IOException {
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
            ServerManager.log(e);
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
            login = false;
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

    public void loginMessage(Message ms) throws IOException {
        if (login) {
            return;
        }
        if (!BangXHManager.isComplete) {
            ms = new Message(4);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(GameString.notFinishedLoadingRanking());
            ds.flush();
            sendMessage(ms);
            return;
        }
        String userS = ms.reader().readUTF().trim();
        String pass = ms.reader().readUTF().trim();
        version = ms.reader().readUTF().trim();
        Pattern p = Pattern.compile("^[a-zA-Z0-9]+$");
        Matcher m1 = p.matcher(userS);
        Matcher m2 = p.matcher(pass);
        if (!m1.find() || !m2.find()) {
            ms = new Message(4);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(GameString.reg_Error1());
            ds.flush();
            sendMessage(ms);
            return;
        }
        System.out.println("Client: " + id + " name: " + userS + " pass: " + pass + " version: " + version);
        User us = User.login(this, userS, pass);
        if (us != null) {
            System.out.println("Login Success!");
            login = true;
            user = us;
            ServerManager.sendNVData(user);
            ServerManager.sendRoomInfo(user);
            ServerManager.sendMapCollisionInfo(user);
        } else {
            System.out.println("Login Failse!");
            login = false;
        }
    }

    public void closeMessage() {
        if (isSendKeyComplete()) {
            close();
        }
    }

    public void regMessage(Message ms) throws IOException {
        String name = ms.reader().readUTF();
        String pass = ms.reader().readUTF();
        ServerManager.log("RegMessage: name=" + name + ", pass=" + pass);

        ms = new Message(4);
        DataOutputStream ds = ms.writer();
        ds.writeUTF("Vui lòng truy cập trang chủ để đăng ký");
        ds.flush();
        sendMessage(ms);
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
                        ServerManager.log("Send mss " + message.getCommand() + " to " + Session.this);
                        Session.this.doSendMessage(message);
                    }
                    try {
                        Thread.sleep(10);
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
                    ServerManager.log(Session.this + " send mss " + message.getCommand());
                    Session.this.messageHandler.onMessage(message);
                    message.cleanup();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                Session.this.closeMessage();
                ServerManager.log(e);
                return null;
            }
        }
    }
}
