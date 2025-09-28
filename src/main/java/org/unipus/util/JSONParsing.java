package org.unipus.util;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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
}
