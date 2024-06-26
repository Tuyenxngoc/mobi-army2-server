package com.teamobi.mobiarmy2.network.Impl;

import com.teamobi.mobiarmy2.network.IMessage;

import java.io.*;

/**
 * @author tuyen
 */
public class Message implements IMessage {

    private byte command;
    private ByteArrayOutputStream os;
    private DataOutputStream dos;
    private ByteArrayInputStream is;
    private DataInputStream dis;

    public Message(int command) {
        this((byte) command);
    }

    public Message(byte command) {
        this.command = command;
        os = new ByteArrayOutputStream();
        dos = new DataOutputStream(os);
    }

    public Message(byte command, byte[] data) {
        this.command = command;
        is = new ByteArrayInputStream(data);
        dis = new DataInputStream(is);
    }

    public byte getCommand() {
        return command;
    }

    public void setCommand(int cmd) {
        setCommand((byte) cmd);
    }

    public void setCommand(byte cmd) {
        command = cmd;
    }

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
        } catch (IOException e) {
        }
    }

}
