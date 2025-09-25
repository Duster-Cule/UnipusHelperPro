package org.unipus.unipus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @response
 * {
 *     "code": 1,
 *     "msg": "SUCCESS",
 *     "value": {
 *         "list": [
 *             {
 *                 "nodeId": "xxxxxxxxx",
 *                 "caption": "Unit 1",
 *                 "name": "XXXXXXXXXXXXXXXX",
 *                 "path": "xxxxxxxxx",
 *                 "required": true,
 *                 "role": "unit",
 *                 "children": [
 *                     {
 *                         "nodeId": "",
 *                         "caption": "",
 *                         "name": "Unit preview",
 *                         "path": "xxxxxxxxx-section",
 *                         "required": true,
 *                         "role": "section",
 *                         "children": [
 *                             {
 *                                 "nodeId": "xxxxxxxxx",
 *                                 "caption": "",
 *                                 "name": "Quotation",
 *                                 "path": "xxxxxxxxx/xxxxxxxxx",
 *                                 "required": true,
 *                                 "role": "node",
 *                                 "children": [
 *                                     {
 *                                         "nodeId": "xxxxxxxxx",
 *                                         "caption": "",
 *                                         "name": "Quotation",
 *                                         "path": "xxxxxxxxx/xxxxxxxxx/xxxxxxxxx",
 *                                         "required": true,
 *                                         "role": "node",
 *                                         "children": [
 *                                             {
 *                                                 "finishProgress": 100,
 *                                                 "duration": 445,
 *                                                 "nodeId": "xxxxxxxxx",
 *                                                 "caption": "",
 *                                                 "name": "Quotation",
 *                                                 "path": "xxxxxxxxx/xxxxxxxxx/xxxxxxxxx/xxxxxxxxx",
 *                                                 "required": true,
 *                                                 "role": "link",
 *                                                 "scoreTaskFlag": false
 *                                             }
 *                                         ]
 *                                     }
 *                                 ]
 *                             }
 *                         ]
 *                     }
 *                 ]
 *             }
 *         ]
 *     },
 *     "success": true
 * }
 */
public class UnitTaskSituation {
    // 顶层字段
    private int code;
    private String msg;
    private boolean success;

    // value 部分
    private List<Node> list;

    // path -> Node（path 唯一）
    private Map<String, Node> pathIndex;
    // nodeId -> Node（nodeId 唯一，排除为空或空串的情况）
    private Map<String, Node> nodeIdIndex; // 新增

    private UnitTaskSituation() {
        this.list = new ArrayList<>();
        this.pathIndex = new HashMap<>();
        this.nodeIdIndex = new HashMap<>();
    }

    public static UnitTaskSituation parse(Response response) {
        try (response) {
            if (response == null || !response.isSuccessful()) {
                return null;
            }
            String body = response.body().string();
            return parse(body);
        } catch (IOException e) {
            return null;
        }
    }

    // 手动解析入口（不使用 GSON 的对象映射）
    public static UnitTaskSituation parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        UnitTaskSituation resp = new UnitTaskSituation();

        if (root.has("code")) resp.code = safeGetInt(root, "code", 0);
        if (root.has("msg")) resp.msg = safeGetString(root, "msg", null);
        if (root.has("success")) resp.success = safeGetBoolean(root, "success", false);

        if (root.has("value") && root.get("value").isJsonObject()) {
            JsonObject value = root.getAsJsonObject("value");
            if (value.has("list") && value.get("list").isJsonArray()) {
                JsonArray arr = value.getAsJsonArray("list");
                for (JsonElement el : arr) {
                    if (el != null && el.isJsonObject()) {
                        Node node = parseNode(el.getAsJsonObject());
                        resp.list.add(node);
                        resp.indexNodeRecursive(node);
                    }
                }
            }
        }
        return resp;
    }

    // 递归构建 Node
    private static Node parseNode(JsonObject obj) {
        Node n = new Node();
        n.nodeId = safeGetString(obj, "nodeId", null);
        n.caption = safeGetString(obj, "caption", null);
        n.name = safeGetString(obj, "name", null);
        n.path = safeGetString(obj, "path", null);
        n.required = obj.has("required") ? safeGetBoolean(obj, "required", null) : null;
        n.role = safeGetString(obj, "role", null);

        // 叶子上可选字段
        if (obj.has("finishProgress") && obj.get("finishProgress").isJsonPrimitive()) {
            try { n.finishProgress = obj.get("finishProgress").getAsDouble(); } catch (Exception ignored) {}
        }
        if (obj.has("duration") && obj.get("duration").isJsonPrimitive()) {
            try { n.duration = obj.get("duration").getAsLong(); } catch (Exception ignored) {}
        }
        if (obj.has("scoreTaskFlag") && obj.get("scoreTaskFlag").isJsonPrimitive()) {
            try { n.scoreTaskFlag = obj.get("scoreTaskFlag").getAsBoolean(); } catch (Exception ignored) {}
        }
        if (obj.has("taskQuesTotalScore") && obj.get("taskQuesTotalScore").isJsonPrimitive()) {
            try { n.taskQuesTotalScore = obj.get("taskQuesTotalScore").getAsInt(); } catch (Exception ignored) {}
        }

        // children
        if (obj.has("children") && obj.get("children").isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray("children");
            for (JsonElement el : arr) {
                if (el != null && el.isJsonObject()) {
                    Node child = parseNode(el.getAsJsonObject());
                    n.children.add(child);
                }
            }
        }
        return n;
    }

    // 建立 path 与 nodeId 索引（递归）
    private void indexNodeRecursive(Node node) {
        if (node.path != null && !node.path.isEmpty()) {
            pathIndex.put(node.path, node);
        }
        // 新增 nodeId 索引（排除 null/空串）
        if (node.nodeId != null && !node.nodeId.isEmpty()) {
            nodeIdIndex.put(node.nodeId, node);
        }
        if (node.children != null) {
            for (Node c : node.children) {
                indexNodeRecursive(c);
            }
        }
    }

    // 根据 path 获取节点
    public Node getNodeByPath(String path) {
        if (path == null || pathIndex == null) return null;
        return pathIndex.get(path);
    }

    // 新增：根据 nodeId 获取节点（忽略 null 或 空串 nodeId）
    public Node getNodeByNodeId(String nodeId) {
        if (nodeId == null || nodeId.isEmpty() || nodeIdIndex == null) return null;
        return nodeIdIndex.get(nodeId);
    }

    // 内部节点类型，递归遍历树
    public static class Node {
        private Double finishProgress;   // 可选
        private Long duration;           // 可选
        private String nodeId;
        private String caption;
        private String name;
        private String path;
        private Boolean required;        // 可选
        private String role;
        private Boolean scoreTaskFlag;   // 可选
        private Integer taskQuesTotalScore; // 可选
        private List<Node> children = new ArrayList<>();

        public Double getFinishProgress() {
            return finishProgress == null ? 0.0 : finishProgress;
        }

        public Long getDuration() {
            return duration == null ? 0L : duration;
        }

        // 方便调试
        @Override
        public String toString() {
            return "Node{" +
                    "name='" + name + '\'' +
                    ", role='" + role + '\'' +
                    ", path='" + path + '\'' +
                    ", children=" + (children == null ? 0 : children.size()) +
                    '}';
        }
    }

    // ============== 安全取值工具 ==============
    private static String safeGetString(JsonObject o, String key, String def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsString(); } catch (Exception e) { return def; }
    }
    private static Integer safeGetInt(JsonObject o, String key, Integer def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsInt(); } catch (Exception e) { return def; }
    }
    private static Boolean safeGetBoolean(JsonObject o, String key, Boolean def) {
        try { return o.get(key).isJsonNull() ? def : o.get(key).getAsBoolean(); } catch (Exception e) { return def; }
    }
}
