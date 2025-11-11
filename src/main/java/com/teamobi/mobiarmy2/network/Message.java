package com.teamobi.mobiarmy2.network;

import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Message {
    @Getter
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

    public byte[] getData() {
        return os.toByteArray();
    }

    public DataInputStream reader() {
        return dis;
    }

    public DataOutputStream writer() {
        return dos;
    }
}
