package com.teamobi.mobiarmy2.util;

public class GameDataEncryptor {
    private static final byte[] key = new byte[]{78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};//NguyenVanMinh

    public static void encode(byte[] bytes) {
        int keyLen = key.length;
        for (int i = 0; i < bytes.length; i++) {
            byte keyByte = key[i % keyLen]; // lấy byte khóa
            bytes[i] = (byte) (bytes[i] ^ keyByte); // XOR dữ liệu
        }
    }

}
