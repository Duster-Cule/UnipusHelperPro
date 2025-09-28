package org.unipus.web.response;

/* (っ*´Д`)っ 小代码要被看光啦 */

/** 用户信息返回实体
 *  @response {
 *     "code": 1,
 *     "msg": "SUCCESS",
 *     "value": {
 *         "userInfo": {
 *             "ssoId": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
 *             "name": "XXX",
 *             "code": "xxxxx",  //学号
 *             "phone": "xxxxxxxxxxx",
 *             "school": xxxxxxx, //学校ID
 *             "roleType": "0",
 *             "unipusCode": "xxxxxx",
 *             "appUserId": "xxxxxxxxxxxxxxxxx",
 *             "schName": "XXXXXXXXX", //学校名称
 *             "createType": 5
 *         },
 *         "applications": null,
 *         "ssxUserInfoList": null
 *     },
 *     "success": true
 * }
 */
public class UserInfoResponse extends Response{
    private int code;
    private String msg;
    private Value value;
    private boolean success;

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

    public static class Value {
        private UserInfo userInfo;

        public UserInfo getUserInfo() {
            return userInfo;
        }
    }

    public static class UserInfo{
        private String ssoId;
        private String name;
        private String code;
        private String phone;
        private long school;
        private String roleType;
        private String unipusCode;
        private String appUserId;
        private String schName;
        private int createType;

        public String getSsoId() {
            return ssoId;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getPhone() {
            return phone;
        }

        public long getSchool() {
            return school;
        }

        public String getRoleType() {
            return roleType;
        }

        public String getUnipusCode() {
            return unipusCode;
        }

        public String getAppUserId() {
            return appUserId;
        }

        public String getSchName() {
            return schName;
        }

        public int getCreateType() {
            return createType;
        }
    }
}
