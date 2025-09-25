package org.unipus.ui;

import org.unipus.unipus.Task;
import org.unipus.unipus.TaskManager;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class TaskPanel extends JPanel {

    private final JLabel nameLabel;
    private final JLabel subtitleLabel; // user.name + 简短描述
    private final JLabel statusLabel;
    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final JButton manageButton;
    private JButton startButton;

    public TaskPanel(Task task, ActionListener manageAction) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 8, 12));

        // 顶部：任务名 + 管理按钮
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        nameLabel = new JLabel(task != null ? task.getName() : "");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 14f));
        topRow.add(nameLabel, BorderLayout.WEST);

        // 状态显示放在管理按钮左侧
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightGroup.setOpaque(false);
        statusLabel = new JLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        rightGroup.add(statusLabel);

        startButton = new JButton("启动");
        startButton.setFocusable(false);
        startButton.setVisible(false);
        startButton.setBorder(new RoundedBorder(8));
        rightGroup.add(startButton);

        manageButton = new JButton("管理");
        manageButton.setFocusable(false);
        if (manageAction != null) manageButton.addActionListener(manageAction);
        manageButton.setBorder(new RoundedBorder(8));
        rightGroup.add(manageButton);

        topRow.add(rightGroup, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        // 中间：subtitle（用户姓名 + 描述）和进度条一行
        JPanel middle = new JPanel(new BorderLayout(8, 6));
        middle.setOpaque(false);

        subtitleLabel = new JLabel();
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setForeground(new Color(0x666666));
        middle.add(subtitleLabel, BorderLayout.NORTH);

        JPanel progressRow = new JPanel(new BorderLayout(8, 0));
        progressRow.setOpaque(false);
        progressLabel = new JLabel();
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 12f));
        progressRow.add(progressLabel, BorderLayout.WEST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(200, 12));
        progressBar.setForeground(new Color(0xF28C2B));
        progressBar.setBackground(new Color(0xEDEDED));
        progressRow.add(progressBar, BorderLayout.CENTER);

        middle.add(progressRow, BorderLayout.CENTER);

        add(middle, BorderLayout.CENTER);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(0xCCCCCC));
        sep.setPreferredSize(new Dimension(0, 8));
        add(sep, BorderLayout.SOUTH);

        // 初始化显示
        if (task != null) setFromTask(task);
    }

    /**
     * 根据 Task 更新面板各项显示（名称、用户、描述、状态、进度）
     */
    public void setFromTask(Task task) {
        if (task == null) return;
        nameLabel.setText(task.getName());
        String uname = (task.getUser() != null && task.getUser().getName() != null) ? task.getUser().getName() : task.getUser() != null ? task.getUser().getUsername() : "";
        String desc = task.getProcessDescription() != null ? task.getProcessDescription() : "";
        String subtitle = "";
        if (!uname.isEmpty()) subtitle += uname;
        if (!desc.isEmpty()) {
            if (!subtitle.isEmpty()) subtitle += " — ";
            subtitle += desc;
        }
        subtitleLabel.setText(subtitle);

        // 状态文本与颜色
        Task.Status s = task.getStatus();
        statusLabel.setText(s.name());
        statusLabel.setForeground(statusColor(s));
        if (s == Task.Status.NEED_OPERATION) {
            statusLabel.setText("<html><u>需要操作</u></html>");
            statusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            UserOprationPanel userOprationPanel = new UserOprationPanel();

            switch (task.getUserOpration()) {
                case CHOOSE_COURSE ->
                        userOprationPanel.initChooseCoursePanel(task.getCourseList(), (course, resource) -> {
                            task.setCurrentCourse(course);
                            task.setCurrentCourseResource(resource);
                            task.resumeTask();
                        });
            }
        }

        // 进度
        setProgress(task.getTasksCompleted(), task.getTotalTasks());
    }

    private Color statusColor(Task.Status s) {
        if (s == null) return Color.GRAY;
        return switch (s) {
            case RUNNING -> new Color(0x1EAA2B);
            case NEED_OPERATION -> new Color(0xF2A900);
            case PAUSED -> new Color(0x888888);
            case COMPLETED -> new Color(0x2B8CF2);
            case ERROR -> new Color(0xD93025);
            default -> Color.GRAY;
        };
    }

    /**
     * 更新进度显示：完成数与总数，进度条以百分比显示。
     */
    public void setProgress(int completed, int total) {
        if (completed < 0) completed = 0;
        if (total < 0) total = 0;
        progressLabel.setText(String.format("进度： %d/%d", completed, total));
        int pct = (total <= 0) ? 0 : (int) ((completed * 100L) / total);
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        progressBar.setValue(pct);
        repaint();
    }

    public void setNameText(String name) {
        nameLabel.setText(name);
    }

    public JButton getManageButton() {
        return manageButton;
    }

    // 新增：显示启动按钮并设置点击事件
    public void showStartButton(ActionListener action) {
        startButton.setVisible(true);
        for (ActionListener l : startButton.getActionListeners()) {
            startButton.removeActionListener(l);
        }
        if (action != null) startButton.addActionListener(action);
        revalidate();
        repaint();
    }

    // 新增：隐藏启动按钮
    public void hideStartButton() {
        startButton.setVisible(false);
        for (ActionListener l : startButton.getActionListeners()) {
            startButton.removeActionListener(l);
        }
        revalidate();
        repaint();
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
    }
}
