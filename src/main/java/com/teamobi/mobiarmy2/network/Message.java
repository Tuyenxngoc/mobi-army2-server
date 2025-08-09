package com.teamobi.mobiarmy2.network;

import java.io.*;

public class Message {
    private final byte command;
    private ByteArrayOutputStream os;
    private DataOutputStream dos;
    private DataInputStream dis;

    public Message(byte command) {
        this.command = command;
        os = new ByteArrayOutputStream();
        dos = new DataOutputStream(os);
    }

    public Message(byte command, byte[] data) {
        this.command = command;
        dis = new DataInputStream(new ByteArrayInputStream(data));
    }

    public byte getCommand() {
        return command;
    }

    public byte[] getData() {
        return os.toByteArray();
    }

    public DataInputStream reader() {
        return dis;
    }

    public DataOutputStream writer() {
        return dos;
    }

    public void cleanup() {
        try {
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
        } catch (IOException ignored) {
        }
    }
}
