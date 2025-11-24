package org.unipus.util;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class JSONParsing {

    /**
     * 全局 Gson：对后端可能出现的 "" （空字符串）数字字段进行宽松解析，避免 NumberFormatException。
     * 规则：
     *  - 数字字段为 "" 或 null -> 对应的包装类型返回 null；若目标是基本类型(long/double)则返回 0 / 0.0
     *  - 非法数字 (例如 "abc") -> 同上，返回 null / 0
     */
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Long.class, new LenientLongAdapter(false))
            .registerTypeAdapter(Long.TYPE, new LenientLongAdapter(true))
            .registerTypeAdapter(Double.class, new LenientDoubleAdapter(false))
            .registerTypeAdapter(Double.TYPE, new LenientDoubleAdapter(true))
            .create();

    // ================= 自定义宽松数字适配器 =================
    private static class LenientLongAdapter extends TypeAdapter<Long> {
        private final boolean primitive;
        LenientLongAdapter(boolean primitive) { this.primitive = primitive; }
        @Override public void write(JsonWriter out, Long value) throws IOException {
            if (value == null) { out.nullValue(); return; }
            out.value(value);
        }
        @Override public Long read(JsonReader in) throws IOException {
            JsonToken t = in.peek();
            if (t == JsonToken.NULL) { in.nextNull(); return primitive ? 0L : null; }
            if (t == JsonToken.STRING) {
                String s = in.nextString();
                if (s == null || s.isEmpty()) return primitive ? 0L : null;
                try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return primitive ? 0L : null; }
            }
            if (t == JsonToken.NUMBER) {
                try { return in.nextLong(); } catch (NumberFormatException e) { return primitive ? 0L : null; }
            }
            // 其它类型：跳过并返回默认
            in.skipValue();
            return primitive ? 0L : null;
        }
    }

    private static class LenientDoubleAdapter extends TypeAdapter<Double> {
        private final boolean primitive;
        LenientDoubleAdapter(boolean primitive) { this.primitive = primitive; }
        @Override public void write(JsonWriter out, Double value) throws IOException {
            if (value == null) { out.nullValue(); return; }
            out.value(value);
        }
        @Override public Double read(JsonReader in) throws IOException {
            JsonToken t = in.peek();
            if (t == JsonToken.NULL) { in.nextNull(); return primitive ? 0.0 : null; }
            if (t == JsonToken.STRING) {
                String s = in.nextString();
                if (s == null || s.isEmpty()) return primitive ? 0.0 : null;
                try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return primitive ? 0.0 : null; }
            }
            if (t == JsonToken.NUMBER) {
                try { return in.nextDouble(); } catch (NumberFormatException e) { return primitive ? 0.0 : null; }
            }
            in.skipValue();
            return primitive ? 0.0 : null;
        }
    }

    public static boolean isValidJSON(String jsonString) {
        try {
            GSON.fromJson(jsonString, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T parseJSON(String jsonString, Class<T> clazz) {
        return GSON.fromJson(jsonString, clazz);
    }

    public static JsonObject toJsonObject(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    // ===============  下面是专门用于解析U校园后端返回的数据的方法  =================

    public static <T extends org.unipus.web.response.Response> T parseRequest(String requestBodyJSON, Class<T> clazz) {
        return GSON.fromJson(requestBodyJSON, clazz);
    }

    @Nullable
    public static <T extends org.unipus.web.response.Response> T parseRequest(Response response, Class<T> clazz) {
        try (response){
            return parseRequest(response.body().string(), clazz);
        } catch (IOException e) {
            return null;
        }
    }

    // ============== 安全取值工具 ==============

    // String 类型
    public static String safeGetString(JsonObject o, String key, String def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsString(); } catch (Exception e) { return def; }
    }

    // Boolean 类型
    public static Boolean safeGetBoolean(JsonObject o, String key, Boolean def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsBoolean(); } catch (Exception e) { return def; }
    }

    // Byte 类型
    public static Byte safeGetByte(JsonObject o, String key, Byte def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsByte(); } catch (Exception e) { return def; }
    }

    // Short 类型
    public static Short safeGetShort(JsonObject o, String key, Short def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsShort(); } catch (Exception e) { return def; }
    }

    // Integer 类型
    public static Integer safeGetInt(JsonObject o, String key, Integer def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsInt(); } catch (Exception e) { return def; }
    }

    // Long 类型
    public static Long safeGetLong(JsonObject o, String key, Long def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsLong(); } catch (Exception e) { return def; }
    }

    // Float 类型
    public static Float safeGetFloat(JsonObject o, String key, Float def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsFloat(); } catch (Exception e) { return def; }
    }

    // Double 类型
    public static Double safeGetDouble(JsonObject o, String key, Double def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsDouble(); } catch (Exception e) { return def; }
    }

    // BigDecimal 类型
    public static java.math.BigDecimal safeGetBigDecimal(JsonObject o, String key, java.math.BigDecimal def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsBigDecimal(); } catch (Exception e) { return def; }
    }

    // BigInteger 类型
    public static java.math.BigInteger safeGetBigInteger(JsonObject o, String key, java.math.BigInteger def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsBigInteger(); } catch (Exception e) { return def; }
    }

    // Number 类型（通用数字类型）
    public static Number safeGetNumber(JsonObject o, String key, Number def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsNumber(); } catch (Exception e) { return def; }
    }

    // Character 类型
    public static Character safeGetCharacter(JsonObject o, String key, Character def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsCharacter(); } catch (Exception e) { return def; }
    }

    // JsonArray 类型
    public static JsonArray safeGetJsonArray(JsonObject o, String key, JsonArray def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsJsonArray(); } catch (Exception e) { return def; }
    }

    // JsonObject 类型
    public static JsonObject safeGetJsonObject(JsonObject o, String key, JsonObject def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsJsonObject(); } catch (Exception e) { return def; }
    }

    // JsonPrimitive 类型
    public static JsonPrimitive safeGetJsonPrimitive(JsonObject o, String key, JsonPrimitive def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsJsonPrimitive(); } catch (Exception e) { return def; }
    }

    // JsonElement 类型（最通用）
    public static JsonElement safeGetJsonElement(JsonObject o, String key, JsonElement def) {
        try {
            JsonElement elem = o.get(key);
            return (elem == null || elem.isJsonNull()) ? def : elem;
        } catch (Exception e) { return def; }
    }

    // ============== 安全取值工具（找不到值时抛出异常） ==============

    // String 类型
    public static <E extends Exception> String safeGetString(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsString();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Boolean 类型
    public static <E extends Exception> Boolean safeGetBoolean(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsBoolean();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Byte 类型
    public static <E extends Exception> Byte safeGetByte(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsByte();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Short 类型
    public static <E extends Exception> Short safeGetShort(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsShort();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Integer 类型
    public static <E extends Exception> Integer safeGetInt(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsInt();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Long 类型
    public static <E extends Exception> Long safeGetLong(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsLong();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Float 类型
    public static <E extends Exception> Float safeGetFloat(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsFloat();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Double 类型
    public static <E extends Exception> Double safeGetDouble(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsDouble();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // BigDecimal 类型
    public static <E extends Exception> java.math.BigDecimal safeGetBigDecimal(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsBigDecimal();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // BigInteger 类型
    public static <E extends Exception> java.math.BigInteger safeGetBigInteger(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsBigInteger();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Number 类型（通用数字类型）
    public static <E extends Exception> Number safeGetNumber(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsNumber();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Character 类型
    public static <E extends Exception> Character safeGetCharacter(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsCharacter();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // JsonArray 类型
    public static <E extends Exception> JsonArray safeGetJsonArray(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsJsonArray();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // JsonObject 类型
    public static <E extends Exception> JsonObject safeGetJsonObject(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsJsonObject();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // JsonPrimitive 类型
    public static <E extends Exception> JsonPrimitive safeGetJsonPrimitive(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem.getAsJsonPrimitive();
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // JsonElement 类型（最通用）
    public static <E extends Exception> JsonElement safeGetJsonElement(JsonObject o, String key, E exception) throws E {
        try {
            JsonElement elem = o.get(key);
            if (elem == null || elem.isJsonNull()) throw exception;
            return elem;
        } catch (Exception e) {
            if (e == exception) throw exception;
            throw exception;
        }
    }

    // Lambda 版本的 safeGetString
    public static String safeGetString(JsonObject json, String key, Supplier<String> defaultSupplier) {
        if (json == null || key == null) {
            return defaultSupplier.get();
        }
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }

        return defaultSupplier.get();
    }

}
