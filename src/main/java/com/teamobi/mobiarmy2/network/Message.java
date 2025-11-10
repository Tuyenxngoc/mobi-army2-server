package com.teamobi.mobiarmy2.network;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
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

    public void cleanup() {
        try {
            if (dis != null) {
                dis.close();
                dis = null;
            }
            if (dos != null) {
                dos.close();
                dos = null;
            }
            if (os != null) {
                os.close();
                os = null;
            }
        } catch (IOException e) {
            log.error("Error during cleanup: {}", e.getMessage());
        }
    }
}
