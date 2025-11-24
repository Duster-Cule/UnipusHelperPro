package org.unipus.ui;

/* (っ*´Д`)っ 小代码要被看光啦 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unipus.log.Reporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * 报告导出对话框
 * 用于生成和导出日志、网络请求等信息的报告
 */
public class ReportDialog extends JDialog {

    private JTextField filePathField;
    private JButton browseButton;
    private JButton generateButton;
    private JButton cancelButton;
    private JEditorPane descriptionArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private static final Logger logger = LogManager.getLogger(ReportDialog.class);

    public ReportDialog(JFrame parent) {
        super(parent, "生成报告", true);
        initComponents();
        layoutComponents();
        initListeners();

        setSize(600, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // 说明文本区域
        descriptionArea = new JEditorPane();
        descriptionArea.setContentType("text/html");
        descriptionArea.setText(
                """
                <html><body style='font-size: 12px; margin: 0; padding: 0;'>
                <p style='margin-top: 0;'>生成一份报告，用于开发者调试以及问题排查。将生成一个zip压缩包。<br>
                程序内部对手机号和姓名以及其他敏感信息进行了替换处理，但仍建议您在发送报告前自行检查内容，确保不包含任何隐私信息。</p>
                <p>请尽量使用邮箱的方式向开发者发送报告，以免将个人信息公布于众。</p>
                <p>向开发者报告时，请在Github Issues中创建一个新的Issue，并将报告文件作为附件通过邮箱发送到 <b>1362105606@qq.com</b>.<br>
                感谢您为开源社区做出的贡献！</p>
                </body></html>"""
        );
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setHighlighter(null);
        descriptionArea.setBackground(UIManager.getColor("Panel.background"));
        descriptionArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        // 确保滚动条在顶部
        descriptionArea.setCaretPosition(0);

        // 文件路径选择
        filePathField = new JTextField();
        filePathField.setText(getDefaultFilePath());

        browseButton = new JButton("浏览...");

        // 进度条和状态标签
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);

        // 按钮
        generateButton = new JButton("生成报告");
        generateButton.setPreferredSize(new Dimension(100, 30));

        cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(100, 30));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // 顶部：说明区域
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(580, 150));
        descScrollPane.setBorder(BorderFactory.createTitledBorder("报告说明"));
        // 确保滚动条在顶部
        SwingUtilities.invokeLater(() -> {
            descScrollPane.getVerticalScrollBar().setValue(0);
            descScrollPane.getHorizontalScrollBar().setValue(0);
        });

        // 中间：选项面板
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("导出选项"));

        // 文件路径选择行
        JPanel filePathPanel = new JPanel(new BorderLayout(5, 0));
        filePathPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        JLabel pathLabel = new JLabel("保存位置：");
        filePathPanel.add(pathLabel, BorderLayout.WEST);
        filePathPanel.add(filePathField, BorderLayout.CENTER);
        filePathPanel.add(browseButton, BorderLayout.EAST);

        // 复选框面板
        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        checkBoxPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // 状态和进度条面板
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        optionsPanel.add(filePathPanel);
        optionsPanel.add(checkBoxPanel);
        optionsPanel.add(statusPanel);

        // 底部：按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);

        // 添加到主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(descScrollPane, BorderLayout.NORTH);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        // 浏览按钮 - 选择保存位置
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(getDefaultFilePath()));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // 生成按钮 - 生成并保存报告
        generateButton.addActionListener(e -> {
            try {
                Reporter.generateAndSaveReports(filePathField.getText());
                logger.info("Report generated at {}", filePathField.getText());
                JOptionPane.showMessageDialog(this,
                        "报告已成功生成并保存到:\n" + filePathField.getText(),
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IOException ex) {
                logger.error("Failed to save log file : ", ex);
                JOptionPane.showMessageDialog(this,
                        "保存文件时出错: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        });

        // 取消按钮 - 关闭对话框
        cancelButton.addActionListener(e -> {
            dispose();
        });
    }

    private String getDefaultFilePath() {
        String userHome = System.getProperty("user.dir");
        String fileName = "unipus_report_" + System.currentTimeMillis() + ".zip";
        return new File(userHome, fileName).getAbsolutePath();
    }

    /**
     * 显示报告对话框
     * @param frame 父窗口
     */
    public static void showReportDialog(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            ReportDialog dialog = new ReportDialog(frame);
            dialog.setVisible(true);
        });
    }
}
