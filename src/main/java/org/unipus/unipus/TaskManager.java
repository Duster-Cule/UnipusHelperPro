package org.unipus.unipus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unipus.exceptions.LoginException;
import org.unipus.exceptions.TaskInitFailedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskManager {

    private static final Logger logger = LogManager.getLogger(TaskManager.class);

    private static TaskManager instance;

    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> futures = new ConcurrentHashMap<>();

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private final ExecutorService executor;

    private TaskManager() {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        executor = Executors.newFixedThreadPool(threads);
    }

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    /**
     * GUI 可以实现此接口以接收任务变化并刷新界面
     */
    public interface Listener {
        void onTaskAdded(Task task);
        void onTaskRemoved(String taskId);
        void onTaskUpdated(Task task);
        void onAllCleared();
        void onExceptionOccurred(String taskId, Throwable ex);
    }

    public void addListener(Listener l) {
        if (l == null) return;
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void notifyAdded(Task task) {
        for (Listener l : listeners) {
            try {
                l.onTaskAdded(task);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyRemoved(String taskId) {
        for (Listener l : listeners) {
            try {
                l.onTaskRemoved(taskId);
            } catch (Exception ignored) {
            }
        }
    }

    public void notifyUpdated(Task task) {
        for (Listener l : listeners) {
            try {
                l.onTaskUpdated(task);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAllCleared() {
        for (Listener l : listeners) {
            try {
                l.onAllCleared();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 创建并添加任务到管理器（不自动启动）。
     * 返回 true 表示添加成功，false 表示失败或已存在。
     */
    public boolean createAndAddTask(String taskId, String username, String password) {
        if (taskId == null) return false;
        if (tasks.containsKey(taskId)) return false;
        try {
            Task t = new Task(taskId, username, password);
            t.setTotalTasks(0);
            t.setTasksCompleted(0);
            t.setListener(this::notifyUpdated);
            tasks.put(taskId, t);
            notifyAdded(t);
            logger.info("Task {} added.", taskId);
            return true;
        } catch (LoginException e) {
            logger.error("Login Failed.", e);
            throw new LoginException(e);
        }catch (TaskInitFailedException e) {
            logger.error("Task Init Failed.", e);
            throw new TaskInitFailedException(e);
        }catch (Exception e) {
            logger.error("Failed to create task {}", taskId, e);
            return false;
        }
    }

    /**
     * 直接把已有 Task 对象添加到管理器（如果 id 不存在）。
     */
    public boolean addTask(Task task) {
        if (task == null || task.getTaskId() == null) return false;
        String id = task.getTaskId();
        if (tasks.containsKey(id)) return false;
        // 注册监听器
        task.setListener(this::notifyUpdated);
        tasks.put(id, task);
        notifyAdded(task);
        logger.info("Task {} added.", id);
        return true;
    }

    /**
     * 启动任务（将调用 Task.run()，并在完成或异常时通知监听器）。
     * 如果任务已在运行则返回 false。
     */
    public boolean startTask(String taskId) {
        Task t = tasks.get(taskId);
        if (t == null) return false;
        if (futures.containsKey(taskId)) return false;

        // 兜底包装，捕获任何未被任务内部处理的异常
        Future<?> f = executor.submit(() -> {
            try {
                t.run();
            } catch (Throwable ex) {
                handleUncaughtTaskException(taskId, ex);
            }
        });
        futures.put(taskId, f);

        // 在一个单独的线程中监视完成以便在结束时通知更新并移除 future
        executor.submit(() -> {
            try {
                f.get(); // 等待任务结束（阻塞），会抛出异常或 CancellationException
            } catch (Exception ignored) {
            } finally {
                futures.remove(taskId);
                notifyUpdated(t);
            }
        });

        return true;
    }

    /**
     * 暂停任务（用户主动暂停）
     * @param taskId task ID
     * @return 是否成功
     */
    public boolean suspendTask(String taskId) {
        Task t = tasks.get(taskId);
        if (t == null) return false;
        try {
            Task.Status s = t.getStatus();
            if (s == Task.Status.PAUSED) {
                notifyUpdated(t);
                return true;
            }
            t.setProcessDescription("已暂停，等待用户恢复");
            // 通过 Task 的 API 请求暂停并唤醒内部等待
            t.requestPause();
            notifyUpdated(t);
            return true;
        } catch (Exception e) {
            logger.error("Failed to suspend task {}", taskId, e);
            return false;
        }
    }

    /**
     * 尝试停止正在运行的任务（协作式 + Future.cancel）。
     * 先调用 Task.requestStop() 唤醒并让任务自行退出，再取消 Future 以中断阻塞 I/O 等。
     */
    public boolean stopTask(String taskId) {
        Task t = tasks.get(taskId);
        if (t != null) {
            try {
                t.requestStop();
            } catch (Exception ignored) {}
        }
        Future<?> f = futures.remove(taskId);
        boolean cancelled = false;
        if (f != null) {
            try {
                cancelled = f.cancel(true);
            } catch (Exception e) {
                logger.warn("Cancel future failed for task {}", taskId, e);
            }
            logger.info("Cancel task {} -> {}", taskId, cancelled);
        }
        if (t != null) notifyUpdated(t);
        return cancelled || t != null;
    }

    /**
     * 移除任务（如果正在运行则先取消），并通知监听器。
     */
    public boolean removeTask(String taskId) {
        if (taskId == null) return false;
        stopTask(taskId);
        Task removed = tasks.remove(taskId);
        futures.remove(taskId);
        if (removed != null) {
            notifyRemoved(taskId);
            logger.info("Task {} removed.", taskId);
            return true;
        }
        return false;
    }

    /**
     * 清空所有任务并取消正在运行的任务。
     */
    public void clearAll() {
        for (Future<?> f : futures.values()) {
            try {
                f.cancel(true);
            } catch (Exception ignored) {
            }
        }
        futures.clear();
        tasks.clear();
        notifyAllCleared();
        logger.info("All tasks cleared.");
    }

    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasks.values()));
    }

    /**
     * 更新任务的进度（GUI 用），并通知监听器刷新显示。
     */
    public void updateTaskProgress(String taskId, int completed, int total) {
        Task t = tasks.get(taskId);
        if (t == null) return;
        t.setTotalTasks(total);
        t.setTasksCompleted(completed);
        notifyUpdated(t);
    }

    /**
     * 查询所有处于等待用户交互的任务
     */
    public List<Task> getWaitingTasks() {
        List<Task> res = new ArrayList<>();
        for (Task t : tasks.values()) {
            if (t.isWaitingForUser()) res.add(t);
        }
        return res;
    }

    /**
     * 判断指定任务当前是否在等待用户操作
     */
    public boolean isTaskWaiting(String taskId) {
        Task t = tasks.get(taskId);
        return t != null && t.isWaitingForUser();
    }

    /**
     * 关闭管理器时释放线程池资源（程序退出时调用）。
     */
    public void shutdown() {
        try {
            executor.shutdownNow();
        } catch (Exception ignored) {
        }
    }

    /**
     * 兜底：处理任务线程未捕获的异常。将提示用户并停止任务。
     * @param taskId 任务 ID
     * @param ex 未捕获的异常
     */
    public void handleUncaughtTaskException(String taskId, Throwable ex) {
        logger.error("Uncaught exception in task {}: {}", taskId, ex.toString(), ex);
        Task t = tasks.get(taskId);
        if (t != null) {
            try {
                t.setStatus(Task.Status.ERROR);
                String msg = ex.getClass().getSimpleName();
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    msg += " - " + ex.getMessage();
                }
                t.setProcessDescription("发生未捕获异常：" + msg);
            } catch (Exception ignored) {}
        }
        for(Listener l : listeners) {
            try {
                l.onExceptionOccurred(taskId, ex);
            } catch (Exception ignored) {}
        }
        try {
            stopTask(taskId);
        } catch (Exception ignored) {}
    }
}
