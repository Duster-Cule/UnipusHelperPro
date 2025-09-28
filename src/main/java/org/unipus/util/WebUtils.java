package org.unipus.util;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.unipus.unipus.CourseDetail;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.unipus.unipus.CourseDetail.*;
import static org.unipus.unipus.CourseDetail.Node.BaseType.*;
import static org.unipus.unipus.CourseDetail.Node.BaseType.RICH_TEXT_READ;
import static org.unipus.unipus.CourseDetail.Node.BaseType.TEXT_LEARN;
import static org.unipus.unipus.CourseDetail.Node.BaseType.VIDEO_POPUP;
import static org.unipus.unipus.CourseDetail.Node.BaseType.VOCABULARY;

public class WebUtils {

    public static final Gson GSON = new Gson();

    // 源码中提取到的 secret
    private static final String SECRET = "a824b379f126b8b7aa5e33dee83fb0a05aa7462c";

    /**
     * 根据 openId 生成 x-annotator-auth-token（JWT string）。
     */
    public static String generateAuthToken(String openId) {
        long expMillis = System.currentTimeMillis() + 31536_000_000L; // 源码使用毫秒（约一年）

        Map<String, Object> claims = new HashMap<>();
        claims.put("open_id", openId == null ? "" : openId);
        claims.put("name", "");
        claims.put("email", "");
        claims.put("administrator", false);
        claims.put("exp", expMillis); // 保持与源码一致（毫秒）
        claims.put("iss", "c4f772063dcfa98e9c50");
        claims.put("aud", "edx.unipus.cn");

        // 使用 SECRET 的 UTF-8 bytes 作为 HMAC 密钥（与 JS 实现等效）
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        // 构造 token：设置 header.typ 为 JWT，claims 如上，HS256 签名
        String jwt = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    /**
     * 创建提交请求body内容
     * @param instanceId    答案密文中所含的id(不是questionid)
     * @param answers        以List形式传入的答案列表，若为空则生成无解答的submit请求
     * @param groupId       groupId
     * @param courseId      courseId
     * @param openId        openId
     * @return 提交请求的body内容
     */
    public static String createSubmitBody(long instanceId, List<String> answers, String groupId, String courseId, String openId, @NotNull CourseDetail.Node.BaseType questionType) {

        if (INVALID_TYPES.contains(questionType)) {
            throw new IllegalArgumentException("Unsupported question type recieved : " + questionType);
        }

        if (PRESET_MODES.contains(questionType)) {
            return "{\n" +
                    "  \"quesDatas\": [],\n" +
                    "  \"groupId\": \"" + groupId + "\",\n" +
                    "  \"isCompleted\": [],\n" +
                    "  \"thirdPartyJudges\": \"[]\",\n" +
                    "  \"submitType\": 2,\n" +
                    "  \"hideLoading\": true,\n" +
                    "  \"associationGroupId\": \"\",\n" +
                    "  \"courseId\": \"" + courseId + "\",\n" +
                    "  \"openId\": \"" + openId + "\",\n" +
                    "  \"version\": \"default\"\n" +
                    "}";
        }

        JsonObject body = new JsonObject();
        JsonArray quesDatas = new JsonArray();
        JsonArray isCompleted = new JsonArray();
        JsonArray thirdPartyJudges = new JsonArray();
        JsonObject quesData = new JsonObject();
        JsonObject answer = new JsonObject();

        answer.add("value", new JsonArray());
        JsonArray children = new JsonArray();
        for (String answ : answers) {
            answ = StringProcesser.processIlligalCharacter(StringProcesser.toPlainText(answ));
            JsonObject child = new JsonObject();
            JsonArray arr = new JsonArray();
            arr.add(answ);
            child.add("value", arr);
            child.addProperty("isDone", true);
            children.add(child);

            JsonObject thirdPartyJudge = new JsonObject();
            String value = answ;
            JsonObject payload = new JsonObject();
            JsonArray payloads = new JsonArray();
            JsonObject recordDetail = new JsonObject();

            recordDetail.addProperty("score", 100.00);
            recordDetail.addProperty("comment", "");
            payload.add("recordDetail", recordDetail);
            payloads.add(payload);
            thirdPartyJudge.addProperty("value", value);
            thirdPartyJudge.addProperty("question_type", "basic");
            thirdPartyJudge.add("payloads", payloads);

            isCompleted.add(true);
            thirdPartyJudges.add(thirdPartyJudge);
        }
        answer.add("children", children);
        answer.add("progress", new JsonObject());
        JsonObject record = new JsonObject();
        record.addProperty("url", "");
        answer.add("record", record);

        quesData.addProperty("instanceId", String.valueOf(instanceId));
        quesData.addProperty("answer", GSON.toJson(answer));
        quesData.addProperty("context", "{\"state\":\"submitted\"}");
        quesData.addProperty("contextVersion", 0);
        quesData.addProperty("answerVersion", 0);
        quesDatas.add(quesData);

        body.add("quesDatas", quesDatas);
        body.addProperty("groupId", groupId);
        body.add("isCompleted", isCompleted);
        body.addProperty("thirdPartyJudges", GSON.toJson(thirdPartyJudges));
        int submitType = STUDY_MODES.contains(questionType) ? 2 : 1;
        body.addProperty("submitType", submitType);
        body.addProperty("hideLoading", false);
        body.addProperty("associationGroupId", "");
        body.addProperty("courseId", courseId);
        body.addProperty("openId", openId);
        body.addProperty("version", "default");

        return GSON.toJson(body);
    }

    /**
     * 创建提交请求body内容（完整结构，所有score字段为满分）
     * @param instanceId    每个题目的instanceId（与quesId等价使用的字符串ID）
     * @param answers       每题的答案列表（每题一个字符串列表，顺序即空格/小题顺序）
     * @param groupId       groupId
     * @param courseId      courseId
     * @param openId        openId
     * @param questionTypes 题型列表，与instanceId一一对应
     * @return JSON字符串
     */
    public static String createSubmitBody(List<Long> instanceId,
                                          List<List<String>> answers,
                                          String groupId,
                                          String courseId,
                                          String openId,
                                          @NotNull List<CourseDetail.Node.BaseType> questionTypes) {

        if (!(instanceId.size() == answers.size() && answers.size() == questionTypes.size())) {
            throw new IllegalArgumentException("The size of instanceId, answers and questionTypes must be equal when answers is provided." +
                    " Got sizes: instanceId=" + instanceId.size() + ", answers=" + answers.size() + ", questionTypes=" + questionTypes.size());
        }

        int n = instanceId.size();

        if (n == 1) return createSubmitBody(instanceId.getFirst(), answers.getFirst(), groupId, courseId, openId, questionTypes.getFirst());

        // 规格化输入
        List<List<String>> safeAnswers = new ArrayList<>(n);
        if (answers == null) {
            for (int i = 0; i < n; i++) safeAnswers.add(Collections.emptyList());
        } else {
            for (int i = 0; i < n; i++) {
                safeAnswers.add(answers.get(i) != null ? answers.get(i) : Collections.emptyList());
            }
        }
        List<CourseDetail.Node.BaseType> safeTypes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            CourseDetail.Node.BaseType t = questionTypes.get(i) != null
                    ? questionTypes.get(i) : questionTypes.getFirst();
            safeTypes.add(t);
        }

        // 统计总空格数（用于全局 submitInfo.state.score 数组）
        int totalBlanks = 0;
        int[] perQuestionBlanks = new int[n];
        for (int i = 0; i < n; i++) {
            int k = safeAnswers.get(i).size();
            perQuestionBlanks[i] = k;
            totalBlanks += k;
        }

        long nowMs = System.currentTimeMillis();
        long nowSec = nowMs / 1000;
        // 构建全局 submitInfo（满分）
        Map<String, Object> submitInfo = buildSubmitInfoFullScore(groupId, totalBlanks, nowMs, nowSec);

        // 顶层字段
        List<Map<String, Object>> quesDatas = new ArrayList<>(n);
        List<Boolean> isCompleted = new ArrayList<>(totalBlanks);
        List<Map<String, Object>> judges = new ArrayList<>(totalBlanks);

        // 预先准备 courseAnswerMap（每个 quesDatas 中都包含针对任务内所有题目的映射）
        // 注意：每个 map 项目的数组长度按对应题目的空格数生成，且全部满分/正确
        Map<String, Object> sharedCourseAnswerMap = new LinkedHashMap<>();
        for (int j = 0; j < n; j++) {
            String qid = String.valueOf(instanceId.get(j));
            sharedCourseAnswerMap.put(qid, buildCourseAnswerBlock(groupId, qid, perQuestionBlanks[j], submitInfo));
        }

        // 逐题构建 quesDatas
        for (int i = 0; i < n; i++) {
            String qid = String.valueOf(instanceId.get(i));
            List<String> ans = safeAnswers.get(i);
            CourseDetail.Node.BaseType qType = safeTypes.get(i);

            // 内层 answer（字符串化）
            String answerStr = buildInnerAnswerString(ans, true);

            // 内层 context（字符串化）
            String contextStr = GSON.toJson(Collections.singletonMap("state", "submitted"));

            // children 同时驱动 isCompleted & thirdPartyJudges
            for (String v : ans) {
                isCompleted.add(true);
                Map<String, Object> judge = new LinkedHashMap<>();
                judge.put("value", v);
                judge.put("question_type", mapQuestionType(qType));
                judge.put("reply_type", mapReplyType(qType));
                Map<String, Object> versions = new LinkedHashMap<>();
                versions.put("course", 0);
                versions.put("group", 1);
                versions.put("template", 1);
                versions.put("answer", 0);
                versions.put("content", 0);
                judge.put("versions", versions);
                judge.put("payloads", Collections.emptyList());
                judges.add(judge);
            }

            Map<String, Object> oneQ = new LinkedHashMap<>();
            // courseAnswer（当前题的状态，满分）
            oneQ.put("courseAnswer", buildCourseAnswerBlock(groupId, qid, ans.size(), submitInfo));
            // courseAnswerMap（任务内所有题的映射，满分，复用）
            oneQ.put("courseAnswerMap", sharedCourseAnswerMap);
            oneQ.put("instanceId", qid);
            oneQ.put("answer", answerStr);
            oneQ.put("context", contextStr);
            oneQ.put("contextVersion", 1);
            oneQ.put("answerVersion", 0);

            quesDatas.add(oneQ);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("quesDatas", quesDatas);
        body.put("groupId", groupId);
        body.put("isCompleted", isCompleted);
        body.put("thirdPartyJudges", GSON.toJson(judges)); // 按样例要求：字符串字段
        int submitType = 2;
        for (CourseDetail.Node.BaseType questionType : questionTypes) {
            if (!STUDY_MODES.contains(questionType)) {
                submitType = 1;
                break;
            }
        }
        body.put("submitType", submitType);
        body.put("hideLoading", false);
        body.put("associationGroupId", "");
        body.put("courseId", courseId);
        body.put("openId", openId);
        body.put("version", "default");

        return GSON.toJson(body);
    }

    private static Map<String, Object> buildCourseAnswerBlock(String groupId,
                                                              String quesId,
                                                              int blanks,
                                                              Map<String, Object> submitInfo) {
        // 满分数组
        List<Boolean> boolTrue = repeatBoolean(true, blanks);
        List<Integer> ones = repeatInt(1, blanks);

        Map<String, Object> ca = new LinkedHashMap<>();
        ca.put("isStudy", false);
        ca.put("groupId", groupId);
        ca.put("quesId", quesId);
        ca.put("isRight", boolTrue);
        ca.put("isDone", repeatBoolean(true, blanks));
        ca.put("subPct", ones);
        ca.put("counted", repeatBoolean(true, blanks));
        ca.put("isObjective", repeatBoolean(true, blanks));
        ca.put("firstSubmit", false);
        ca.put("submitInfo", submitInfo); // 引用同一份全局 submitInfo
        return ca;
    }

    private static Map<String, Object> buildSubmitInfoFullScore(String groupId,
                                                                int totalBlanks,
                                                                long nowMs,
                                                                long nowSec) {
        Map<String, Object> recordGrade = new LinkedHashMap<>();
        recordGrade.put("scorePct", 1.0);
        recordGrade.put("ts", nowMs);

        Map<String, Object> state = new LinkedHashMap<>();
        state.put("expired", false);
        state.put("lastSubmit", nowSec);
        state.put("not_start", false);
        state.put("real_score_pct", "1.0");
        state.put("score", repeatInt(1, totalBlanks));
        state.put("score_avg", "100");
        state.put("score_pct", "1.0");
        state.put("state", 0);

        Map<String, Object> strategy = new LinkedHashMap<>();
        strategy.put("endTime", nowSec + 30L * 24 * 3600); // +30天
        strategy.put("record_every_submit", false);
        strategy.put("record_max_submit", false);
        strategy.put("required", true);
        strategy.put("startTime", nowSec);
        strategy.put("task_mini_score_pct", 0);

        Map<String, Object> submitInfo = new LinkedHashMap<>();
        submitInfo.put("group_id", groupId);
        submitInfo.put("strategyId", 0);
        submitInfo.put("record_grade", recordGrade);
        submitInfo.put("state", state);
        submitInfo.put("strategy", strategy);
        submitInfo.put("version", String.valueOf(nowSec));
        submitInfo.put("pass", true);

        return submitInfo;
    }

    private static String buildInnerAnswerString(List<String> ans, boolean fullRight) {
        List<Map<String, Object>> children = new ArrayList<>(ans.size());
        for (String v : ans) {
            Map<String, Object> child = new LinkedHashMap<>();
            child.put("value", Collections.singletonList(v));
            child.put("isDone", true);
            child.put("isRight", fullRight ? Boolean.TRUE : null);
            child.put("replyCategory", "objective");
            children.add(child);
        }
        Map<String, Object> answerObj = new LinkedHashMap<>();
        answerObj.put("value", Collections.emptyList());
        answerObj.put("children", children);
        answerObj.put("progress", new LinkedHashMap<>());
        answerObj.put("record", Collections.singletonMap("url", ""));
        return GSON.toJson(answerObj);
    }

    private static List<Boolean> repeatBoolean(boolean v, int n) {
        List<Boolean> r = new ArrayList<>(n);
        for (int i = 0; i < n; i++) r.add(v);
        return r;
    }

    private static List<Integer> repeatInt(int v, int n) {
        List<Integer> r = new ArrayList<>(n);
        for (int i = 0; i < n; i++) r.add(v);
        return r;
    }

    private static String mapQuestionType(CourseDetail.Node.BaseType type) {
        String name = type.name().toLowerCase(Locale.ROOT);
        if (name.contains("banked") || name.contains("cloze")) return "material-banked-cloze";
        return name.replace('_', '-');
    }

    private static String mapReplyType(CourseDetail.Node.BaseType type) {
        String name = type.name().toLowerCase(Locale.ROOT);
        if (name.contains("banked") || name.contains("cloze")) return "bankedcloze";
        return "objective";
    }
}