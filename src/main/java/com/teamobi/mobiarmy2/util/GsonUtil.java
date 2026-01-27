package com.teamobi.mobiarmy2.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonUtil {
    private static final Gson INSTANCE = createGson();

    private static final DateTimeFormatter ISO_DATE_TIME =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public static Gson getInstance() {
        return INSTANCE;
    }

    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(
                LocalDateTime localDateTime,
                Type type,
                JsonSerializationContext context
        ) {
            return new JsonPrimitive(localDateTime.format(ISO_DATE_TIME));
        }

        @Override
        public LocalDateTime deserialize(
                JsonElement json,
                Type typeOfT,
                JsonDeserializationContext context
        ) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), ISO_DATE_TIME);
        }
    }

    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(
                LocalDate value,
                Type type,
                JsonSerializationContext context
        ) {
            return new JsonPrimitive(value.format(ISO_DATE));
        }

        @Override
        public LocalDate deserialize(
                JsonElement json,
                Type typeOfT,
                JsonDeserializationContext context
        ) throws JsonParseException {
            return LocalDate.parse(json.getAsString(), ISO_DATE);
        }
    }
}
