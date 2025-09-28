package org.unipus.ui;

/* (っ*´Д`)っ 小代码要被看光啦 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unipus.exceptions.LoginException;
import org.unipus.exceptions.TaskInitFailedException;
import org.unipus.unipus.Task;
import org.unipus.unipus.TaskManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaskManagerPanel extends JPanel {

    private static final Logger log = LogManager.getLogger(TaskManagerPanel.class);
    private final JPanel listPanel;
    private final JScrollPane scrollPane;
    private final Map<String, TaskPanel> panels = new LinkedHashMap<>();
    private static TaskManagerPanel taskManagerPanel = null;

    // 新增：用于在有/无任务时切换显示的容器和按钮
    private final JPanel centerCards;
    private final static String CARD_LIST = "LIST";
    private final static String CARD_EMPTY = "EMPTY";
    private final JButton addTaskButton;

    // 新增：保留空态和底部栏引用以便主题刷新
    private final JPanel emptyPanel;
    private final JPanel southPanel;

    public static synchronized TaskManagerPanel getInstance() {
        if (taskManagerPanel == null) {
            taskManagerPanel = new TaskManagerPanel();
        }
        return taskManagerPanel;
    }

    private TaskManagerPanel() {
        setLayout(new BorderLayout());

        // 主题背景色
        Color bg = UIManager.getColor("Panel.background");

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(bg);

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);

        // 中心卡片面板：list / empty
        centerCards = new JPanel(new CardLayout());
        centerCards.add(scrollPane, CARD_LIST);

        // empty 面板，居中显示添加按钮
        emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(bg);
        JButton emptyAdd = new JButton("添加任务");
        emptyAdd.addActionListener(e -> openLoginDialog());
        JLabel emptyLabel = new JLabel("当前还没有任务，请");
        emptyLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        emptyPanel.add(emptyLabel);
        emptyPanel.add(emptyAdd);
        centerCards.add(emptyPanel, CARD_EMPTY);

        add(centerCards, BorderLayout.CENTER);

        // 下方固定的添加按钮（在有任务时显示）
        addTaskButton = new JButton("添加任务");
        addTaskButton.addActionListener(e -> openLoginDialog());
        addTaskButton.setVisible(false); // 初始无任务时隐藏
        southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(bg);
        southPanel.add(addTaskButton);
        add(southPanel, BorderLayout.SOUTH);

        TaskManager tm = TaskManager.getInstance();
        tm.addListener(new TaskManager.Listener() {
            @Override
            public void onTaskAdded(Task task) {
                SwingUtilities.invokeLater(() -> addTask(task));
            }

            @Override
            public void onTaskRemoved(String taskId) {
                SwingUtilities.invokeLater(() -> removeTask(taskId));
            }

            @Override
            public void onTaskUpdated(Task task) {
                SwingUtilities.invokeLater(() -> updateTask(task));
            }

            @Override
            public void onAllCleared() {
                SwingUtilities.invokeLater(() -> clearAll());
            }

            @Override
            public void onExceptionOccurred(String taskId, Throwable ex) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String title = "任务异常";
                        String sb = "任务 " + taskId + " 发生未捕获异常\n" +
                                ex.toString() + "\n\n任务已停止。";
                        JOptionPane.showMessageDialog(null, sb, title, JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ignored) {}
                });
            }
        });

        // 初始化时把已存在的任务加入面板
        for (Task t : tm.getAllTasks()) {
            addTask(t);
        }

        updateViewState();
    }

    // 主题切换时调用，更新自定义背景色为当前 LAF 的默认值
    public void applyThemeFromUI() {
        Color bg = UIManager.getColor("Panel.background");
        listPanel.setBackground(bg);
        emptyPanel.setBackground(bg);
        southPanel.setBackground(bg);
        revalidate();
        repaint();
    }

    private void updateViewState() {
        CardLayout cl = (CardLayout) centerCards.getLayout();
        if (panels.isEmpty()) {
            cl.show(centerCards, CARD_EMPTY);
            addTaskButton.setVisible(false);
        } else {
            cl.show(centerCards, CARD_LIST);
            addTaskButton.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void openLoginDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, "登录并添加任务", Dialog.ModalityType.APPLICATION_MODAL);
        LoginPanel loginPanel = new LoginPanel();
        loginPanel.addLoginListener(e -> {
            String username = loginPanel.getUsername();
            String password = loginPanel.getPassword();
            try {
                TaskManager.getInstance().createAndAddTask("task-" + username, username, password);
            } catch (LoginException ex) {
                JOptionPane.showMessageDialog(dialog, "登录失败，请检查账号密码", "登录失败", JOptionPane.ERROR_MESSAGE);
            } catch (TaskInitFailedException ex) {
                JOptionPane.showMessageDialog(dialog, "任务初始化失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
            dialog.dispose();
        });
        loginPanel.addCancelListener(e -> dialog.dispose());
        dialog.setContentPane(loginPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * 添加任务行。如果 id 已存在则忽略。
     */
    public void addTask(Task task) {
        if (task == null || task.getTaskId() == null) return;
        if (panels.containsKey(task.getTaskId())) return;

        TaskPanel tp = new TaskPanel(task, e -> showManageMenu(task));
        tp.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = tp.getPreferredSize();
        tp.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
        panels.put(task.getTaskId(), tp);

        tp.showStartButton(e -> {
            boolean ok = TaskManager.getInstance().startTask(task.getTaskId());
            if (ok) tp.hideStartButton();
        });

        listPanel.add(tp);
        listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        updateViewState();
    }

    private void showManageMenu(Task task) {
        // 使用自定义面板和模态对话框展示任务详情与管理按钮
        if (task == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, "任务详情与管理", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 详情信息区
        JPanel details = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        JLabel idValue = new JLabel(task.getTaskId());
        JLabel nameValue = new JLabel(task.getName());
        JLabel statusValue = new JLabel(task.getStatus() != null ? task.getStatus().name() : "");
        JLabel progressValue = new JLabel(task.getTasksCompleted() + "/" + task.getTotalTasks() +
                " (" + task.getProgressPercent() + "%)");
        String userStr = "";
        if (task.getUser() != null) {
            String uname = task.getUser().getUsername() != null ? task.getUser().getUsername() : "";
            String real = task.getUser().getName() != null ? task.getUser().getName() : "";
            String sch = task.getUser().getSchoolName() != null ? task.getUser().getSchoolName() : "";
            userStr = String.join(" ", new String[]{uname, real.isEmpty()?"":("/ " + real), sch.isEmpty()?"":("/ " + sch)}).trim();
        }
        JLabel userValue = new JLabel(userStr);
        JLabel descValue = new JLabel(task.getProcessDescription() != null ? task.getProcessDescription() : "");

        int row = 0;
        addDetailRow(details, gbc, row++, "任务 ID:", idValue);
        addDetailRow(details, gbc, row++, "名称:", nameValue);
        addDetailRow(details, gbc, row++, "状态:", statusValue);
        addDetailRow(details, gbc, row++, "进度:", progressValue);
        addDetailRow(details, gbc, row++, "用户:", userValue);
        addDetailRow(details, gbc, row++, "描述:", descValue);

        content.add(details, BorderLayout.CENTER);

        // 按钮区：左侧红色“停止任务”，右侧其余按钮
        JButton stopBtn = new JButton("停止任务");
        stopBtn.setForeground(Color.WHITE);
        stopBtn.setBackground(new Color(0xD93025));
        stopBtn.setOpaque(true);
        stopBtn.setBorderPainted(false);

        JButton pauseResumeBtn = new JButton();
        JButton removeBtn = new JButton("移除");
        JButton cancelBtn = new JButton("取消");

        Runnable refresh = () -> {
            Task current = TaskManager.getInstance().getTask(task.getTaskId());
            if (current == null) {
                statusValue.setText("已移除");
                pauseResumeBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                removeBtn.setEnabled(false);
                return;
            }
            statusValue.setText(current.getStatus() != null ? current.getStatus().name() : "");
            progressValue.setText(current.getTasksCompleted() + "/" + current.getTotalTasks() +
                    " (" + current.getProgressPercent() + "%)");
            descValue.setText(current.getProcessDescription() != null ? current.getProcessDescription() : "");
            Task.Status s = current.getStatus();
            boolean canResume = (s == Task.Status.PAUSED || s == Task.Status.NEED_OPERATION || s == Task.Status.WAITING);
            boolean canPause = (s == Task.Status.RUNNING);
            boolean canStart = (s == Task.Status.READY);
            if (canPause) {
                pauseResumeBtn.setText("暂停");
                pauseResumeBtn.setEnabled(true);
            } else if (canStart) {
                pauseResumeBtn.setText("启动");
                pauseResumeBtn.setEnabled(true);
            } else if (canResume) {
                pauseResumeBtn.setText("恢复");
                pauseResumeBtn.setEnabled(true);
            } else {
                pauseResumeBtn.setText("恢复");
                pauseResumeBtn.setEnabled(false);
            }
            stopBtn.setEnabled(s == Task.Status.RUNNING || s == Task.Status.NEED_OPERATION || s == Task.Status.WAITING);
        };

        pauseResumeBtn.addActionListener(e -> {
            Task current = TaskManager.getInstance().getTask(task.getTaskId());
            if (current == null) return;
            Task.Status s = current.getStatus();
            try {
                if (s == Task.Status.RUNNING) {
                    TaskManager.getInstance().suspendTask(current.getTaskId());
                } else if (s == Task.Status.READY) {
                    // READY 状态下“启动”
                    TaskManager.getInstance().startTask(current.getTaskId());
                } else if (s == Task.Status.PAUSED || s == Task.Status.NEED_OPERATION || s == Task.Status.WAITING) {
                    current.resumeTask();
                }
            } catch (Exception ex) {
                log.error("Pause/Resume/Start failed for task {}", current.getTaskId(), ex);
                JOptionPane.showMessageDialog(dialog, "操作失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
            refresh.run();
        });

        stopBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(dialog, "确定要停止该任务吗？", "确认停止", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                boolean ok = TaskManager.getInstance().stopTask(task.getTaskId());
                if (!ok) {
                    JOptionPane.showMessageDialog(dialog, "未能停止任务或任务已不在运行。", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                refresh.run();
            }
        });

        removeBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(dialog, "确定移除该任务？（若在运行将先尝试停止）", "确认移除", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                boolean ok = TaskManager.getInstance().removeTask(task.getTaskId());
                log.info("Task {} removed by user via details dialog: {}", task.getTaskId(), ok);
                dialog.dispose();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(stopBtn);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(pauseResumeBtn);
        right.add(removeBtn);
        right.add(cancelBtn);
        JPanel south = new JPanel(new BorderLayout());
        south.add(left, BorderLayout.WEST);
        south.add(right, BorderLayout.EAST);

        content.add(south, BorderLayout.SOUTH);

        // 注册临时监听器以便动态刷新/移除时关闭
        TaskManager.Listener tmpListener = new TaskManager.Listener() {
            @Override
            public void onTaskAdded(Task t) {}
            @Override
            public void onTaskRemoved(String taskId) {
                if (task.getTaskId().equals(taskId)) {
                    SwingUtilities.invokeLater(dialog::dispose);
                }
            }
            @Override
            public void onTaskUpdated(Task t) {
                if (t != null && task.getTaskId().equals(t.getTaskId())) {
                    SwingUtilities.invokeLater(refresh);
                }
            }
            @Override
            public void onAllCleared() {
                SwingUtilities.invokeLater(dialog::dispose);
            }
            @Override
            public void onExceptionOccurred(String taskId, Throwable ex) {
                if (task.getTaskId().equals(taskId)) SwingUtilities.invokeLater(refresh);
            }
        };
        TaskManager.getInstance().addListener(tmpListener);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                TaskManager.getInstance().removeListener(tmpListener);
            }
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                TaskManager.getInstance().removeListener(tmpListener);
            }
        });

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(Math.max(dialog.getWidth(), 520), dialog.getHeight()));
        dialog.setLocationRelativeTo(owner);
        refresh.run();
        dialog.setVisible(true);
    }

    private static void addDetailRow(JPanel details, GridBagConstraints base, int row, String label, JComponent comp) {
        GridBagConstraints g1 = (GridBagConstraints) base.clone();
        g1.gridx = 0;
        g1.gridy = row;
        g1.weightx = 0;
        g1.gridwidth = 1;
        g1.fill = GridBagConstraints.NONE;
        JLabel l = new JLabel(label);
        l.setForeground(new Color(0x555555));
        details.add(l, g1);

        GridBagConstraints g2 = (GridBagConstraints) base.clone();
        g2.gridx = 1;
        g2.gridy = row;
        g2.weightx = 1;
        g2.gridwidth = 1;
        g2.fill = GridBagConstraints.HORIZONTAL;
        details.add(comp, g2);
    }

    /**
     * 根据 id 移除任务行
     */
    public void removeTask(String id) {
        if (id == null) return;
        TaskPanel tp = panels.remove(id);
        if (tp != null) {
            rebuildList();
        }
        updateViewState();
    }

    private void rebuildList() {
        listPanel.removeAll();
        for (TaskPanel tp : panels.values()) {
            tp.setAlignmentX(Component.LEFT_ALIGNMENT);
            Dimension pref = tp.getPreferredSize();
            tp.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
            listPanel.add(tp);
            listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        revalidate();
        repaint();
    }

    /**
     * 更新任务显示（名称与进度）
     */
    public void updateTask(Task task) {
        if (task == null || task.getTaskId() == null) return;
        TaskPanel tp = panels.get(task.getTaskId());
        if (tp != null) {
            tp.setFromTask(task);
            if (task.getStatus() == org.unipus.unipus.Task.Status.READY) {
                tp.showStartButton(e -> {
                    boolean ok = TaskManager.getInstance().startTask(task.getTaskId());
                    if (ok) tp.hideStartButton();
                });
            } else {
                tp.hideStartButton();
            }
        }
    }

    public void clearAll() {
        panels.clear();
        listPanel.removeAll();
        updateViewState();
    }

    public void warnPopup() {
        warnPopup("警告", "发生警告，请检查日志或任务详情。");
    }

    /**
     * 弹出一个警告弹窗，默认标题为“警告”。
     */
    public void warnPopup(String message) {
        warnPopup("警告", message);
    }

    /**
     * 弹出一个警告弹窗，包含自定义标题与内容。
     */
    public void warnPopup(String title, String message) {
        showMessageDialog(title, message, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 弹出一个信息提示弹窗，默认标题为“提示”。
     */
    public void infoPopup(String message) {
        infoPopup("提示", message);
    }

    /**
     * 弹出一个信息提示弹窗，包含自定义标题与内容。
     */
    public void infoPopup(String title, String message) {
        showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 统一的消息框展示，确保在 EDT 上显示，并居中到当前面板所属窗口。
     */
    private void showMessageDialog(String title, String message, int messageType) {
        Runnable r = () -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JOptionPane.showMessageDialog(owner, message, title, messageType);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}