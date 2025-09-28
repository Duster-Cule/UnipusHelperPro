package org.unipus.web.response;

/* (っ*´Д`)っ 小代码要被看光啦 */

/**
 * {
 *     "code": 1,
 *     "msg": "SUCCESS",
 *     "value": {
 *         "courseResource": {
 *             "courseId": 000000,
 *             "courseName": "大英",
 *             "classId": "xxxxxxxxxxxxxx",
 *             "schCourseId": 0000,
 *             "courseResourceId": 20000000000,
 *             "resourceId": "",
 *             "courseInstanceId": "",
 *             "strategyId": xxxxxx,
 *             "type": 1,
 *             "tutorial": {
 *                 "resourceId": "",
 *                 "resourceName": "新视野大学英语读写教程",
 *                 "resourcePcImage": "",
 *                 "resourceMobileImage": "",
 *                 "bookCoverImage": "",
 *                 "seriesTemplate": null,
 *                 "resourceDesc": "",
 *                 "extra": "",
 *                 "skuId": null,
 *                 "courseVersion": null,
 *                 "instanceId": null,
 *                 "introduceUrl": null,
 *                 "ssxResourceRelation": null,
 *                 "zhoudaoTutorial": false,
 *                 "goodsType": 1,
 *                 "collectionFlag": null
 *             },
 *             "zhoudaoTutorial": false
 *         }
 *     },
 *     "success": true
 * }
 */
public class CourseResourceInfoByIdResponse extends Response {
    private int code;
    private String msg;
    private Value value;
    private boolean success;

    public static class Value {
        private CourseResource courseResource;

        public CourseResource getCourseResource() {
            return courseResource;
        }
    }

    public static class CourseResource {
        private long courseId;
        private String courseName;
        private String classId;
        private long schCourseId;
        private long courseResourceId;
        private String resourceId;
        private String courseInstanceId;
        private int strategyId;
        private int type;
        private Tutorial tutorial;
        private boolean zhoudaoTutorial;

        public long getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getClassId() {
            return classId;
        }

        public long getSchCourseId() {
            return schCourseId;
        }

        public long getCourseResourceId() {
            return courseResourceId;
        }

        public String getResourceId() {
            return resourceId;
        }

        public String getCourseInstanceId() {
            return courseInstanceId;
        }

        public int getStrategyId() {
            return strategyId;
        }

        public int getType() {
            return type;
        }

        public Tutorial getTutorial() {
            return tutorial;
        }

        public boolean isZhoudaoTutorial() {
            return zhoudaoTutorial;
        }
    }

    public static class Tutorial {
        private String resourceId;
        private String resourceName;
        private String resourcePcImage;
        private String resourceMobileImage;
        private String bookCoverImage;
        private Object seriesTemplate;
        private String resourceDesc;
        private String extra;
        private Object skuId;
        private Object courseVersion;
        private Object instanceId;
        private Object introduceUrl;
        private Object ssxResourceRelation;
        private boolean zhoudaoTutorial;
        private int goodsType;
        private Object collectionFlag;

        public String getResourceId() {
            return resourceId;
        }

        public String getResourceName() {
            return resourceName;
        }

        public String getResourcePcImage() {
            return resourcePcImage;
        }

        public String getResourceMobileImage() {
            return resourceMobileImage;
        }

        public String getBookCoverImage() {
            return bookCoverImage;
        }

        public Object getSeriesTemplate() {
            return seriesTemplate;
        }

        public String getResourceDesc() {
            return resourceDesc;
        }

        public String getExtra() {
            return extra;
        }

        public Object getSkuId() {
            return skuId;
        }

        public Object getCourseVersion() {
            return courseVersion;
        }

        public Object getInstanceId() {
            return instanceId;
        }

        public Object getIntroduceUrl() {
            return introduceUrl;
        }

        public Object getSsxResourceRelation() {
            return ssxResourceRelation;
        }

        public boolean isZhoudaoTutorial() {
            return zhoudaoTutorial;
        }

        public int getGoodsType() {
            return goodsType;
        }

        public Object getCollectionFlag() {
            return collectionFlag;
        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Value getValue() {
        return value;
    }

    public boolean isSuccess() {
        return success;
    }
}
