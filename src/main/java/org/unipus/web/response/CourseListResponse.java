package org.unipus.web.response;

/* (っ*´Д`)っ 小代码要被看光啦 */

import java.util.List;
import java.util.Objects;

/**{
 *     "code": 1,  // 1 表示成功，其他表示失败
 *     "msg": "SUCCESS",
 *     "value": {
 *         "courseList": [      // 课程列表
 *             {
 *                 "id": xxxxxx,
 *                 "name": "大英2",
 *                 "classId": "xxxxxxxxxxxxxxxxxxxx",
 *                 "className": "xxxx",
 *                 "gradeName": "xxxx",
 *                 "startTime": xxxxxxxxxxxxx,  //开始时间（时间戳）
 *                 "endTime": xxxxxxxxxxxxx,    //结束时间（时间戳）
 *                 "schCourseId": xxxx,
 *                 "courseResourceList": [      //课程资源列表
 *                     {
 *                         "id": 20000000000,   //课程资源ID，这个后续有用
 *                         "resourceId": "course-v2:Unipus+xxxx_v4_rw_2+20230116",
 *                         "instanceId": "course-v2:xxxxxxxxxxxxxxx+xxxx_v4_rw_2+xxxxxxxx", //课程实例ID，这个后续有用
 *                         "strategyId": xxxxxx, //课程策略ID，这个后续有用
 *                         "name": "新视野大学英语 读写教程",
 *                         "imgUrl": "xxx",
 *                         "mobileImgUrl": "xxx",
 *                         "bookCoverImage": "",
 *                         "type": 1,
 *                         "finishPointNum": xx, //已学必修知识点
 *                         "totalPointNum": xx, //总必修知识点
 *                         "activation": 0,
 *                         "bookType": "uai",
 *                         "introduceUrl": "",
 *                         "goodsType": 1,
 *                         "zhoudaoTutorial": false
 *                     }
 *                 ],
 *                 "archived": false,
 *                 "createTime": xxxxxxxxxxxxx //课程创建时间（时间戳）
 *             }
 *         ]
 *     },
 *     "success": true
 * }
 */

public class CourseListResponse extends Response {
    private int code;
    private String msg;
    private Value value;
    private boolean success;

    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public Value getValue() { return value; }
    public boolean isSuccess() { return success; }

    public static class Value {
        private List<Course> courseList;
        public List<Course> getCourseList() { return courseList; }
    }

    public static class Course {
        private long id;
        private String name;
        private String classId;
        private String className;
        private String gradeName;
        private long startTime;
        private long endTime;
        private long schCourseId;
        private List<CourseResource> courseResourceList;
        private boolean archived;
        private long createTime;

        public long getId() { return id; }
        public String getName() { return name; }
        public String getClassId() { return classId; }
        public String getClassName() { return className; }
        public String getGradeName() { return gradeName; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public long getSchCourseId() { return schCourseId; }
        public List<CourseResource> getCourseResourceList() { return courseResourceList; }
        public boolean isArchived() { return archived; }
        public long getCreateTime() { return createTime; }
    }

    public static class CourseResource {
        private long id;
        private String resourceId;
        private String instanceId;
        private int strategyId;
        private String name;
        private String imgUrl;
        private String mobileImgUrl;
        private String bookCoverImage;
        private int type;
        private int finishPointNum;
        private int totalPointNum;
        private int activation;
        private String bookType;
        private String introduceUrl;
        private int goodsType;
        private boolean zhoudaoTutorial;

        public long getId() { return id; }
        public String getResourceId() { return resourceId; }
        public String getInstanceId() { return instanceId; }
        public int getStrategyId() { return strategyId; }
        public String getName() { return name; }
        public String getImgUrl() { return imgUrl; }
        public String getMobileImgUrl() { return mobileImgUrl; }
        public String getBookCoverImage() { return bookCoverImage; }
        public int getType() { return type; }
        public int getFinishPointNum() { return finishPointNum; }
        public int getTotalPointNum() { return totalPointNum; }
        public int getActivation() { return activation; }
        public String getBookType() { return bookType; }
        public String getIntroduceUrl() { return introduceUrl; }
        public int getGoodsType() { return goodsType; }
        public boolean isZhoudaoTutorial() { return zhoudaoTutorial; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseResource that = (CourseResource) o;
            return id == that.id &&
                   Objects.equals(instanceId, that.instanceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, instanceId);
        }
    }
}
