package org.unipus.unipus;

/* (っ*´Д`)っ 小代码要被看光啦 */

import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unipus.exceptions.NetworkException;
import org.unipus.exceptions.UnknownQuestionTypeException;
import org.unipus.ui.TaskManagerPanel;
import org.unipus.util.JSONParsing;
import org.unipus.util.StringProcesser;
import org.unipus.util.WebUtils;
import org.unipus.web.UnipusRequest;
import org.unipus.web.response.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.unipus.unipus.CourseDetail.Node.BaseType.*;

public class Learn {

    private static final Logger logger = LogManager.getLogger(Learn.class);

    UnipusRequest request;

    private String courseName;
    private long resourceId;
    private String courseResourceId;    //短的
    private String courseInstanceId;    //长的
    private int strategyId;
    private String courseResourceName;
    private CourseDetail course;
    private HashMap<String, List<String>> requiredTasks;
    private TotalAndUnitSituationResponse totalProgress;
    private UnitTaskSituation unitProgress;

    private final int MAX_SUBMIT_PER_MINUTE = 5;
    private final LinkedList<Long> submitTimestamps = new LinkedList<>();

    public boolean startLearn(Task task, CourseListResponse.CourseResource resource) {
        logger.info("Start learning.");
        if (!checkpoint(task)) return false;

        //获取教程信息
        task.setProcessDescription("正在获取教程信息");
        request = task.getRequest();
        if (!checkpoint(task)) return false;
        Response courseResourceInfo = request.getCourseResourceInfoById(String.valueOf(resource.getId()));

        if (task.isStopRequested()) return false;
        if (courseResourceInfo == null || !courseResourceInfo.isSuccessful()) {
            logger.error("Failed to get course resource info.");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取教程信息失败，请检查网络连接然后重试");
            throw new NetworkException("Get course info failed: Please try again later.");
        }

        CourseResourceInfoByIdResponse courseInfo = JSONParsing.parseRequest(courseResourceInfo, CourseResourceInfoByIdResponse.class);
        if (!checkpoint(task)) return false;
        if (courseInfo == null || !courseInfo.isSuccess()) {
            logger.error("Failed to get course resource info.");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取教程信息失败，请检查网络连接然后重试");
            throw new NetworkException("Get course info failed: Please try again later.");
        }

        this.courseName = courseInfo.getValue().getCourseResource().getCourseName();
        this.resourceId = courseInfo.getValue().getCourseResource().getCourseResourceId();
        this.courseResourceId = courseInfo.getValue().getCourseResource().getResourceId();
        this.courseInstanceId = courseInfo.getValue().getCourseResource().getCourseInstanceId();
        this.strategyId = courseInfo.getValue().getCourseResource().getStrategyId();
        this.courseResourceName = courseInfo.getValue().getCourseResource().getTutorial().getResourceName();

        //获取所有题目
        if (!checkpoint(task)) return false;
        task.setProcessDescription("正在获取所有题目信息");
        Response allTaskInfo = request.getAllTasksofCourse(resource.getInstanceId());
        if (!checkpoint(task)) return false;
        if (allTaskInfo == null || !allTaskInfo.isSuccessful()) {
            logger.error("Failed to get course detail info.");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取教程信息失败，请检查网络连接然后重试");
            throw new NetworkException("Get course info failed: Please try again later.");
        }

        AllTaskofCourseResponse courseDetailInfo = JSONParsing.parseRequest(allTaskInfo, AllTaskofCourseResponse.class);
        if (!checkpoint(task)) return false;
        if (courseDetailInfo == null || courseDetailInfo.getCode() != 0) {
            logger.error("Failed to get all task info of course.");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取教程信息失败，请检查网络连接然后重试");
            throw new NetworkException("Get course info failed: Please try again later.");
        }

        course = CourseDetail.valueOf(courseDetailInfo.getCourse());

        //获取必修课程
        if (!checkpoint(task)) return false;
        task.setProcessDescription("正在获取必修课程信息");
        Response requiredPartofCourse = request.getRequiredPartofCourse(resource.getStrategyId(), String.valueOf(resourceId));

        if (!checkpoint(task)) return false;
        if (requiredPartofCourse == null || !requiredPartofCourse.isSuccessful()) {
            logger.error("Failed to get required part of course.");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取必修课程信息失败，请检查网络连接然后重试");
            throw new NetworkException("Get required task id failed: Please try again later.");
        }

        RequiredPartofCourseResponse requiredTasksDetail = JSONParsing.parseRequest(requiredPartofCourse, RequiredPartofCourseResponse.class);
        if (!checkpoint(task)) return false;
        if (requiredTasksDetail == null || !requiredTasksDetail.isSuccess()) {
            logger.error("Failed to get all task info of course.");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取必修课程信息失败，请检查网络连接然后重试");
            throw new NetworkException("Get required task id failed: Please try again later.");
        }

        requiredTasks = requiredTasksDetail.getAllRequiredTasksinMap();

        //查询学习进度
        if (!checkpoint(task)) return false;
        task.setProcessDescription("正在查询学习进度");
        Response totalAndUnitSituation = request.getTotalAndUnitSituation(resourceId, task.getUser().getAppUserId());
        if (!checkpoint(task)) return false;
        if (totalAndUnitSituation == null || !totalAndUnitSituation.isSuccessful()) {
            logger.error("Failed to obtain the learning progress of the unit");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取学习进度失败，请检查网络连接然后重试");
            throw new NetworkException("Get learning progress id failed: Please try again later.");
        }

        TotalAndUnitSituationResponse totalAndUnitSituationResponse = JSONParsing.parseRequest(totalAndUnitSituation, TotalAndUnitSituationResponse.class);
        if (!checkpoint(task)) return false;
        if (totalAndUnitSituationResponse == null || !totalAndUnitSituationResponse.isSuccess()) {
            logger.error("Failed to obtain the learning progress of the unit");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取学习进度失败，请检查网络连接然后重试");
            throw new NetworkException("Get learning progress id failed: Please try again later。");
        }

        totalProgress = totalAndUnitSituationResponse;

        //查询单元学习进度
        if (!checkpoint(task)) return false;
        if (totalProgress.getValue().getTotalDetail().getFinishProgress() == 100.0) {
            logger.info("All units have been completed.");
            task.setProcessDescription("所有单元已完成学习");
            task.setStatus(Task.Status.COMPLETED);
            return true;
        }

        for (TotalAndUnitSituationResponse.Unit unit : totalProgress.getValue().getUnitList()) {
            if (task.isStopRequested()) return false;
            task.setProcessDescription("正在学习" + unit.getCaption() + " : " + unit.getName());
            if (unit.getFinishProgress() == 100.0) {
                logger.info("{} : {} has been completed.", unit.getCaption(), unit.getName());
                task.setProcessDescription(unit.getCaption() + " : " + unit.getName() + "已完成");
                continue;
            }

            //开始学习单元
            if (task.isStopRequested()) return false;
            if (learnUnit(task, unit)) {
                task.setProcessDescription(unit.getCaption() + " : " + unit.getName() + "已完成");
            } else {
                if (task.isStopRequested()) return false;
                task.setProcessDescription("单元 " + unit.getCaption() + " : " + unit.getName() + " 已跳过: 无必修课程或均已完成");
                logger.info("All task completed unit: {} : {}, skipped.", unit.getCaption(), unit.getName());
            }
        }
        return true;
    }

    private boolean learnUnit(Task task, TotalAndUnitSituationResponse.Unit unit) {
        if (!checkpoint(task)) return false;
        task.setProcessDescription("正在学习" + unit.getCaption() + " : " + unit.getName());

        //获取任务开始结束时间
        Response courseTimeResponse = request.getTaskTimeInfo(courseInstanceId, task.getUser().getOpenId(), unit.getNodeId());
        try (courseTimeResponse) {
            if (courseTimeResponse == null || !courseTimeResponse.isSuccessful()) throw new IOException();
            course.initTaskTimes(courseTimeResponse.body().string());
        } catch (IOException e) {
            logger.error("Failed to get the time info of tasks");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取任务开始结束时间失败，请检查网络连接然后重试");
            throw new NetworkException("Get time info of tasks failed: Please try again later.");
        }

        //获取单元任务进度
        Response unitTaskSituationResponse = request.getUnitTaskSituation(unit.getNodeId(), resourceId, task.getUser().getAppUserId(), task.getUser().getSsoId());
        if (!checkpoint(task)) return false;
        if (unitTaskSituationResponse == null || !unitTaskSituationResponse.isSuccessful()) {
            logger.error("Failed to get the learning progress of the task");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取单元学习进度失败，请检查网络连接然后重试");
            throw new NetworkException("Get unit learning progress id failed: Please try again later.");
        }

        unitProgress = UnitTaskSituation.parse(unitTaskSituationResponse);
        if (!checkpoint(task)) return false;
        if (unitProgress == null) {
            logger.error("Failed to get the learning progress of the task");
            task.setStatus(Task.Status.ERROR);
            task.setProcessDescription("获取单元学习进度失败，请检查网络连接然后重试");
            throw new NetworkException("Get unit learning progress id failed: Please try again later.");
        }

        //获取必修任务
        if (!checkpoint(task)) return false;
        List<String> unitRequiredTasks = requiredTasks.get(unit.getNodeId());
        if (unitRequiredTasks == null || unitRequiredTasks.isEmpty()) {
            return false;
        }

        //检查任务完成情况
        if (!checkpoint(task)) return false;
        List<String> needLearnTasks = unitRequiredTasks.stream()
                .filter((taskId) -> unitProgress.getNodeByNodeId(taskId).getFinishProgress() != 100.0)
                .toList();
        if (needLearnTasks.isEmpty()) {
            return false;
        }

        //开始学习任务
        for (String taskId : needLearnTasks) {
            task.setProcessDescription("正在学习任务 " + unit.getCaption() + " : " + unit.getName() + " - " + course.getNode(taskId).getName());
            if (!learnTask(task, taskId)) {
                if (task.isStopRequested()) return false;
                task.setProcessDescription("不支持的任务 " + unit.getCaption() + " : " + unit.getName() + " - " + course.getNode(taskId).getName());
            }
        }
        return true;
    }

    private boolean learnTask(Task task, String taskId) {
        if (!checkpoint(task)) return false;

        List<Long> ids = new ArrayList<>();
        List<List<String>> answers = new ArrayList<>();
        List<CourseDetail.Node.BaseType> questionTypes;

        //检查题目是否可学习
        {
            long now = System.currentTimeMillis() / 1000L;
            long taskStartTime = course.getTaskStartTime(taskId);
            long taskEndTime = course.getTaskEndTime(taskId);
            if (!(taskStartTime == 0 || taskEndTime == 0) && !(taskStartTime < now && now < taskEndTime)) {
                logger.info("Task {} has not started yet, skipped.", taskId);
                return false;
            }
        }

        try {
            questionTypes = course.getQuestionTypes(taskId);
        } catch (UnknownQuestionTypeException e) {
            logger.warn("Unknown question type of task {}, skipped.", taskId);
            return false;
        }

        //TODO:后续支持：多题目作答，目前样本太少
        //跳过多题目
        if (questionTypes.size() != 1) {
            return false;
        }

        List<CourseDetail.Node.BaseType> typesNeedSkip = List.of(DISCUSSION, MULTI_FILE_UPLOAD, EXIT_TICKET, MULTICHOICE, UNKNOWN);

        if (!Collections.disjoint(questionTypes, typesNeedSkip)) {
            logger.info("Unsupported question type of task {} : {}, skipped.", taskId, Arrays.toString(questionTypes.toArray()));
            return false;
        }

        //获取答案
        if (!new HashSet<>(CourseDetail.PRESET_MODES).containsAll(questionTypes)) {
            List<String> answers0 = new ArrayList<>();
            if (!checkpoint(task)) return false;
            Response answerRes = request.getAnswer(courseInstanceId, taskId, task.getUser().getOpenId());
            if (!checkpoint(task)) return false;
            if (answerRes == null || !answerRes.isSuccessful()) {
                task.setStatus(Task.Status.ERROR);
                task.setProcessDescription("获取题目答案失败，请检查网络连接然后重试");
                throw new NetworkException("Get answer of task " + taskId + " failed: Please try again later.");
            }

            AnswerResponse answerResponse = JSONParsing.parseRequest(answerRes, AnswerResponse.class);
            if (!checkpoint(task)) return false;
            if (answerResponse == null || answerResponse.getCode() != 0) {
                task.setStatus(Task.Status.ERROR);
                task.setProcessDescription("获取题目答案失败，请检查网络连接然后重试");
                throw new NetworkException("Get answer of task " + taskId + " failed: Please try again later.");
            }

            String answerString = StringProcesser.decrypt(answerResponse.getData(), answerResponse.getK());
            logger.trace("Decrypted answer string of task {} : {}", taskId, answerString);
            if (!checkpoint(task)) return false;

            for (int i = 0; i < course.getNode(taskId).getQuestion_num(); i++) {
                Answer answer = Answer.getInstanceByJSON(answerString, i);
                CourseDetail.Node.BaseType questionType = course.getQuestionType(taskId, i);
                try {
                    switch (questionType) {
                        case MATERIAL_BANKED_CLOZE, SINGLE_CHOICE, SEQUENCE, BASIC_SCOOP_CONTENT, TRANSLATION,
                             VIDEO_POPUP:
                            answer.getQuestionAnswers().getChildren().forEach(e -> answers0.add(e.getAnswer().getFirst()));
                            break;
                        case SHORT_ANSWER:
                            answer.getQuestionAnalysis().getChildren().forEach(e -> answers0.add(e.getAnalysis()));
                            break;
                        case WRITING:
                            answers0.add(answer.getQuestionAnalysis().getAnalysis());
                            break;
                        case RICH_TEXT_READ, TEXT_LEARN, VIDEO_POINT_READ, VOCABULARY, INPUT:
                            break;
                        default:
                            throw new IllegalStateException("Unexpected enum value: " + questionType);
                    }
                } catch (NullPointerException e) {
                    logger.error("Something wrong when getting answer of task {} : {}, skipped, and you should report this to developer.", taskId, e.getMessage());
                }
                answers.add(answers0);
                ids.add(answer.getId());
            }
        } else {
            for (int i = 0; i < course.getNode(taskId).getQuestion_num(); i++) {
                answers.add(new ArrayList<String>());
                ids.add(0L);
            }
        }

        //提交答案
        String submitBody = WebUtils.createSubmitBody(ids, answers, taskId, courseInstanceId, task.getUser().getOpenId(), questionTypes);
        boolean submitSuccess = false;
        do {
            if (!checkpoint(task)) return false;
            long now = System.currentTimeMillis();
            while (!submitTimestamps.isEmpty() && submitTimestamps.peek() + 60 * 1000L < now) {
                submitTimestamps.poll();
            }
            if (!checkpoint(task)) return false;
            if (submitTimestamps.size() >= MAX_SUBMIT_PER_MINUTE) {
                long waitMs = Math.max(0, submitTimestamps.peek() + 60 * 1000L - now);
                logger.info("Submit too frequently, wait {} ms", waitMs);
                task.waitForCooldown(waitMs, "提交速度太快了，休息一下", "继续提交");
                if (!checkpoint(task)) return false;
            }
            Response submit = request.submit(submitBody, task.getUser().getOpenId());
            if (!checkpoint(task)) return false;
            submitTimestamps.offer(System.currentTimeMillis());
            if (submit == null || !submit.isSuccessful()) {
                task.setStatus(Task.Status.ERROR);
                task.setProcessDescription("提交答案失败，请检查网络连接然后重试");
                throw new NetworkException("Get answer of task " + taskId + " failed: Please try again later。");
            }

            SubmitResponse submitResponse = JSONParsing.parseRequest(submit, SubmitResponse.class);
            if (!checkpoint(task)) return false;
            if (submitResponse == null || !(submitResponse.getCode() == 0 || submitResponse.getCode() == 600001 || submitResponse.getCode() == 600002)) {
                task.setStatus(Task.Status.ERROR);
                task.setProcessDescription("提交答案失败，请检查网络连接然后重试");
                throw new NetworkException("Get answer of task " + taskId + " failed: Please try again later。");
            }
            if (!checkpoint(task)) return false;

            if (submitResponse.getCode() == 600001 || submitResponse.getCode() == 600002) {
                logger.info("Submitting too frequently, wait 2 minutes.");
                task.waitForCooldown(2 * 60 * 1000L, "提交速度太快了，休息两分钟", "继续提交");
                continue;
            }

            //分数为0时提醒用户
            String version = submitResponse.getData().getVersion();
            boolean skipped = false;

            Response taskInfo = request.getTaskInfo(courseInstanceId, taskId, version, task.getUser().getOpenId());
            if (!checkpoint(task)) return false;
            if (taskInfo == null || !taskInfo.isSuccessful()) {
                task.setProcessDescription("获取题目信息失败");
                logger.warn("Get task info " + taskId + " failed, skipped check 0 score.");
                skipped = true;
            }
            TaskInfoResponse taskInfoResponse = JSONParsing.parseRequest(taskInfo, TaskInfoResponse.class);
            if (!checkpoint(task)) return false;
            if (taskInfoResponse == null || taskInfoResponse.getCode() != 0) {
                task.setProcessDescription("获取题目信息失败");
                logger.warn("Get task info " + taskId + " failed, skipped check 0 score.");
                skipped = true;
            }
            if(!skipped) {
                AtomicBoolean counted = new AtomicBoolean(false);
                taskInfoResponse.getData().getState().getExtendData().getSummary().getAnswerList().forEach((e, a) -> {
                    if (a.getQuestionType() == 1 || a.getQuestionType() == 3) {
                        counted.set(true);
                    }
                });
                if (counted.get() && submitResponse.getData().getState().getScoreAvg() == 0.0) {
                    logger.warn("The score of task {} is 0, please check it later.", taskId);
                    task.setProcessDescription("注意：任务 " + course.getNode(taskId).getName() + " 的得分为0，已自动暂停");
                    task.suspendTask();
                    TaskManagerPanel.getInstance().warnPopup("任务 " + course.getNode(taskId).getName() + " 目前的得分为0，为防止事件再次发生，Task已自动暂停，您可能需要排查情况。\n" +
                            "可能的原因包括但不限于：\n" +
                            "1. 未知的新题型，程序的提交逻辑有问题。\n" +
                            "2. U校园服务端给定的答案或逻辑有误\n" +
                            "3. 其他未知原因\n\n" +
                            "您现在可以开始排查：\n" +
                            "1. 在U校园中找到这个题目并查看这个题目是否唯一，若只有这个题和其他题目不一样，可以继续恢复运行\n" +
                            "2. 若这个题目类型非唯一，或您已确认这个是不支持的新题型，可以在github issue页面反馈开发者\n\n" +
                            "正在进行的题目id：" + taskId);
                }
            }
            submitSuccess = true;
        } while (!submitSuccess);

        //更新任务进度
        if (!checkpoint(task)) return false;
        Response courseListResp = request.getCourseList();
        if (courseListResp == null || !courseListResp.isSuccessful()) {
            logger.warn("Failed to update course progress because of network issues.");
            return true;
        }
        
        if (!checkpoint(task)) return false;

        CourseListResponse courseListResponse = JSONParsing.parseRequest(courseListResp, CourseListResponse.class);
        if (courseListResponse == null || !courseListResponse.isSuccess()) {
            logger.warn("Failed to update course progress because of network issues.");
            return true;
        }

        boolean progressUpdated = false;
        OUT:
        for (CourseListResponse.Course course : courseListResponse.getValue().getCourseList()) {
            for (CourseListResponse.CourseResource courseResource : course.getCourseResourceList()) {
                if (courseResource.equals(task.getCurrentCourseResource())) {
                    TaskManager.getInstance().updateTaskProgress(task.getTaskId(), courseResource.getFinishPointNum(), courseResource.getTotalPointNum());
                    logger.debug("Updated task progress: {}/{}", courseResource.getFinishPointNum(), courseResource.getTotalPointNum());
                    progressUpdated = true;
                    break OUT;
                }
            }
        }
        if (!progressUpdated) {
            logger.warn("Did not find matching CourseResource to update progress for task {} (instanceId={}), UI may not refresh.",
                    task.getTaskId(), task.getCurrentCourseResource() != null ? task.getCurrentCourseResource().getInstanceId() : "null");
        }

        return true;
    }

    private boolean checkpoint(Task task) {
        if (task.isStopRequested()) return false;
        if (task.getStatus().equals(Task.Status.PAUSED)) {
            task.suspendTask();
        }
        return true;
    }
}
