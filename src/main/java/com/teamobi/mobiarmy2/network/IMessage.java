package com.teamobi.mobiarmy2.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface IMessage {

    byte getCommand();

    byte[] getData();

    DataInputStream reader();

    DataOutputStream writer();

    void cleanup();

}
