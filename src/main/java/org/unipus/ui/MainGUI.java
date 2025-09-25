package org.unipus.ui;

import com.formdev.flatlaf.*;
import org.unipus.ui.theme.*;

import javax.swing.*;
import java.awt.*;

public class MainGUI {

    private static MainGUI mainGUI = null;

    private JFrame frame;

    public static LookAndFeel FLATLAF_LIGHT = new FlatLightLaf();
    public static LookAndFeel FLATLAF_DARK = new FlatDarkLaf();
    public static LookAndFeel FLATLAF_INTELLIJ = new FlatIntelliJLaf();

    public static LookAndFeel LIGHT_THEME = new LightTheme();
    public static LookAndFeel DARK_THEME = new DarkTheme();

    // 单独的调试窗口（选项卡：日志 / 网络）
    private JFrame debugFrame;
    private JTabbedPane debugTabs;
    private LogPanel logPanel;
    private NetworkActivityPanel networkPanel;

    public void setup(LookAndFeel theme) {
        try {
            UIManager.setLookAndFeel(theme);
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        frame = new JFrame("Unipus Helper Pro");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 480);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainGUI.class.getResource("/icon/icon.png")));
    }

    private MainGUI() {
        // 确保日志从程序启动起就捕获（全局 Appender + 历史缓冲）
        LogPanel.ensureGlobalLogCapture();

        setup(LIGHT_THEME);

        SwingUtilities.invokeLater(() -> {
            // 主内容：中心使用任务管理面板
            initMainContent();
            // 菜单 & 快捷键
            initMenus();
            initKeyBindings();
            frame.setVisible(true);
        });
    }

    private void initMainContent() {
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(TaskManagerPanel.getInstance(), BorderLayout.CENTER);
    }

    private void initDebugWindow() {
        logPanel = new LogPanel();
        networkPanel = new NetworkActivityPanel();

        debugTabs = new JTabbedPane(JTabbedPane.TOP);
        debugTabs.addTab("日志", logPanel);
        debugTabs.addTab("网络", networkPanel);

        debugFrame = new JFrame("调试");
        debugFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        debugFrame.getContentPane().add(debugTabs, BorderLayout.CENTER);
        debugFrame.setSize(1100, 750);
        debugFrame.setLocationRelativeTo(frame);
        // 注意：不在启动时创建或显示，延迟到首次打开
    }

    public void setTheme(LookAndFeel theme) {
        try {
            UIManager.setLookAndFeel(theme);
            // 更新所有窗口的组件 UI，确保子组件也应用主题
            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
            // 特殊：TaskManagerPanel 自定义背景需要手动刷新
            TaskManagerPanel.getInstance().applyThemeFromUI();
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    private void initMenus() {
        JMenuBar menuBar = new JMenuBar();

        // 设置 -> 外观
        JMenu settingsMenu = new JMenu("设置");
        JMenu appearanceSub = new JMenu("外观");
        JRadioButtonMenuItem lightItem = new JRadioButtonMenuItem("浅色模式", true);
        JRadioButtonMenuItem darkItem = new JRadioButtonMenuItem("深色模式");
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightItem);
        themeGroup.add(darkItem);
        lightItem.addActionListener(e -> setTheme(LIGHT_THEME));
        darkItem.addActionListener(e -> setTheme(DARK_THEME));
        appearanceSub.add(lightItem);
        appearanceSub.add(darkItem);
        settingsMenu.add(appearanceSub);

        // 调试 -> 日志 / 网络（单独窗口中选择对应标签页）
        JMenu debugMenu = new JMenu("调试");
        JMenuItem logItem = new JMenuItem("日志 (F6)");
        JMenuItem netItem = new JMenuItem("网络 (F12)");
        logItem.addActionListener(e -> showDebugWindow(0));
        netItem.addActionListener(e -> showDebugWindow(1));
        debugMenu.add(logItem);
        debugMenu.add(netItem);

        menuBar.add(settingsMenu);
        menuBar.add(debugMenu);
        frame.setJMenuBar(menuBar);
    }

    private void initKeyBindings() {
        JRootPane root = frame.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke("F6"), "showLogs");
        am.put("showLogs", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { showDebugWindow(0); }
        });

        im.put(KeyStroke.getKeyStroke("F12"), "showNetwork");
        am.put("showNetwork", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { showDebugWindow(1); }
        });
    }

    private void showDebugWindow(int tabIndex) {
        if (debugFrame == null) {
            initDebugWindow();
        }
        if (debugFrame == null) return;
        if (tabIndex >= 0 && tabIndex < debugTabs.getTabCount()) {
            debugTabs.setSelectedIndex(tabIndex);
        }
        if (!debugFrame.isVisible()) {
            // 尽量放在主窗口右侧，避免重叠
            tryPlaceDebugRightOfMain();
            debugFrame.setVisible(true);
        }
        debugFrame.toFront();
        debugFrame.requestFocus();
    }

    private void tryPlaceDebugRightOfMain() {
        if (frame == null || debugFrame == null) return;
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int desiredX = frame.getX() + frame.getWidth() + 12;
        int desiredY = frame.getY();
        int w = debugFrame.getWidth();
        int h = debugFrame.getHeight();
        // 如果右侧放不下，则尝试放在左侧；否则相对主窗居中
        if (desiredX + w <= screen.x + screen.width) {
            debugFrame.setLocation(desiredX, Math.max(screen.y, desiredY));
        } else if (frame.getX() - 12 - w >= screen.x) {
            debugFrame.setLocation(frame.getX() - 12 - w, Math.max(screen.y, desiredY));
        } else {
            debugFrame.setLocationRelativeTo(frame);
        }
    }

    public void warnPopup(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public void infoPopup(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public synchronized static MainGUI getInstance() {
        if(mainGUI == null) {
            mainGUI = new MainGUI();
        }
        return mainGUI;
    }
}
