package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;

@Slf4j
public final class MessageUtils {

    private MessageUtils() {
    }

    @FunctionalInterface
    public interface IOConsumer<T> {
        void accept(T t) throws IOException;
    }

    public static void send(Session s, byte cmd, IOConsumer<DataOutputStream> writer) {
        try {
            Message ms = new Message(cmd);
            DataOutputStream ds = ms.writer();
            writer.accept(ds);
            ds.flush();
            s.sendMessage(ms);
        } catch (IOException e) {
            log.error("Failed to send message with cmd: {}", cmd, e);
        }
    }
}
