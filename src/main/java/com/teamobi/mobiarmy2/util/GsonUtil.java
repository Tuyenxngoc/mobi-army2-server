package com.teamobi.mobiarmy2.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
public class GsonUtil {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
}
