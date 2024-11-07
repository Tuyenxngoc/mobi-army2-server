package com.teamobi.mobiarmy2.network.impl;

import com.teamobi.mobiarmy2.network.IMessage;

import java.io.*;

/**
 * @author tuyen
 */
public class Message implements IMessage {

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

    @Override
    public byte getCommand() {
        return command;
    }

    @Override
    public byte[] getData() {
        return os.toByteArray();
    }

    @Override
    public DataInputStream reader() {
        return dis;
    }

    @Override
    public DataOutputStream writer() {
        return dos;
    }

    @Override
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
