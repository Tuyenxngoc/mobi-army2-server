package com.teamobi.mobiarmy2.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author tuyen
 */
public class GsonUtil {

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            long epochMilli = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            return new JsonPrimitive(epochMilli);
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            long epochMilli = json.getAsLong();
            return LocalDateTime.ofEpochSecond(epochMilli / 1000, (int) (epochMilli % 1000) * 1_000_000, ZoneOffset.UTC);
        }
    }

    private static final Gson INSTANCE = createGson();

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static Gson getInstance() {
        return INSTANCE;
    }
}
