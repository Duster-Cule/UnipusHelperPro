package org.unipus.unipus;

import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.unipus.exceptions.LoginException;
import org.unipus.exceptions.TaskInitFailedException;
import org.unipus.util.JSONParsing;
import org.unipus.web.UnipusRequest;
import org.unipus.web.response.*;

import java.io.IOException;
import java.util.List;

public class Task implements Runnable{

    private static final Logger logger = LogManager.getLogger(Task.class);

    private final String taskId;
    private String name;
    private String processDescription;
    private Status status = Status.INITIALIZING;
    private int tasksCompleted;
    private int totalTasks;
    private final UnipusRequest request;
    private UserOpration userOpration;                                  // NEED_OPERATION 状态下的用户操作类型
    private List<CourseListResponse.Course> courseList;
    private CourseListResponse.Course currentCourse;                   // 当前正在学习的课程
    private CourseListResponse.CourseResource currentCourseResource;    // 当前正在学习的教程
    private Learn learn;

    private User user;

    // 停止标记（协作式停止）
    private volatile boolean stopRequested = false;

    // 用于等待/唤醒用户交互的锁
    private final Object pauseLock = new Object();

    // 是否当前正处于等待用户操作的阶段
    private volatile boolean awaitingUserOperation = false;

    public Task(String taskId, String username, String password) {
        this.taskId = taskId;
        logger.info("Creating task for user: {}", username);
        try {
            request = new UnipusRequest(taskId);
        } catch (IOException e) {
            logger.error("Failed to create UnipusRequest.", e);
            throw new TaskInitFailedException("Task initialization failed: Failed to create UnipusRequest.");
        }
        login(username, password);
        this.status = Status.READY;
    }

    private void login(String username, String password) {
        logger.info("User {} logging in...", username);
        Response response = request.login(username, password);
        if (response == null || !response.isSuccessful()) {
            throw new LoginException("Login failed: Please check your internet.");
        }

        LoginResponse logininfo = JSONParsing.parseRequest(response, LoginResponse.class);
        if(logininfo == null || logininfo.getRs() == null) {
            throw new LoginException("Login failed: Please check your username and password.");
        }
        user = new User(logininfo.getRs().getUsername(), logininfo.getRs().getJwt(), logininfo.getRs().getOpenId());


        response = request.getUserInfo();
        if (response == null || !response.isSuccessful()) {
            throw new LoginException("Get user info failed: Please check your internet.");
        }

        UserInfoResponse userInfo = JSONParsing.parseRequest(response, UserInfoResponse.class);
        if (userInfo == null || !userInfo.isSuccess()) {
            throw new LoginException("Get user info failed: Please try again later.");
        }

        this.name = userInfo.getValue().getUserInfo().getSchName() + " - " + userInfo.getValue().getUserInfo().getName();
        user.setName(userInfo.getValue().getUserInfo().getName());
        user.setSchoolName(userInfo.getValue().getUserInfo().getSchName());
        user.setAppUserId(userInfo.getValue().getUserInfo().getAppUserId());
        user.setSsoId(userInfo.getValue().getUserInfo().getSsoId());

        this.setProcessDescription("登录成功，未启动");
        logger.info("Login successful for user: {}", username);
    }

    @Override
    public void run() {
        ThreadContext.put("taskId", this.taskId);
        Thread.currentThread().setName(taskId);
        try {
            // 允许重新启动：清除停止标记
            stopRequested = false;

            this.setStatus(Status.RUNNING);
            this.setProcessDescription("正在启动任务...");

            Response courseListResp = request.getCourseList();
            if (courseListResp == null || !courseListResp.isSuccessful()) {
                this.setStatus(Status.ERROR);
                this.setProcessDescription("获取课程列表失败，请检查网络连接。");
                logger.error("Failed to get course list.");
                return;
            }
            CourseListResponse courseListResponse = JSONParsing.parseRequest(courseListResp, CourseListResponse.class);
            if (courseListResponse == null || !courseListResponse.isSuccess()) {
                this.setStatus(Status.ERROR);
                this.setProcessDescription("解析课程列表失败，请稍后再试。");
                logger.error("Failed to parse course list response.");
                return;
            }
            courseList = courseListResponse.getValue().getCourseList();
            learn = new Learn();
            if (courseList.size() == 1) {
                this.currentCourse = courseList.getFirst();
                logger.info("Selected course: {}", currentCourse.getName());
                List<CourseListResponse.CourseResource> courseResourceList = currentCourse.getCourseResourceList();
                if (courseResourceList.size() == 1) {
                    this.currentCourseResource = courseResourceList.getFirst();
                    logger.info("Selected course resource: {}", currentCourseResource.getName());
                    this.name = currentCourse.getName() + " - " + currentCourseResource.getName();
                } else if (!courseResourceList.isEmpty()) {
                    this.setProcessDescription("请选择要学习的教程");
                    this.userOpration = UserOpration.CHOOSE_COURSE;
                    waitForUserOperation();
                    logger.info("User selected course resource: {}", currentCourseResource.getName());
                    this.name = currentCourse.getName() + " - " + currentCourseResource.getName();
                }
            } else if (courseList.size() > 1) {
                this.setProcessDescription("请选择要学习的教程");
                this.userOpration = UserOpration.CHOOSE_COURSE;
                waitForUserOperation();
                logger.info("User selected course: {}, course resource: {}", currentCourse.getName(), currentCourseResource.getName());
                this.name = currentCourse.getName() + " - " + currentCourseResource.getName();
            } else {
                this.setStatus(Status.ERROR);
                this.setProcessDescription("未找到可学习的课程，请检查账号信息。");
                logger.error("No courses found for user: {}", user.getUsername());
                return;
            }
            if (!(currentCourse.getStartTime() < System.currentTimeMillis() && currentCourse.getEndTime() > System.currentTimeMillis())) {
                this.setProcessDescription("课程已结束，无法学习。");
                this.setStatus(Status.ERROR);
                logger.error("Canceled learning course because course {} has ended", currentCourse.getName());
                return;
            }
            TaskManager.getInstance().updateTaskProgress(taskId, currentCourseResource.getFinishPointNum(), currentCourseResource.getTotalPointNum());
            if (learn.startLearn(this, currentCourseResource)) {
                setStatus(Status.COMPLETED);
                setProcessDescription("任务已完成。");
                logger.info("Task {} completed successfully for user: {}", taskId, user.getUsername());
            } else {
                if (stopRequested) return;
                setStatus(Status.ERROR);
                setProcessDescription("任务因未知原因未完成，详情请查看日志。");
                logger.error("Failed to finish learning course for user: {}", user.getUsername());
            }

        } catch (Exception e) {
            logger.error("Task {} encountered an unexpected error: {}", taskId, e.getMessage(), e);
            this.setStatus(Status.ERROR);
            TaskManager.getInstance().handleUncaughtTaskException(this.taskId, e);
        } finally {
            ThreadContext.remove("taskId");
        }
    }

    public UserOpration getUserOpration() {
        return userOpration;
    }

    public String getName() {
        return name;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(String processDescription) {
        this.processDescription = processDescription;
        notifyChanged();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        notifyChanged();
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
        notifyChanged();
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
        notifyChanged();
    }

    public User getUser() {
        return user;
    }

    public List<CourseListResponse.Course> getCourseList() {
        return courseList;
    }

    public CourseListResponse.CourseResource getCurrentCourseResource() {
        return currentCourseResource;
    }

    public void setCurrentCourseResource(CourseListResponse.CourseResource currentCourseResource) {
        this.currentCourseResource = currentCourseResource;
    }

    public CourseListResponse.Course getCurrentCourse() {
        return currentCourse;
    }

    public void setCurrentCourse(CourseListResponse.Course selectedCourse) {
        this.currentCourse = selectedCourse;
    }

    UnipusRequest getRequest() {
        return request;
    }

    /** 返回 0-100 的整数百分比 */
    public int getProgressPercent() {
        if (totalTasks <= 0) return 0;
        int pct = (int) ((tasksCompleted * 100L) / totalTasks);
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        return pct;
    }

    /**
     * 以下为用户交互暂停/恢复支持：
     * - 当 run() 需要等待用户操作时，应调用 waitForUserOperation()
     * - 用户操作完成后，TaskManager 调用 resumeTask() 唤醒任务
     * - 若用户在等待期间点击“暂停”，将打断交互并在恢复后继续等待用户操作
     */
    public void waitForUserOperation() {
        synchronized (pauseLock) {
            awaitingUserOperation = true;
            setStatus(Status.NEED_OPERATION);
            for (;;) {
                if (status == Status.PAUSED) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ignored) {}
                    if (stopRequested) break;
                    // 恢复后继续处于等待用户操作状态
                    setStatus(Status.NEED_OPERATION);
                    continue;
                }
                if (status != Status.NEED_OPERATION) break; // 仅在状态被有效恢复为非 NEED_OPERATION 时退出
                try {
                    pauseLock.wait();
                } catch (InterruptedException ignored) {}
            }
            awaitingUserOperation = false;
        }
    }

    /**
     * 用户点击“恢复”时调用：唤醒所有等待并进入 RUNNING
     */
    public void resumeTask() {
        synchronized (pauseLock) {
            if (stopRequested || status == Status.ERROR) return;
            if (awaitingUserOperation) {
                // 仍处于用户交互阶段
                if (userOpration == UserOpration.CHOOSE_COURSE) {
                    if (currentCourse == null || currentCourseResource == null) {
                        // 还没选完，保持 NEED_OPERATION，仅唤醒以刷新 UI 等
                        setStatus(Status.NEED_OPERATION);
                        try { pauseLock.notifyAll(); } catch (IllegalMonitorStateException ignored) {}
                        return;
                    }
                }
                // 用户操作已完成，可以进入 RUNNING
                try { pauseLock.notifyAll(); } catch (IllegalMonitorStateException ignored) {}
                setStatus(Status.RUNNING);
                return;
            }
            // 非用户交互阶段的恢复：直接进入 RUNNING
            try { pauseLock.notifyAll(); } catch (IllegalMonitorStateException ignored) {}
            setStatus(Status.RUNNING);
        }
    }

    /**
     * 仅供任务内部在合适的检查点调用，进入用户暂停直至恢复。
     * 一般不由外部直接调用（外部请调用 requestPause()）。
     */
    public void suspendTask() {
        synchronized (pauseLock) {
            if (stopRequested) return;
            setStatus(Status.PAUSED);
            try {
                pauseLock.wait();
            } catch (InterruptedException ignored) {}
            finally {
                if (!stopRequested && status != Status.ERROR) setStatus(Status.RUNNING);
            }
        }
    }

    /**
     * 供外部（管理面板）请求暂停：置为 PAUSED 并唤醒当前任何等待，使其尽快进入暂停。
     */
    public void requestPause() {
        synchronized (pauseLock) {
            if (stopRequested) return;
            setStatus(Status.PAUSED);
            try {
                pauseLock.notifyAll();
            } catch (IllegalMonitorStateException ignored) {}
        }
    }

    /**
     * 请求停止任务：
     * - 置 stopRequested 标志；
     * - 更新描述为“已停止，未启动”，状态置为 READY（可再次启动）；
     * - 唤醒所有等待（NEED_OPERATION/WAITING/PAUSED）尽快退出。
     */
    public void requestStop() {
        synchronized (pauseLock) {
            stopRequested = true;
            setProcessDescription("已停止，未启动");
            setStatus(Status.READY);
            try {
                pauseLock.notifyAll();
            } catch (IllegalMonitorStateException ignored) {}
        }
    }

    /** 是否已请求停止（任务内部可轮询） */
    public boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * 任务内部的限流/冷却等待：
     * - 设置状态为 WAITING 并显示 waitingDesc；
     * - 等待至超时；
     * - 若期间用户点击“恢复”，立即结束等待；
     * - 若期间用户点击“暂停”，将阻塞至恢复，然后直接结束等待；
     * - 若请求停止，立即结束等待；
     * - 结束后（未停止）恢复为 RUNNING 并设置 afterDesc。
     */
    public void waitForCooldown(long millis, String waitingDesc, String afterDesc) {
        if (millis <= 0 || stopRequested) {
            if (!stopRequested) setStatus(Status.RUNNING);
            if (!stopRequested && afterDesc != null && !afterDesc.isEmpty()) setProcessDescription(afterDesc);
            return;
        }

        final String originalDesc = this.getProcessDescription();
        setStatus(Status.WAITING);
        if (waitingDesc != null && !waitingDesc.isEmpty()) {
            setProcessDescription(waitingDesc);
        }

        long deadline = System.currentTimeMillis() + millis;
        boolean showingWaitingDesc = true;
        long lastToggleTime = System.currentTimeMillis();

        synchronized (pauseLock) {
            while (true) {
                if (stopRequested) break;
                // 如果外部显式恢复（状态被设为 RUNNING），立即结束等待
                if (status != Status.WAITING && status != Status.PAUSED) break;

                if (status == Status.PAUSED) {
                    try {
                        pauseLock.wait(0); // 等待恢复
                    } catch (InterruptedException ignored) {}
                    continue; // 恢复后重新检查状态和时间
                }

                long now = System.currentTimeMillis();
                if (now >= deadline) break;

                // 每5秒切换一次描述
                if (now - lastToggleTime >= 5000) {
                    showingWaitingDesc = !showingWaitingDesc;
                    setProcessDescription(showingWaitingDesc ? waitingDesc : "[WAITING] - " + originalDesc);
                    lastToggleTime = now;
                }

                long toWait = Math.min(1000L, Math.max(1L, deadline - now));
                try {
                    pauseLock.wait(toWait);
                } catch (InterruptedException ignored) {}
            }
        }

        if (!stopRequested) {
            setStatus(Status.RUNNING);
            if (afterDesc != null && !afterDesc.isEmpty()) setProcessDescription(afterDesc);
            else setProcessDescription(originalDesc); // 如果没有afterDesc，恢复原始描述
        }
    }

    public boolean isWaitingForUser() {
        return this.status == Status.NEED_OPERATION;
    }

    public interface Listener {
        void onTaskChanged(Task task);
    }
    private Listener listener;
    public void setListener(Listener listener) {
        this.listener = listener;
    }
    private void notifyChanged() {
        if (listener != null) listener.onTaskChanged(this);
    }

    public enum Status {
        INITIALIZING,       // 初始化中，这个状态当且仅当刚Task刚开始创建时使用
        READY,              // 准备就绪，尚未开始
        NEED_OPERATION,     // 需要用户操作（暂停状态）
        WAITING,            // 等待中（提交次数到上限等）
        RUNNING,            // 运行中
        PAUSED,             // 暂停中（用户暂停）
        COMPLETED,          // 已完成
        ERROR               // 出错
    }

    public enum UserOpration {
        CHOOSE_COURSE
    }
}
