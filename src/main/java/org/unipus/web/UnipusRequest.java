package org.unipus.web;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unipus.util.WebUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * 所有发送请求方法都将放在此类中。
 * 所有方法的返回值均为 Response，后续处理交给调用者。
 */
public class UnipusRequest {

    private static final Logger LOGGER = LogManager.getLogger(UnipusRequest.class);

    private static final String scheme = "https";
    private static final String mainHost = "uai.unipus.cn";
    private static final String ssoHost = "sso.unipus.cn";
    private static final String contentHost = "ucontent.unipus.cn";
    private static final int port = 443;

    //For Debugging
//    private static final String scheme = "http";
//    private static final String mainHost = "localhost";
//    private static final String ssoHost = "localhost";
//    private static final String contentHost = "localhost";
//    private static final int port = 3399;

    private String taskId;
    private OkHttpClient client;
    private final PersistentCookieJar cookieJar;
    private final Gson gson = new Gson();
    private final String userAgent = "UnipusHelperPro/1.0";

    public UnipusRequest(String taskId) throws IOException {
        this.taskId = taskId;
        cookieJar = new PersistentCookieJar(null, null, 300);
        client = new OkHttpClient().newBuilder().cookieJar(cookieJar).build();
    }

    public UnipusRequest(String taskId, PersistentCookieJar cookieJar) throws IOException {
        this.cookieJar = cookieJar;
        client = new OkHttpClient().newBuilder().cookieJar(cookieJar).build();
        this.taskId = taskId;
    }

    public boolean setProxy(String host, int port, Proxy.Type type) {
        try {
            client = client.newBuilder()
                    .proxy(new Proxy(type, new InetSocketAddress(host, port)))
                    .build();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid proxy settings: {}:{}", host, port);
            return false;
        }
        LOGGER.info("Proxy set to {}: {}:{}", type.name(), host, port);
        return true;
    }

    public void removeProxy() {
        client = client.newBuilder()
                .proxy(Proxy.NO_PROXY)
                .build();
        LOGGER.info("Proxy removed.");
    }

    // ===================== 执行与记录 =====================
    private @NotNull Response execute(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    private void record(Request request,
                        @Nullable Response response,
                        long startMillis,
                        long endMillis,
                        String taskId,
                        @Nullable List<Cookie> allCookies,
                        @Nullable Throwable error) {
        RequestManager.RequestRecord rec = RequestManager.RequestRecord.from(
                request, response, startMillis, endMillis, taskId, allCookies, error
        );
        RequestManager.getInstance().record(rec);
    }

    private @Nullable Response executeAndRecord(Request request, String taskId, String errorContext) {
        long start = System.currentTimeMillis();
        Response response = null;
        Throwable error = null;
        try {
            response = execute(request);
            return response;
        } catch (IOException e) {
            error = e;
            LOGGER.error("{} request failed: {}", errorContext, e.getMessage());
            return null;
        } finally {
            long end = System.currentTimeMillis();
            // 获取当前 cookie jar 中的所有 cookie
            List<Cookie> allCookies = cookieJar.getAllCookies();
            record(request, response, start, end, taskId, allCookies, error);
        }
    }

    /**
     * 登录请求
     * @param username 手机号/邮箱
     * @param password 密码
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
     *     "code": "0",                // 0 表示成功，其他表示失败
     *     "msg": "操作成功",
     *     "rs": {
     *         "grantingTicket": "XXX-XXXX-XXXXXXXXXXXXXXXX-sso-api-X",
     *         "serviceTicket": "XXXX-XXXXX-XXXXXXXXXXXXXXXX-sso-api-X",
     *         "tgtExpiredTime": XXX,
     *         "role": null,
     *         "openid": "XXXXXXXXXXXXXXXXXXXXXXXX",
     *         "nickname": "XXXXXX",
     *         "fullname": null,
     *         "username": "XXXXX",
     *         "mobile": "XXXXX",
     *         "email": null,
     *         "perms": "student",
     *         "isSsoLogin": "0",
     *         "isCompleted": null,
     *         "openidHash": null,
     *         "jwt": "XXXXX",
     *         "rt": "XXXXXX",
     *         "createTime": null,
     *         "status": 0,
     *         "source": null,
     *         "links": [
     *             {
     *                 "rel": "self",
     *                 "href": "http://sso.unipus.cn/sso/0.1/sso/login"
     *             }
     *         ]
     *     }
     * }
     */
    @Nullable
    public Response login(@NotNull String username, @NotNull String password) {
        LOGGER.debug("Attempting to log in with username: {}", username);

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);
        json.addProperty("remember", true);
        json.addProperty("agreement", true);
        json.addProperty("service", "https://uai.unipus.cn/home");
        String jsonString = gson.toJson(json);

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(ssoHost)
                .port(port)
                .addPathSegments("sso/0.1/sso/login")
                .build();

        RequestBody body = RequestBody.create(
                jsonString,
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .build();

        String reqTaskId = "task-" + username;
        return executeAndRecord(request, reqTaskId, "Login");
    }

    /**
     * 获取用户信息请求
     * @param authorization 校验 (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
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
    @Nullable
    public Response getUserInfo(@Nullable String authorization) {
        LOGGER.debug("Fetching user info.");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(mainHost)
                .port(port)
                .addPathSegments("api/account/user/info")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get User Info");
    }

    @Nullable
    public Response getUserInfo() {
        return getUserInfo(null);
    }

    /**
     * 获取课程列表请求
     * @param authorization 校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
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
    @Nullable
    public Response getCourseList(@Nullable String authorization) {
        LOGGER.debug("Fetching course list.");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(mainHost)
                .port(port)
                .addPathSegments("api/cmgt/course/getCourseListByStudent")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Course List");
    }

    @Nullable
    public Response getCourseList() {
        return getCourseList(null);
    }

    /**
     * 此处Info特指开始时间/结束时间/是否必修
     * @param courseInstanceId 课程实例ID
     * @param openId openId
     * @param unitNodeId 单元NodeID，注意一定要是Task所在单元的NodeID
     * @param authorization 校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     *
     * @response
     * {
     *     "code": 0,
     *     "msg": "success",
     *     "rt": {
     *         "duration_time": 0,
     *         "flag": "{\"sdk\":0,\"lastSubmit\":0}",
     *         "leafs": {
     *             "xxxxxxxxxxxx": {
     *                 "duration": 0,
     *                 "state": {
     *                     "pass": 0,
     *                     "pass2": 0,
     *                     "perm": 1
     *                 },
     *                 "strategies": {
     *                     "end_time": 1768060799,
     *                     "min_score_pct": 0,
     *                     "required": true,
     *                     "start_time": 1763308800,
     *                     "statistic_mode_out": false
     *                 },
     *                 "tab_type": "text"
     *             }
     *         },
     *         "micros": {
     *             "xxxxxxxxxxxx/xxxxxxxxxxxx": {
     *                 "state": {
     *                     "pass": 0,
     *                     "pass2": 0,
     *                     "perm": 1
     *                 }
     *         },
     *         "open_id": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
     *         "publish_version": "xxxxx",
     *         "tutorialId": "course-v2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
     *         "unit_id": "xxxxxxxxxxxx"
     *     }
     * }
     */
    @Nullable
    public Response getTaskTimeInfo(String courseInstanceId, String openId, String unitNodeId, String authorization) {
        LOGGER.debug("Fetching task time info.");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(contentHost)
                .port(port)
                .addPathSegments("course/api/v2/course_progress/")
                .addPathSegments(courseInstanceId)
                .addPathSegments(unitNodeId)
                .addPathSegments(openId)
                .addPathSegments("default")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .header("x-annotator-auth-token", WebUtils.generateAuthToken(openId))
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Task Time Info");
    }

    @Nullable
    public Response getTaskTimeInfo(String courseInstanceId, String openId, String unitNodeId) {
        return getTaskTimeInfo(courseInstanceId, openId, unitNodeId, null);
    }

    /**
     * 获取课程必修部分Task id 请求
     * @param strategyId 课程策略ID
     * @param courseResourceId 课程资源ID
     * @param authorization 校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
     *     "code": 1,       // 1 表示成功，其他表示失败
     *     "msg": "SUCCESS",
     *     "value": {
     *         "courseUnitStrategyList": [
     *             {
     *                 "id": xxxxxxx,
     *                 "strategyId": xxxxxx,
     *                 "unitId": "xxxxxxxx",
     *                 "passScore": "0",
     *                 "scoreType": "0",
     *                 "requiredTask": [
     *                     "xxxxxxxxxxxxxxx",
     *                     "xxxxxxxxxxxxxxx",
     *                     "xxxxxxxxxxxxxxx",           // Task ID, 后续需要用到
     *                 ],
     *                 "studyStartTime": xxxxxxxxxxxxx, //学习开始时间（时间戳）
     *                 "studyEndTime": xxxxxxxxxxxxx,   //学习结束时间（时间戳）
     *                 "sort": 1,                       //单元顺序（从1开始）
     *                 "createTime": null,
     *                 "unitName": "Language in mission",   //单元名称
     *                 "caption": "Unit 1",                 //单元标题
     *                 "requireNodeType": "task"
     *             }
     *         ],
     *         "courseInfo": {
     *             "curriculaName": "大英",
     *             "courseName": "大英",
     *             "term": "2025春季",
     *             "grade": "2025",
     *             "className": "xx",
     *             "classId": "xxxxxxxxxxxxxxxxxxxxx",
     *             "updateName": "学校管理员",
     *             "startTime": xxxxxxxxxxxxx,
     *             "endTime": xxxxxxxxxxxxx,
     *             "archived": "false",
     *             "seriesTemplate": "general"
     *         },
     *         "courseStudyStrategy": {
     *             "id": xxxxxx,
     *             "schId": xxxx,
     *             "strategyId": xxxxxxx,
     *             "strategyName": "大英",
     *             "resourceId": "course-v2:Unipus+xxxx_v4_rw_2+xxxxxxxx",
     *             "instanceId": "course-v2:xxxxxxxxxxxxxxx+xxxx_v4_rw_2+xxxxxxxx",
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
     *             "openId": "xxxxxxxxxxxxxxxxx",
     *             "parentId": 490610,
     *             "courseResourceId": 20000000000,
     *             "delFlag": 0,
     *             "roleType": 2,
     *             "classNum": null,
     *             "syncData": null
     *         }
     *     },
     *     "success": true
     * }
     */
    @Nullable
    public Response getRequiredPartofCourse(int strategyId, @NotNull String courseResourceId, @Nullable String authorization) {
        LOGGER.debug("Fetching required part of course.");

        JsonObject json = new JsonObject();
        json.addProperty("id", strategyId);
        json.addProperty("courseResourceId", courseResourceId);
        String jsonString = gson.toJson(json);

        RequestBody body = RequestBody.create(
                jsonString,
                MediaType.parse("application/json; charset=utf-8")
        );

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(mainHost)
                .port(port)
                .addPathSegments("api/tla/courseStudyStrategy/detail")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Required Part of Course");
    }

    @Nullable
    public Response getRequiredPartofCourse(int strategyId, @NotNull String courseResourceId) {
        return getRequiredPartofCourse(strategyId, courseResourceId, null);
    }

    /**
     * 通过id获取教程资源信息请求
     * @param courseResourceId  教程资源ID
     * @param authorization     校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
     *     "code": 1,       // 1 表示成功，其他表示失败
     *     "msg": "SUCCESS",
     *     "value": {
     *         "courseResource": {
     *             "courseId": xxxxxx,
     *             "courseName": "大英",
     *             "classId": "xxxxxxxxxxxxxxxxxxxxxx",
     *             "schCourseId": xxxx,
     *             "courseResourceId": 20000000000,
     *             "resourceId": "course-v2:Unipus+xxxx_v4_rw_2+xxxxxxxx",
     *             "courseInstanceId": "course-v2:xxxxxxxxxxx+xxxx_v4_rw_2+xxxxxxxx",
     *             "strategyId": xxxxxx,
     *             "type": 1,
     *             "tutorial": {
     *                 "resourceId": "course-v2:Unipus+xxxx_v4_rw_2+xxxxxxxx",
     *                 "resourceName": "新视野大学英语（第四版）读写教程2",
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
    @Nullable
    public Response getCourseResourceInfoById(String courseResourceId, @Nullable String authorization) {
        LOGGER.debug("Fetching course resource info by ID.");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(mainHost)
                .port(port)
                .addPathSegments("api/cmgt/course/getCourseResourceInfoById/")
                .addPathSegments(courseResourceId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Course Resource Info By ID");

    }

    @Nullable
    public Response getCourseResourceInfoById(String courseResourceId) {
        return getCourseResourceInfoById(courseResourceId, null);
    }

    /**
     * 获取课程所有任务请求
     * @param courseInstanceId 课程实例ID
     * @param authorization    校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     */
    @Nullable
    public Response getAllTasksofCourse(@NotNull String courseInstanceId, @Nullable String authorization) {
        LOGGER.debug("Fetching all tasks of course.");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(contentHost)
                .port(port)
                .addPathSegments("course/api/course/")
                .addPathSegments(courseInstanceId)
                .addPathSegments("default")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get All Tasks of Course");
    }

    @Nullable
    public Response getAllTasksofCourse(@NotNull String courseInstanceId) {
        return getAllTasksofCourse(courseInstanceId, null);
    }

    /**
     * 获取某任务答案请求
     * @param courseInstanceId 课程实例ID
     * @param taskId           任务ID
     * @param openid           openid
     * @param authorization    校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
     *     "code": 0,               // 0 表示成功，其他表示失败
     *     "message": "success",
     *     "data": "unipus.xxxxxxxx",  //加密的答案字符串，需解密
     *     "publish_version": xxxxxx,
     *     "k": "xxxxxxxx"             //解密密钥片段
     * }
     */
    @Nullable
    public Response getAnswer(String courseInstanceId, String taskId, String openid, @Nullable String authorization) {
        LOGGER.debug("Fetching answer for task ID {} in course instance {}", taskId, courseInstanceId);

        String token = WebUtils.generateAuthToken(openid);

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(contentHost)
                .port(port)
                .addPathSegments("course/api/v3/answer")
                .addPathSegments(courseInstanceId)
                .addPathSegments(taskId)
                .addPathSegments("default")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .header("x-annotator-auth-token", token)
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Answer");
    }

    @Nullable
    public Response getAnswer(String courseInstanceId, String taskId, String openid) {
        return getAnswer(courseInstanceId, taskId, openid, null);
    }

    /**
     * 获取学习进度请求（总进度及单元进度）
     * @param id 课程ID
     * @param appUserId appUserID
     * @param authorization 校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
     *     "code": 1,
     *     "msg": "SUCCESS",
     *     "value": {
     *         "user": {
     *             "ssoId": "8a1b608d3b854904b855c40436edf1fe",
     *             "appUserId": "1306829441477433778",
     *             "stuName": "张冠中",
     *             "stuCode": "20241526",
     *             "img": ""
     *         },
     *         "totalDetail": {
     *             "finishProgress": 100,   //总完成进度，百分比
     *             "duration": 140000,      //总学习时长，单位秒
     *             "score": 72.3            //总得分
     *         },
     *         "unitList": [
     *             {
     *                 "finishProgress": 100,
     *                 "duration": 37542,
     *                 "score": 85.5,
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
    @Nullable
    public Response getTotalAndUnitSituation(long id, String appUserId, String authorization) {
        LOGGER.debug("Fetching progress of studying");

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(mainHost)
                .port(port)
                .addPathSegments("api/tla/learningDetail/studyRecord/totalAndUnitSituation")
                .addQueryParameter("id", String.valueOf(id))
                .addQueryParameter("appUserId", appUserId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("authorization", authorization == null ? "" : authorization)
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Total and Unit Situation");
    }

    @Nullable
    public Response getTotalAndUnitSituation(long id, String appUserId) {
        return getTotalAndUnitSituation(id, appUserId, null);
    }

    /**
     * 获取单元学习进度请求（包含任务完成情况）
     * @param nodeId 单元NodeID
     * @param id 课程ID
     * @param appUserId appUserID
     * @param ssoId ssoID
     * @param authorization 校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
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
    @Nullable
    public Response getUnitTaskSituation(String nodeId, long id, String appUserId, String ssoId, @Nullable String authorization) {
        LOGGER.debug("Fetching task situation of unit {}", nodeId);

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(mainHost)
                .port(port)
                .addPathSegments("api/tla/learningDetail/studyRecord/unitTaskSituation")
                .addQueryParameter("nodeId", nodeId)
                .addQueryParameter("id", String.valueOf(id))
                .addQueryParameter("appUserId", appUserId)
                .addQueryParameter("ssoId", ssoId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("authorization", authorization == null ? "" : authorization)
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Unit Task Situation");
    }

    @Nullable
    public Response getUnitTaskSituation(String nodeId, long id, String appUserId, String ssoId) {
        return getUnitTaskSituation(nodeId, id, appUserId, ssoId, null);
    }

    /**
     * @param openId     openId
     * @param authorization 校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     */
    @Nullable
    public Response submit(String body, String openId, @Nullable String authorization) {
        LOGGER.debug("Submitting answers.");

        String token = WebUtils.generateAuthToken(openId);

        RequestBody requestBody = RequestBody.create(
                body,
                MediaType.parse("application/json; charset=utf-8")
        );

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(contentHost)
                .port(port)
                .addPathSegments("course/api/v3/newExploration/submit")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .header("x-annotator-auth-token", token)
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Submit Answers");
    }

    public Response submit(String body, String openId) {
        return submit(body, openId, null);
    }

    /**
     * 获取题目信息
     * @param courseInstanceId  courseInstanceId
     * @param taskId            taskId
     * @param version           version(submit中获取)
     * @param openId            openId
     * @param authorization     校验  (可选，有Cookie时可不传)
     * @return 服务器返回的 Response， 可能为 null（请求失败）
     * @response
     * {
     *     "success": true,
     *     "code": 0,
     *     "data": {
     *         "course": "course-v2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
     *         "module": "xxxxxxxxxxxx-xxxxxxxxx",
     *         "base": "xxxxxxxxxxxxxxxxxxxxxx",
     *         "commit": "xxxxxxxxxxxxxxxxxxxxxx",
     *         "state": {
     *             "__EXTEND_DATA__": {
     *                 "__SUBMIT_INFO__": {
     *                     "course_id": "course-v2:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
     *                     "group_id": "xxxxxxxxxxxxxxxxxxxxxx",
     *                     "record_grade": {
     *                         "scorePct": 0.01,
     *                         "ts": xxxxxxxxxxxxx
     *                     },
     *                     "state": {
     *                         "expired": false,
     *                         "lastSubmit": xxxxxxxxx,
     *                         "not_start": false,
     *                         "real_score_pct": 1,
     *                         "score": [
     *                             0,
     *                             0
     *                         ],
     *                         "score_avg": 0,
     *                         "score_pct": 0,
     *                         "state": 1
     *                     },
     *                     "strategy": {
     *                         "endTime": xxxxxxxxxxxx,
     *                         "record_every_submit": false,
     *                         "record_max_submit": false,
     *                         "required": true,
     *                         "startTime": xxxxxxxx,
     *                         "task_mini_score_pct": 0
     *                     },
     *                     "strategyId": xxxxxx,
     *                     "user_id": xxxxxxxx,
     *                     "version": "xxxxxxxx"
     *                 },
     *                 "__SUMMARY__": {
     *                     "answerList": {
     *                         "0": {
     *                             "done": true,
     *                             "questionType": 2,
     *                             "question_type": "basic",
     *                             "right": false,
     *                             "rule": "subjective",
     *                             "signature": "",
     *                             "student_answer": {
     *                                 "answers": "1",
     *                                 "payloads": [],
     *                                 "question_type": "basic",
     *                                 "reply_type": "text-area",
     *                                 "value": "1",
     *                                 "versions": {
     *                                     "answer": 3,
     *                                     "content": 0,
     *                                     "course": xxxxxx,
     *                                     "group": 1,
     *                                     "template": 1
     *                                 }
     *                             },
     *                             "versions": {
     *                                 "answer": 3,
     *                                 "content": 0,
     *                                 "course": xxxxxxx,
     *                                 "group": 1,
     *                                 "template": 1
     *                             }
     *                         }
     *                     }
     *                 }
     *             },
     *             "correlationData": null,
     *             "quesData": "[{\"instanceId\":\"xxxxxxxxxxxxxxxx\",\"answer\":\"{\\\"value\\\":[],\\\"children\\\":[{\\\"value\\\":[\\\"1\\\"],\\\"isDone\\\":true},{\\\"value\\\":[\\\"2\\\"],\\\"isDone\\\":true}],\\\"progress\\\":{},\\\"record\\\":{\\\"url\\\":\\\"\\\"}}\",\"context\":\"{\\\"state\\\":\\\"submitted\\\"}\",\"contextVersion\":1,\"answerVersion\":4}]",
     *             "version": "1758707921"
     *         }
     *     },
     *     "msg": ""
     * }
     */
    public Response getTaskInfo(String courseInstanceId, String taskId, String version, String openId, String authorization) {
        LOGGER.debug("Getting Task Info.");

        String token = WebUtils.generateAuthToken(openId);

        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(contentHost)
                .port(port)
                .addPathSegments("api/mobile/user_module/")
                .addPathSegments(courseInstanceId)
                .addPathSegments(taskId + "-" + version)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization == null ? "" : authorization)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .header("x-annotator-auth-token", token)
                .build();

        String reqTaskId = Thread.currentThread().getName();
        return executeAndRecord(request, reqTaskId, "Get Task Info");
    }

    public Response getTaskInfo(String courseResourceId, String taskId, String version, String openId) {
        return getTaskInfo(courseResourceId, taskId, version, openId, null);
    }

    public void clearCookies() {
        cookieJar.clear();
        LOGGER.debug("All cookies cleared.");
    }

    public void addCookie(Cookie cookie) {
        cookieJar.addCookie(cookie);
        LOGGER.debug("Cookie added: {}", cookie.toString());
    }

    public PersistentCookieJar getCookieJar() {
        return cookieJar;
    }
}
