package org.unipus.web.response;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @response
 * {
 *     "code": 0,
 *     "message": "成功",
 *     "data": {
 *         "course_id": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
 *         "group_id": "xxxxxxxxxxxx",
 *         "record_grade": {
 *             "scorePct": 1,
 *             "ts": xxxxxxxxx
 *         },
 *         "state": {
 *             "expired": false,
 *             "lastSubmit": xxxxxxxxx,
 *             "not_start": false,
 *             "real_score_pct": 0.375,
 *             "score": [
 *                 1,
 *                 0,
 *                 0,
 *                 0,
 *                 1,
 *                 0,
 *                 1,
 *                 0
 *             ],
 *             "score_avg": 37.5,
 *             "score_pct": 0.375,
 *             "state": 1
 *         },
 *         "strategy": {
 *             "endTime": null,
 *             "record_every_submit": false,
 *             "record_max_submit": false,
 *             "required": false,
 *             "startTime": null,
 *             "task_mini_score_pct": 0
 *         },
 *         "strategyId": xxxxxxx,
 *         "user_id": xxxxxxx,
 *         "version": "xxxxxxxxxxx"
 *     }
 * }
 * 或
 * {
 *     "code": 600001/600002,
 *     "msg": "您的操作过于频繁，请休息几分钟后再继续学习吧!"
 * }
 */
public class SubmitResponse extends Response {
    private int code;
    // 成功返回使用 message, 频繁操作等错误使用 msg
    @Nullable private String message;
    @Nullable private String msg;
    @Nullable private Data data;

    public int getCode() {
        return code;
    }

    /**
     * 统一获取消息字段
     */
    public String getMessage() {
        if (message != null) return message;
        return msg; // 可能为 null
    }

    @Nullable
    public Data getData() {
        return data;
    }

    // ================= inner classes =================
    public static class Data {
        @SerializedName("course_id")
        private String courseId;
        @SerializedName("group_id")
        private String groupId;
        @SerializedName("user_id")
        private Long userId;
        private Long strategyId;
        private String version;
        @SerializedName("record_grade")
        private RecordGrade recordGrade;
        private State state;
        private Strategy strategy;

        public String getCourseId() { return courseId; }
        public String getGroupId() { return groupId; }
        public Long getUserId() { return userId; }
        public Long getStrategyId() { return strategyId; }
        public String getVersion() { return version; }
        public RecordGrade getRecordGrade() { return recordGrade; }
        public State getState() { return state; }
        public Strategy getStrategy() { return strategy; }
    }

    public static class RecordGrade {
        private double scorePct; // 可能为 1 或小数
        private long ts;
        public double getScorePct() { return scorePct; }
        public long getTs() { return ts; }
    }

    @com.google.gson.annotations.JsonAdapter(StateDeserializer.class)
    public static class State {
        private boolean expired;
        private long lastSubmit;
        @SerializedName("not_start") private boolean notStart;
        @SerializedName("real_score_pct") private double realScorePct;
        private List<Integer> score; // 分数数组
        @SerializedName("score_avg") private double scoreAvg;
        @SerializedName("score_pct") private double scorePct;
        private int state; // 状态码

        public boolean isExpired() { return expired; }
        public long getLastSubmit() { return lastSubmit; }
        public boolean isNotStart() { return notStart; }
        public double getRealScorePct() { return realScorePct; }
        public List<Integer> getScore() { return score; }
        public double getScoreAvg() { return scoreAvg; }
        public double getScorePct() { return scorePct; }
        public int getState() { return state; }
    }

    public static class StateDeserializer implements com.google.gson.JsonDeserializer<State> {
        @Override
        public State deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            com.google.gson.JsonObject jsonObject = json.getAsJsonObject();
            State state = new State();

            if(jsonObject.has("expired")) state.expired = !jsonObject.get("expired").isJsonNull() && jsonObject.get("expired").getAsBoolean();
            if(jsonObject.has("lastSubmit")) state.lastSubmit = jsonObject.get("lastSubmit").isJsonNull() ? 0L : jsonObject.get("lastSubmit").getAsLong();
            if(jsonObject.has("not_start")) state.notStart = !jsonObject.get("not_start").isJsonNull() && jsonObject.get("not_start").getAsBoolean();
            if(jsonObject.has("real_score_pct")) state.realScorePct = jsonObject.get("real_score_pct").getAsDouble();
            if(jsonObject.has("score_avg")) state.scoreAvg = jsonObject.get("score_avg").isJsonNull() ? 0.0 : jsonObject.get("score_avg").getAsDouble();
            if(jsonObject.has("score_pct")) state.scorePct = jsonObject.get("score_pct").isJsonNull() ? 0.0 : jsonObject.get("score_pct").getAsDouble();
            if(jsonObject.has("state")) state.state = jsonObject.get("state").isJsonNull() ? 0 : jsonObject.get("state").getAsInt();

            com.google.gson.JsonElement scoreElement = jsonObject.get("score");
            if (scoreElement != null && !scoreElement.isJsonNull()) {
                if (scoreElement.isJsonArray()) {
                    state.score = context.deserialize(scoreElement, new com.google.gson.reflect.TypeToken<List<Integer>>() {}.getType());
                } else if (scoreElement.isJsonPrimitive()) {
                    state.score = List.of(scoreElement.getAsInt());
                }
            }

            return state;
        }
    }

    @com.google.gson.annotations.JsonAdapter(StrategyDeserializer.class)
    public static class Strategy {
        @Nullable private Long endTime; // 可能为 null 或空字符串
        @SerializedName("record_every_submit") private boolean recordEverySubmit;
        @SerializedName("record_max_submit") private boolean recordMaxSubmit;
        private boolean required;
        @Nullable private Long startTime; // 可能为 null 或空字符串
        @SerializedName("task_mini_score_pct") private double taskMiniScorePct;

        @Nullable public Long getEndTime() { return endTime; }
        public boolean isRecordEverySubmit() { return recordEverySubmit; }
        public boolean isRecordMaxSubmit() { return recordMaxSubmit; }
        public boolean isRequired() { return required; }
        @Nullable public Long getStartTime() { return startTime; }
        public double getTaskMiniScorePct() { return taskMiniScorePct; }
    }

    public static class StrategyDeserializer implements com.google.gson.JsonDeserializer<Strategy> {
        @Override
        public Strategy deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            com.google.gson.JsonObject obj = json.getAsJsonObject();
            Strategy s = new Strategy();

            // endTime & startTime may be "" or null or number
            s.endTime = parseMaybeEmptyLong(obj.get("endTime"));
            s.startTime = parseMaybeEmptyLong(obj.get("startTime"));

            com.google.gson.JsonElement recEvery = obj.get("record_every_submit");
            s.recordEverySubmit = recEvery != null && !recEvery.isJsonNull() && recEvery.getAsBoolean();
            com.google.gson.JsonElement recMax = obj.get("record_max_submit");
            s.recordMaxSubmit = recMax != null && !recMax.isJsonNull() && recMax.getAsBoolean();
            com.google.gson.JsonElement requiredEl = obj.get("required");
            s.required = requiredEl != null && !requiredEl.isJsonNull() && requiredEl.getAsBoolean();

            com.google.gson.JsonElement miniPct = obj.get("task_mini_score_pct");
            s.taskMiniScorePct = (miniPct == null || miniPct.isJsonNull()) ? 0.0 : miniPct.getAsDouble();

            return s;
        }

        private static Long parseMaybeEmptyLong(@Nullable com.google.gson.JsonElement el) {
            if (el == null || el.isJsonNull()) return null;
            if (el.isJsonPrimitive()) {
                com.google.gson.JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isNumber()) return p.getAsLong();
                if (p.isString()) {
                    String s = p.getAsString();
                    if (s == null || s.isEmpty()) return null;
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        return null; // 容错：非法字符串返回 null
                    }
                }
            }
            return null;
        }
    }
}
