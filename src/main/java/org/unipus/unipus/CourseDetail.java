package org.unipus.unipus;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unipus.exceptions.CourseInstanceInitException;
import org.unipus.exceptions.UnknownQuestionTypeException;

import java.util.*;

import static org.unipus.unipus.CourseDetail.Node.BaseType.*;
import static org.unipus.unipus.CourseDetail.Node.BaseType.DISCUSSION;
import static org.unipus.unipus.CourseDetail.Node.BaseType.INPUT;
import static org.unipus.unipus.CourseDetail.Node.BaseType.MULTICHOICE;
import static org.unipus.unipus.CourseDetail.Node.BaseType.RICH_TEXT_READ;
import static org.unipus.unipus.CourseDetail.Node.BaseType.TEXT_LEARN;
import static org.unipus.unipus.CourseDetail.Node.BaseType.UNKNOWN;
import static org.unipus.unipus.CourseDetail.Node.BaseType.VIDEO_POINT_READ;
import static org.unipus.unipus.CourseDetail.Node.BaseType.VIDEO_POPUP;
import static org.unipus.unipus.CourseDetail.Node.BaseType.VOCABULARY;

public class CourseDetail {

    public static final List<CourseDetail.Node.BaseType> INVALID_TYPES = List.of(DISCUSSION, MULTI_FILE_UPLOAD, EXIT_TICKET, MULTICHOICE, UNKNOWN);
    public static final List<CourseDetail.Node.BaseType> STUDY_MODES = List.of(RICH_TEXT_READ, TEXT_LEARN, VIDEO_POPUP, VOCABULARY, DISCUSSION, INPUT, VIDEO_POINT_READ);
    public static final List<CourseDetail.Node.BaseType> PRESET_MODES = List.of(RICH_TEXT_READ, TEXT_LEARN, VOCABULARY, INPUT, VIDEO_POINT_READ);

    private static final Logger LOGGER = LogManager.getLogger(CourseDetail.class);
    private List<Node> units;
    // 新增: 根据 id 快速获取节点
    private Map<String, Node> nodeIndex;
    private String name;
    private String type;
    private int courseType;
    private String seriesTemplate;
    private String platform;
    private Map<String, Long> taskStartTimes = new HashMap<>();
    private Map<String, Long> taskEndTimes = new HashMap<>();

    private CourseDetail() {}

    public static CourseDetail valueOf(String courseJSON) {
        CourseDetail courseDetail = new CourseDetail();
        Gson gson = new Gson();
        JsonElement root = JsonParser.parseString(courseJSON);
        courseDetail.name = root.getAsJsonObject().get("name").getAsString();
        courseDetail.type = root.getAsJsonObject().get("type").getAsString();
        courseDetail.courseType = root.getAsJsonObject().get("courseType").getAsInt();
        courseDetail.seriesTemplate = root.getAsJsonObject().get("seriesTemplate").getAsString();
        courseDetail.platform = root.getAsJsonObject().get("platform").getAsString();
        List<Node> units = new ArrayList<>();
        courseDetail.units = units;
        courseDetail.nodeIndex = new HashMap<>(); // 初始化索引
        for (JsonElement element : root.getAsJsonObject().get("units").getAsJsonArray()) {
            Node node = new Node();
            units.add(node);
            traverse(node, element, gson, courseDetail.nodeIndex);
        }
        return courseDetail;
    }

    private static void traverse(Node node, JsonElement element, Gson gson, Map<String, Node> nodeIndex) {
        JsonObject jsonObject = element.getAsJsonObject();
        node.id = jsonObject.get("id").getAsString();
        node.role = Node.Role.valueOf(jsonObject.get("role").getAsString().toUpperCase());
        node.caption = jsonObject.get("caption").getAsString();
        node.name = jsonObject.get("name").getAsString();
        if(!(node.role == Node.Role.SECTION)) {
            node.url = jsonObject.get("url").getAsString();
            node.ref_out = jsonObject.get("ref-out").getAsString();
        }
        // 记录索引
        nodeIndex.put(node.id, node);
        if(jsonObject.has("children")) {
            ArrayList<Node> children = new ArrayList<>();
            node.children = children;
            for(JsonElement childNodeElement : jsonObject.get("children").getAsJsonArray()) {
                Node childNode = new Node();
                children.add(childNode);
                traverse(childNode, childNodeElement, gson, nodeIndex);
            }
        } else {
            node.question_num = jsonObject.get("question_num").getAsInt();
            node.bases = Arrays.stream(jsonObject.get("base").getAsString().split(","))
                            .map(String::trim)
                            .map(Node.BaseType::ofOrUnknown)
                            .toList();
            node.tab_type = jsonObject.get("tab_type").getAsString();
            if (node.role == Node.Role.LINK) node.linkType = jsonObject.get("linkType").getAsString();
        }
    }

    public boolean initTaskTimes(String taskTimesJSON) {
        JsonElement root = JsonParser.parseString(taskTimesJSON);
        if (root.getAsJsonObject().has("code") && root.getAsJsonObject().get("code").getAsInt() != 0) {
            LOGGER.warn("Failed to initialize task times: code isn't 0 in JSON");
            return false;
        }

        JsonObject rt = root.getAsJsonObject().get("rt").getAsJsonObject();
        if (!rt.has("leafs")) {
            LOGGER.warn("Failed to initialize task times: 'leafs' field not found in JSON");
            return false;
        }
        JsonObject leafs = rt.get("leafs").getAsJsonObject();

        leafs.keySet().forEach(nodeId -> {
            JsonObject strategies = leafs.get(nodeId).getAsJsonObject().get("strategies").getAsJsonObject();
            taskStartTimes.put(nodeId, strategies.get("start_time").getAsLong());
            taskEndTimes.put(nodeId, strategies.get("end_time").getAsLong());
        });
        return true;
    }

    public long getTaskStartTime(String taskId) {
        if (taskStartTimes == null) throw new CourseInstanceInitException("Course instance not initialized");
        Long time = taskStartTimes.get(taskId);
        if (time == null) LOGGER.warn("Task start time not found for taskId: {}", taskId);
        return time == null ? 0L : time;
    }

    public long getTaskEndTime(String taskId) {
        if (taskEndTimes == null) throw new CourseInstanceInitException("Course instance not initialized");
        Long time = taskEndTimes.get(taskId);
        if (time == null) LOGGER.warn("Task end time not found for taskId: {}", taskId);
        return time == null ? 0L : time;
    }

    /**
     * 使用 O(1) 方式获取题型
     * @param id    题目 ID
     * @param index 有多个题目时的索引（从 0 开始）
     */
    public Node.BaseType getQuestionType(String id, int index) {
        Node node = nodeIndex == null ? null : nodeIndex.get(id);
        if (node == null) throw new UnknownQuestionTypeException("Unknown Node of TaskId : " + id);
        if (node.bases.get(index) == Node.BaseType.UNKNOWN) throw new UnknownQuestionTypeException("Unknown question type of " + id);
        if (node.role == Node.Role.GROUP || node.role == Node.Role.LINK) return node.bases.get(index);
        throw new UnknownQuestionTypeException("Question type query failed: " + id + " is not a task");
    }

    public Node.BaseType getQuestionType(String id) {
        return getQuestionType(id, 0);
    }

    public List<Node.BaseType> getQuestionTypes(String id) {
        Node node = nodeIndex == null ? null : nodeIndex.get(id);
        if (node == null) throw new UnknownQuestionTypeException("Unknown Node of TaskId : " + id);
        if (node.bases.contains(Node.BaseType.UNKNOWN)) throw new UnknownQuestionTypeException("Unknown question type of " + id);
        if (node.role == Node.Role.GROUP || node.role == Node.Role.LINK) return node.bases;
        throw new UnknownQuestionTypeException("Question type query failed: " + id + " is not a task");
    }

    /**
     * 返回节点（可能为 null）
     */
    public Node getNode(String id) {
        return nodeIndex == null ? null : nodeIndex.get(id);
    }

    /**
     * 是否存在该节点
     */
    public boolean hasNode(String id) {
        return nodeIndex != null && nodeIndex.containsKey(id);
    }

    /**
     * 所有节点 ID（只读）
     */
    public Set<String> getAllNodeIds() {
        return nodeIndex == null ? Collections.emptySet() : Collections.unmodifiableSet(nodeIndex.keySet());
    }

    public List<Node> getUnits() {
        return units;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getCourseType() {
        return courseType;
    }

    public String getSeriesTemplate() {
        return seriesTemplate;
    }

    public String getPlatform() {
        return platform;
    }

    public static class Node {
        private String id;
        private Role role;
        private String caption;
        private String name;
        private String url;
        private String ref_out;

        private List<Node> children;

        private int question_num;
        private List<BaseType> bases;
        private String tab_type;
        private List<Integer> scoreDetail;
        private String linkType;

        public enum Role {
            UNIT,
            SECTION,
            NODE,
            LINK,
            GROUP;

            @Override
            public String toString() {
                return this.name().toLowerCase();
            }
        }

        /**
         * 题目类型，由于后端返回值为base，故命名BaseType
         */
        public enum BaseType {

            MATERIAL_BANKED_CLOZE("material-banked-cloze"),
            SINGLE_CHOICE("single-choice"),
            SHORT_ANSWER("short_answer"),
            TRANSLATION("translation"),
            TEXT_LEARN("text-learn"),
            DISCUSSION("discussion"),
            INPUT("input"), // 主要出现在 role=link
            VIDEO_POINT_READ("video-point-read"),
            RICH_TEXT_READ("rich-text-read"),
            VOCABULARY("vocabulary"),
            SEQUENCE("sequence"),
            BASIC_SCOOP_CONTENT("basic-scoop-content"),
            VIDEO_POPUP("video-popup"),
            WRITING("writing"),
            MULTI_FILE_UPLOAD("multiFileUpload"), // 原始为驼峰
            EXIT_TICKET("exit-ticket"),
            MULTICHOICE("multichoice"),

            UNKNOWN("__unknown__");

            private final String value;

            BaseType(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }

            // ------- 反解析实现 -------

            private static final Map<String, BaseType> LOOKUP;

            static {
                Map<String, BaseType> map = new HashMap<>();
                for (BaseType t : values()) {
                    // 1) 精确原值键（小写）
                    map.put(normalizeKey(t.value), t);

                    // 2) 对原值再做一次 "_" <-> "-" 的替换作为额外键
                    //    例如 short_answer -> short-answer
                    String alt = t.value.replace('_', '-');
                    map.put(normalizeKey(alt), t);

                    // 3) 对原值做一次驼峰转连字符（multiFileUpload -> multi-file-upload）
                    String kebab = camelToKebab(t.value);
                    map.put(normalizeKey(kebab), t);
                }
                LOOKUP = Collections.unmodifiableMap(map);
            }

            /**
             * 尝试将任意字符串解析为 BaseType。
             * 宽松匹配策略：忽略大小写；"_" 与 "-" 互换；自动将驼峰转为连字符后匹配。
             *
             * @param s 输入字符串，可为 null
             * @return Optional<BaseType>（不匹配则返回 Optional.empty()）
             */
            public static Optional<BaseType> fromString(String s) {
                if (s == null || s.isEmpty()) return Optional.empty();

                String k = normalizeKey(s);

                // 直接查
                BaseType t = LOOKUP.get(k);
                if (t != null) return Optional.of(t);

                // 尝试 "_" -> "-"
                String alt = k.replace('_', '-');
                t = LOOKUP.get(alt);
                if (t != null) return Optional.of(t);

                // 再尝试驼峰 -> 连字符（针对原始输入再处理一次）
                String kebab = normalizeKey(camelToKebab(s));
                t = LOOKUP.get(kebab);
                if (t != null) return Optional.of(t);

                return Optional.empty();
            }

            /**
             * 与 {@link #fromString(String)} 类似，但不匹配时返回 UNKNOWN。
             */
            public static BaseType ofOrUnknown(String s) {
                return fromString(s).orElse(UNKNOWN);
            }

            // ------- 工具方法 -------

            private static String normalizeKey(String s) {
                return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
            }

            /**
             * 将驼峰命名转换为连字符命名（idempotent：非驼峰输入不改变语义）。
             * 例：multiFileUpload -> multi-File-Upload（随后通过 normalizeKey 转为小写）。
             */
            private static String camelToKebab(String s) {
                if (s == null || s.isEmpty()) return s;
                // 在小写字母与大写字母的边界加 '-'，例如 "multiFileUpload" -> "multi-File-Upload"
                return s.replaceAll("([a-z])([A-Z])", "$1-$2");
            }
        }

        public String getId() {
            return id;
        }

        public Role getRole() {
            return role;
        }

        public String getCaption() {
            return caption;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getRef_out() {
            return ref_out;
        }

        public List<Node> getChildren() {
            return children;
        }

        public int getQuestion_num() {
            return question_num;
        }

        public List<BaseType> getBases() {
            return bases;
        }

        public String getTab_type() {
            return tab_type;
        }

        public List<Integer> getScoreDetail() {
            return scoreDetail;
        }
    }
}
