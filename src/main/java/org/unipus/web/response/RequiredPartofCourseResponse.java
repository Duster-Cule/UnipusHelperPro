package org.unipus.web.response;

/* (っ*´Д`)っ 小代码要被看光啦 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @response
 * {
 *     "code": 1,
 *     "msg": "SUCCESS",
 *     "value": {
 *         "courseUnitStrategyList": [
 *             {
 *                 "id": xxxxxxx,
 *                 "strategyId": xxxxxxxx,
 *                 "unitId": "xxxxxxxxxxxx",
 *                 "passScore": "0",
 *                 "scoreType": "0",
 *                 "requiredTask": [
 *                     "xxxxxxxxxxxxxxx",
 *                     "xxxxxxxxxxxxxxx"
 *                 ],
 *                 "studyStartTime": xxxxxxxxxx,
 *                 "studyEndTime": xxxxxxxxx,
 *                 "sort": 1,
 *                 "createTime": null,
 *                 "unitName": "Language in mission",
 *                 "caption": "Unit 1",
 *                 "requireNodeType": "task"
 *             }
 *         ],
 *         "courseInfo": {
 *             "curriculaName": "大英",
 *             "courseName": "大英",
 *             "term": "2025年",
 *             "grade": "2025",
 *             "className": "",
 *             "classId": "142608",
 *             "updateName": "学校管理员",
 *             "startTime": xxxxxxxxxx000,
 *             "endTime": xxxxxxxxxx000,
 *             "archived": "false",
 *             "seriesTemplate": "general"
 *         },
 *         "courseStudyStrategy": {
 *             "id": xxxxxx,
 *             "schId": xxxx,
 *             "strategyId": xxxxxxxx,
 *             "strategyName": "大英",
 *             "resourceId": "course-v2:Unipus+xxxxx_2+20230116",
 *             "instantId": "course-v2:xxxxxxxxxxx+xxxxx_2+20230116",
 *             "unitUnlock": 1,
 *             "unitInnerUnlock": 1,
 *             "scoringMode": 0,
 *             "newRulesJson": null,
 *             "tchUpdateEnable": 0,
 *             "settingSource": 1,
 *             "createTime": xxxxxxxxxxxxx,
 *             "updateTime": xxxxxxxxxxxxx,
 *             "studyStartTime": xxxxxxxxxxxxx,
 *             "studyEndTime": xxxxxxxxxxxxx,
 *             "strategyType": 1,
 *             "openId": "130000000000000",
 *             "parentId": 000000,
 *             "courseResourceId": 200000000000,
 *             "delFlag": 0,
 *             "roleType": 2,
 *             "classNum": null,
 *             "syncData": null
 *         }
 *     },
 *     "success": true
 * }
 */
public class RequiredPartofCourseResponse extends Response {
    // 顶层字段
    private int code;
    private String msg;
    private Value value;
    private boolean success;

    // 仅对外提供 requiredTask 相关访问方法
    public List<List<String>> getAllRequiredTasks() {
        if (value == null || value.courseUnitStrategyList == null) {
            return Collections.emptyList();
        }
        List<List<String>> result = new ArrayList<>();
        for (CourseUnitStrategy cus : value.courseUnitStrategyList) {
            result.add(cus == null || cus.requiredTask == null ? Collections.emptyList() : cus.requiredTask);
        }
        return result;
    }

    public HashMap<String, List<String>> getAllRequiredTasksinMap() {
        if (value == null || value.courseUnitStrategyList == null) {
            return new HashMap<>();
        }
        HashMap<String, List<String>> result = new HashMap<>();
        for (CourseUnitStrategy cus : value.courseUnitStrategyList) {
            if (cus != null) {
                result.put(cus.unitId, cus.requiredTask == null ? Collections.emptyList() : cus.requiredTask);
            }
        }
        return result;
    }

    public List<String> getRequiredTaskByUnitId(String unitId) {
        if (unitId == null || value == null || value.courseUnitStrategyList == null) {
            return Collections.emptyList();
        }
        for (CourseUnitStrategy cus : value.courseUnitStrategyList) {
            if (cus != null && unitId.equals(cus.unitId)) {
                return cus.requiredTask == null ? Collections.emptyList() : cus.requiredTask;
            }
        }
        return Collections.emptyList();
    }

    public boolean isSuccess() {
        return success;
    }

    // 与 JSON 结构对应的内部类
    private static class Value {
        private List<CourseUnitStrategy> courseUnitStrategyList;
        private CourseInfo courseInfo;
        private CourseStudyStrategy courseStudyStrategy;
    }

    private static class CourseUnitStrategy {
        private long id;
        private long strategyId;
        private String unitId;
        private String passScore;
        private String scoreType;
        private List<String> requiredTask;
        private Long studyStartTime;
        private Long studyEndTime;
        private Integer sort;
        private String unitName;
        private String caption;
        private String requireNodeType;
    }

    private static class CourseInfo {
        private String curriculaName;
        private String courseName;
        private String term;
        private String grade;
        private String className;
        private String classId;
        private String updateName;
        private Long startTime;
        private Long endTime;
        private String seriesTemplate;
    }

    private static class CourseStudyStrategy {
        private long id;
        private long schId;
        private long strategyId;
        private String strategyName;
        private String resourceId;
        private String instantId;
        private Integer unitUnlock;
        private Integer unitInnerUnlock;
        private Integer scoringMode;
        private Integer tchUpdateEnable;
        private Integer settingSource;
        private Long createTime;
        private Long updateTime;
        private Long studyStartTime;
        private Long studyEndTime;
        private Integer strategyType;
        private String openId;
        private Long parentId;
        private Long courseResourceId;
        private Integer delFlag;
        private Integer roleType;
    }
}