package com.teamobi.mobiarmy2.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonUtil {

    private static final Gson INSTANCE = createGson();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public static Gson getInstance() {
        return INSTANCE;
    }

    // ===================== LocalDateTime =====================

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.getAsString().isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(json.getAsString(), DATETIME_FORMATTER);
        }

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            return new JsonPrimitive(src.format(DATETIME_FORMATTER));
        }
    }

    // ===================== LocalDate =====================

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.getAsString().isEmpty()) {
                return null;
            }
            return LocalDate.parse(json.getAsString(), DATE_FORMATTER);
        }

        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            return new JsonPrimitive(src.format(DATE_FORMATTER));
        }
    }
}
