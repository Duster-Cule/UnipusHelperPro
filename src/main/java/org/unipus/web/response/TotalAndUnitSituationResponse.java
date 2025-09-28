package org.unipus.web.response;

/* (っ*´Д`)っ 小代码要被看光啦 */

import java.util.List;

/**
 * @response
 * {
 *     "code": 1,
 *     "msg": "SUCCESS",
 *     "value": {
 *         "user": {
 *             "ssoId": "xxxxxxxxxxxxxxxxxxxxxxxxxx",
 *             "appUserId": "xxxxxxxxxxxxxxxxxxxx",
 *             "stuName": "XXX",
 *             "stuCode": "xxxxxxxx",
 *             "img": ""
 *         },
 *         "totalDetail": {
 *             "finishProgress": 100.0,
 *             "duration": 140000,
 *             "score": 70.5
 *         },
 *         "unitList": [
 *             {
 *                 "finishProgress": 100.0,
 *                 "duration": 30000,
 *                 "score": 80.1,
 *                 "nodeId": "xxxxxxxxxxxxx",
 *                 "caption": "Unit 1",
 *                 "name": "Language in mission",
 *                 "path": "xxxxxxxxxxxxx",
 *                 "required": true,
 *                 "role": "unit"
 *             }
 *         ]
 *     },
 *     "success": true
 * }
 */
public class TotalAndUnitSituationResponse extends Response {
    // 顶层字段
    private int code;
    private String msg;
    private Value value;
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public Value getValue() {
        return value;
    }

    // 与 JSON 结构对应的内部类
    public static class Value {
        private User user;
        private TotalDetail totalDetail;
        private List<Unit> unitList;

        public List<Unit> getUnitList() {
            return unitList;
        }

        public TotalDetail getTotalDetail() {
            return totalDetail;
        }
    }

    public static class User {
        private String ssoId;
        private String appUserId;
        private String stuName;
        private String stuCode;
        private String img;
    }

    public static class TotalDetail {
        private Double finishProgress;
        private Long duration;
        private Double score;

        public Double getFinishProgress() {
            return finishProgress == null ? 0.0 : finishProgress;
        }

        public Long getDuration() {
            return duration == null ? 0L : duration;
        }

        public Double getScore() {
            return score == null ? 0.0 : score;
        }
    }

    public static class Unit {
        private Double finishProgress;
        private Long duration;
        private Double score;
        private String nodeId;
        private String caption;
        private String name;
        private String path;
        private Boolean required;
        private String role;

        public Double getFinishProgress() {
            return finishProgress == null ? 0.0 : finishProgress;
        }

        public Long getDuration() {
            return duration == null ? 0L : duration;
        }

        public Double getScore() {
            return score == null ? 0.0 : score;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getPath() {
            return path;
        }

        public Boolean getRequired() {
            return required;
        }

        public String getCaption() {
            return caption;
        }

        public String getName() {
            return name;
        }
    }
}
