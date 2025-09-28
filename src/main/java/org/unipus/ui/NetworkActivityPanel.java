package org.unipus.ui;

/* (っ*´Д`)っ 小代码要被看光啦 */

import org.unipus.web.RequestManager;
import org.unipus.web.RequestManager.RequestRecord;
import org.unipus.web.RequestManager.CookieInfo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * 网络活动面板 - 类似 Chrome DevTools Network 面板
 * 用于显示和调试 RequestManager 中的所有 HTTP 请求
 */
public class NetworkActivityPanel extends JPanel implements RequestManager.RequestListener {

    private final RequestTableModel tableModel;
    private final JTable requestTable;
    private final TableRowSorter<RequestTableModel> sorter;
    private final JSplitPane mainSplitPane;
    private final JTabbedPane detailTabs;
    private final JTextField searchField;
    private final JComboBox<String> methodFilter;
    private final JComboBox<String> statusFilter;

    // 新增：直接引用各标签页中的文本区域，避免层级查找/组件丢失
    private JTextArea requestHeadersArea;
    private JTextArea responseHeadersArea;
    private JTextArea cookiesArea;
    // 新增：请求/响应正文页签的文本域
    private RSyntaxTextArea requestPayloadArea;
    private RSyntaxTextArea responseBodyArea;

    // 状态栏
    private final JLabel statusLabel;
    private final JLabel countLabel;

    // 当前选中的请求
    private RequestRecord selectedRecord;

    // General 面板中每一行的固定高度
    private static final int GENERAL_ROW_HEIGHT = 28;

    public NetworkActivityPanel() {
        setLayout(new BorderLayout());

        // 初始化组件
        tableModel = new RequestTableModel();
        requestTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        requestTable.setRowSorter(sorter);
        // 强制以请求时间（记录插入顺序）为顺序：禁用所有列的排序切换
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }
        sorter.setSortKeys(null); // 不使用任何列排序，保持模型插入顺序

        detailTabs = new JTabbedPane();
        searchField = new JTextField(20);
        methodFilter = new JComboBox<>(new String[]{"All", "GET", "POST", "PUT", "DELETE", "PATCH"});
        statusFilter = new JComboBox<>(new String[]{"All", "2xx", "3xx", "4xx", "5xx", "Error"});

        statusLabel = new JLabel("Ready");
        countLabel = new JLabel("0 requests");

        // 创建分割面板
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        initializeComponents();
        setupEventHandlers();

        // 注册为 RequestManager 的监听器
        RequestManager.getInstance().addListener(this);

        // 加载现有的请求记录
        loadExistingRecords();
        // 首次自动调整列宽
        SwingUtilities.invokeLater(this::autoResizeColumns);
    }

    private void initializeComponents() {
        // =============== 工具栏 ===============
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // =============== 请求表格 ===============
        setupRequestTable();
        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));
        // 重要：当列内容过长时出现水平滚动条，而不是撑大整个窗口
        tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // =============== 详细信息面板 ===============
        setupDetailPanel();

        // =============== 分割面板 ===============
        // 删除右侧区域：底部直接放置左侧标签页
        mainSplitPane.setTopComponent(tableScrollPane);
        mainSplitPane.setBottomComponent(detailTabs);
        mainSplitPane.setResizeWeight(0.6);

        add(mainSplitPane, BorderLayout.CENTER);

        // =============== 状态栏 ===============
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        // 清除按钮
        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear all requests");
        clearButton.addActionListener(e -> {
            RequestManager.getInstance().clear();
            selectedRecord = null;
            updateDetailPanel();
        });

        // 导出按钮
        JButton exportButton = new JButton("Export");
        exportButton.setToolTipText("Export requests to file");
        exportButton.addActionListener(e -> exportRequests());

        // 捕获上限按钮（设置 Response.peekBody 上限）
        JButton peekLimitButton = new JButton("捕获上限: " + formatBytes(RequestManager.getResponsePeekLimitBytes()));
        peekLimitButton.setToolTipText("设置网络记录的响应体捕获上限（用于记录，不影响业务读取）。支持 B/KB/MB/GB，例如 512KB, 10MB。");
        peekLimitButton.addActionListener(e -> openPeekLimitDialog(peekLimitButton));

        // 搜索框
        searchField.setToolTipText("Filter by URL, method, or status");

        // 过滤器
        methodFilter.setToolTipText("Filter by HTTP method");
        statusFilter.setToolTipText("Filter by status code");

        toolbar.add(clearButton);
        toolbar.add(exportButton);
        toolbar.add(peekLimitButton);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Method:"));
        toolbar.add(methodFilter);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Status:"));
        toolbar.add(statusFilter);

        return toolbar;
    }

    private void openPeekLimitDialog(JButton sourceButton) {
        long current = RequestManager.getResponsePeekLimitBytes();
        String currentText = formatBytes(current);
        String input = JOptionPane.showInputDialog(this,
                "设置响应体捕获上限（用于记录，不影响业务读取）。\n支持单位: B, KB, MB, GB。留空取消。",
                currentText);
        if (input == null) return; // 取消
        input = input.trim();
        if (input.isEmpty()) return;
        try {
            long bytes = parseSizeToBytes(input);
            if (bytes <= 0) throw new IllegalArgumentException("必须大于 0");
            long warnThreshold = 20L * 1024 * 1024; // 20MB
            if (bytes > warnThreshold) {
                int r = JOptionPane.showConfirmDialog(this,
                        "你设置了较大的捕获上限: " + formatBytes(bytes) + "\n可能占用较多内存，确定继续吗？",
                        "确认大上限",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.YES_OPTION) return;
            }
            RequestManager.setResponsePeekLimitBytes(bytes);
            sourceButton.setText("捕获上限: " + formatBytes(bytes));
            statusLabel.setText("Peek limit: " + formatBytes(bytes));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "输入无效: " + ex.getMessage() + "\n示例: 512KB, 10MB, 1GB",
                    "无效输入",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static long parseSizeToBytes(String input) {
        String s = input.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) throw new IllegalArgumentException("为空");
        long multiplier;
        if (s.endsWith("GB")) { multiplier = 1024L * 1024 * 1024; s = s.substring(0, s.length() - 2); }
        else if (s.endsWith("MB")) { multiplier = 1024L * 1024; s = s.substring(0, s.length() - 2); }
        else if (s.endsWith("KB")) { multiplier = 1024L; s = s.substring(0, s.length() - 2); }
        else if (s.endsWith("B")) { multiplier = 1L; s = s.substring(0, s.length() - 1); }
        else {
            // 无单位：按 MB 处理（兼容纯数字）
            multiplier = 1024L * 1024;
        }
        s = s.trim();
        double val = Double.parseDouble(s);
        if (val <= 0) throw new IllegalArgumentException("必须大于 0");
        double bytes = val * multiplier;
        if (bytes > Long.MAX_VALUE) throw new IllegalArgumentException("过大");
        return (long) bytes;
    }

    private void setupRequestTable() {
        // 设置表格样式，类似 Chrome DevTools
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(22);
        requestTable.setShowGrid(false);
        requestTable.setIntercellSpacing(new Dimension(0, 0));
        // 当列内容很长（如 URL、状态文本）时，不允许自动拉伸表格宽度，改为出现水平滚动条
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // 设置列宽
        int[] columnWidths = {80, 60, 250, 60, 80, 80, 80, 150};
        for (int i = 0; i < columnWidths.length && i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // 自定义渲染器
        requestTable.setDefaultRenderer(Object.class, new RequestTableCellRenderer());

        // 表头样式
        requestTable.getTableHeader().setBackground(new Color(240, 240, 240));
        requestTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
    }

    private void setupDetailPanel() {
        // 创建详细信息标签页
        JPanel generalTabPanel = createGeneralTab();
        JPanel requestHeadersTab = createHeadersTab(true);
        JPanel responseHeadersTab = createHeadersTab(false);
        JPanel cookiesTab = createCookiesTab();
        JPanel reqPayloadTab = createBodyTab(true);
        JPanel respBodyTab = createBodyTab(false);

        // 为 General 页签添加滚动条，防止内容过长溢出
        JScrollPane generalScroll = new JScrollPane(generalTabPanel);
        generalScroll.setBorder(BorderFactory.createEmptyBorder());
        generalScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        generalScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        detailTabs.addTab("General", generalScroll);
        detailTabs.addTab("Request Headers", requestHeadersTab);
        detailTabs.addTab("Response Headers", responseHeadersTab);
        detailTabs.addTab("Cookies", cookiesTab);
        detailTabs.addTab("Request Payload", reqPayloadTab);
        detailTabs.addTab("Response Body", respBodyTab);

        updateDetailPanel(); // 初始为空
    }

    private JPanel createGeneralTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private JPanel createHeadersTab(boolean isRequest) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(textArea);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);
        if (isRequest) {
            requestHeadersArea = textArea;
        } else {
            responseHeadersArea = textArea;
        }
        return panel;
    }

    // 限制首选宽度的容器，防止子组件(如长行文本域)把整个窗口首选宽度抬得过大
    private static class CappedPreferredSizePanel extends JPanel {
        private final int maxWidth;
        CappedPreferredSizePanel(LayoutManager layout, int maxWidth) {
            super(layout);
            this.maxWidth = maxWidth;
        }
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            if (d == null) return new Dimension(maxWidth, 0);
            if (d.width > maxWidth) return new Dimension(maxWidth, d.height);
            return d;
        }
    }

    // 根据父容器宽度动态收敛首选宽度的滚动面板，避免选项卡根据超长行扩大窗口
    private static class WidthCappedScrollPane extends RTextScrollPane {
        public WidthCappedScrollPane(RSyntaxTextArea textArea) {
            super(textArea);
        }
        private int computeWidthCap() {
            // 优先使用最近的 JTabbedPane 可用宽度（更贴近选项卡内容区）
            Container ancTabs = SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (ancTabs instanceof JTabbedPane tabs) {
                int w = tabs.getWidth();
                if (w > 0) return Math.max(400, w - 4); // 留出 2px 边距
            }
            // 次选：最近的 JViewport（如外部表格/滚动容器的视口宽度）
            Container ancVp = SwingUtilities.getAncestorOfClass(JViewport.class, this);
            if (ancVp instanceof JViewport vp) {
                int w = vp.getWidth();
                if (w > 0) return Math.max(400, w - 2);
            }
            // 再次：直接父容器宽度
            Container p = getParent();
            if (p != null && p.getWidth() > 0) return Math.max(400, p.getWidth() - 2);
            // 回退
            return 700;
        }
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            int cap = computeWidthCap();
            if (d.width > cap) return new Dimension(cap, d.height);
            return d;
        }
        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            int cap = computeWidthCap();
            if (d.width > cap) return new Dimension(cap, d.height);
            return d;
        }
        @Override
        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            int cap = computeWidthCap();
            if (d.width > cap) return new Dimension(cap, d.height);
            return d;
        }
    }

    private JPanel createBodyTab(boolean isRequestPayload) {
        // 使用带宽度上限的容器包裹滚动面板，避免内容过长导致 JTabbedPane/窗口过宽
        JPanel panel = new CappedPreferredSizePanel(new BorderLayout(), 1000);
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setEditable(false);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        // 重要：不自动换行，使用水平滚动条，避免撑大容器
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(false);
        // 控制首选尺寸，不随内容增长
        textArea.setColumns(1);
        textArea.setRows(18);
        WidthCappedScrollPane sp = new WidthCappedScrollPane(textArea);
        sp.setFoldIndicatorEnabled(true);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);
        if (isRequestPayload) {
            requestPayloadArea = textArea;
        } else {
            responseBodyArea = textArea;
        }
        return panel;
    }

    private JPanel createCookiesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(textArea);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);
        cookiesArea = textArea;
        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(countLabel, BorderLayout.EAST);
        return statusBar;
    }

    private void setupEventHandlers() {
        // 表格选择事件
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = requestTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = requestTable.convertRowIndexToModel(selectedRow);
                    selectedRecord = tableModel.getRequestAt(modelRow);
                    updateDetailPanel();
                }
            }
        });

        // 搜索和过滤事件
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); autoResizeColumns(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); autoResizeColumns(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); autoResizeColumns(); }
        });

        methodFilter.addActionListener(e -> { applyFilters(); autoResizeColumns(); });
        statusFilter.addActionListener(e -> { applyFilters(); autoResizeColumns(); });

        // 双击事件 - 显示完整详情
        requestTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selectedRecord != null) {
                    showRequestDetailDialog();
                }
            }
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String methodText = (String) methodFilter.getSelectedItem();
        String statusText = (String) statusFilter.getSelectedItem();

        try {
            sorter.setRowFilter(new RowFilter<RequestTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends RequestTableModel, ? extends Integer> entry) {
                    RequestRecord record = tableModel.getRequestAt(entry.getIdentifier());

                    // 搜索过滤
                    if (!searchText.isEmpty()) {
                        String url = record.requestUrl.toLowerCase();
                        String method = record.requestMethod.toLowerCase();
                        String status = record.statusCode != null ? record.statusCode.toString() : "";

                        if (!url.contains(searchText) &&
                            !method.contains(searchText) &&
                            !status.contains(searchText)) {
                            return false;
                        }
                    }

                    // 方法过滤
                    if (!"All".equals(methodText) && (record.requestMethod == null || !methodText.equals(record.requestMethod))) {
                        return false;
                    }

                    // 状态过滤
                    if (!"All".equals(statusText)) {
                        Integer status = record.statusCode;
                        if (status == null && !"Error".equals(statusText)) {
                            return false;
                        } else if (status != null) {
                            if ("2xx".equals(statusText) && !(status >= 200 && status < 300)) return false;
                            if ("3xx".equals(statusText) && !(status >= 300 && status < 400)) return false;
                            if ("4xx".equals(statusText) && !(status >= 400 && status < 500)) return false;
                            if ("5xx".equals(statusText) && !(status >= 500 && status < 600)) return false;
                            if ("Error".equals(statusText) && status >= 200 && status < 400) return false;
                        }
                    }

                    return true;
                }
            });
        } catch (PatternSyntaxException e) {
            // 忽略正则表达式错误
        }

        updateStatusBar();
    }

    // 根据内容自动调整列宽（参考 LogPanel 实现）。
    private void autoResizeColumns() {
        if (requestTable.getColumnModel().getColumnCount() < 8) return;

        final int padding = 6;
        TableCellRenderer headerRenderer = requestTable.getTableHeader() != null
                ? requestTable.getTableHeader().getDefaultRenderer() : null;

        int viewRowCount = requestTable.getRowCount();
        int sample = Math.min(viewRowCount, 500);

        int colCount = requestTable.getColumnModel().getColumnCount();
        int[] widths = new int[colCount];

        for (int col = 0; col < colCount; col++) {
            TableColumn column = requestTable.getColumnModel().getColumn(col);
            int width = 0;
            // 表头
            Object headerValue = column.getHeaderValue();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(requestTable, headerValue, false, false, 0, col);
                width = Math.max(width, headerComp.getPreferredSize().width);
            } else if (headerValue != null) {
                width = Math.max(width, headerValue.toString().length() * requestTable.getFontMetrics(requestTable.getFont()).charWidth('字'));
            }
            // 内容（采样）
            if (sample > 0) {
                int step = Math.max(1, viewRowCount / sample);
                for (int r = 0, counted = 0; r < viewRowCount && counted < sample; r += step, counted++) {
                    TableCellRenderer cellRenderer = requestTable.getCellRenderer(r, col);
                    Component comp = requestTable.prepareRenderer(cellRenderer, r, col);
                    width = Math.max(width, comp.getPreferredSize().width);
                }
            }
            width += padding;
            widths[col] = width;
        }

        // URL 列（索引 2）至少占用视口宽度的 50%
        int urlCol = 2;
        JViewport vp = (JViewport) requestTable.getParent();
        int viewportWidth = vp != null ? vp.getWidth() : 800;
        int minUrlWidth = Math.max(200, viewportWidth / 2);
        if (urlCol < colCount) widths[urlCol] = Math.max(widths[urlCol], minUrlWidth);

        // 应用宽度：设置 preferredWidth，使列自适应但仍允许水平滚动
        for (int col = 0; col < colCount; col++) {
            TableColumn column = requestTable.getColumnModel().getColumn(col);
            column.setPreferredWidth(widths[col]);
            // 收紧过小列的最大宽度，避免无意义超宽（对 Status/Method/Protocol/Size/Time 等）
            if (col != urlCol) {
                int max = Math.max(widths[col], 60);
                column.setMinWidth(Math.min(max, 400));
            }
        }
        requestTable.revalidate();
        requestTable.repaint();
    }

    private void updateDetailPanel() {
        if (selectedRecord == null) {
            // 清空文本但不移除组件
            clearAllTabs();
            return;
        }

        // 更新各个标签页
        updateGeneralTab();
        updateHeadersTabs();
        updateCookiesTab();
        updateBodyTabs();
    }

    private void clearAllTabs() {
        if (requestHeadersArea != null) requestHeadersArea.setText("");
        if (responseHeadersArea != null) responseHeadersArea.setText("");
        if (cookiesArea != null) cookiesArea.setText("");
        if (requestPayloadArea != null) requestPayloadArea.setText("");
        if (responseBodyArea != null) responseBodyArea.setText("");
    }

    private void updateGeneralTab() {
        Component comp = detailTabs.getComponentAt(0);
        JPanel generalTab;
        if (comp instanceof JScrollPane) {
            JViewport vp = ((JScrollPane) comp).getViewport();
            Component view = vp != null ? vp.getView() : null;
            if (view instanceof JPanel) {
                generalTab = (JPanel) view;
            } else {
                return; // 无法找到面板，直接返回
            }
        } else if (comp instanceof JPanel) {
            generalTab = (JPanel) comp;
        } else {
            return;
        }

        generalTab.removeAll();

        if (selectedRecord != null) {
            generalTab.add(createInfoRow("Request URL:", selectedRecord.requestUrl));
            generalTab.add(createInfoRow("Request Method:", selectedRecord.requestMethod));
            generalTab.add(createInfoRow("Status Code:",
                selectedRecord.statusCode != null ? selectedRecord.statusCode + " " + selectedRecord.statusText : "Failed"));
            generalTab.add(createInfoRow("Remote Address:", selectedRecord.remoteAddress));
            generalTab.add(createInfoRow("Protocol:", selectedRecord.protocol));
            generalTab.add(createInfoRow("Referrer Policy:", selectedRecord.referrerPolicy));
            generalTab.add(createInfoRow("Duration:", selectedRecord.durationMs + " ms"));
            generalTab.add(createInfoRow("Request Size:", formatBytes(selectedRecord.requestSize)));
            generalTab.add(createInfoRow("Response Size:", formatBytes(selectedRecord.responseSize)));
            generalTab.add(createInfoRow("Task ID:", selectedRecord.taskId));
            if (selectedRecord.error != null) {
                generalTab.add(createInfoRow("Error:", selectedRecord.error));
            }
        }

        generalTab.revalidate();
        generalTab.repaint();
    }

    private void updateHeadersTabs() {
        if (requestHeadersArea != null) {
            if (selectedRecord != null && selectedRecord.requestHeaders != null) {
                StringBuilder sb = new StringBuilder();
                selectedRecord.requestHeaders.forEach((name, values) -> {
                    for (String value : values) {
                        sb.append(name).append(": ").append(value).append("\n");
                    }
                });
                requestHeadersArea.setText(sb.toString());
            } else {
                requestHeadersArea.setText("");
            }
        }

        if (responseHeadersArea != null) {
            if (selectedRecord != null && selectedRecord.responseHeaders != null) {
                StringBuilder sb = new StringBuilder();
                selectedRecord.responseHeaders.forEach((name, values) -> {
                    for (String value : values) {
                        sb.append(name).append(": ").append(value).append("\n");
                    }
                });
                responseHeadersArea.setText(sb.toString());
            } else {
                responseHeadersArea.setText("");
            }
        }
    }

    private void updateCookiesTab() {

        if (cookiesArea != null) {
            if (selectedRecord != null) {
                StringBuilder sb = new StringBuilder();

                // Display all cookies from the jar, grouped by domain and path
                if (selectedRecord.allCookies != null && !selectedRecord.allCookies.isEmpty()) {
                    sb.append("All Cookies in Jar (at time of request):\n");

                    // Group by domain
                    Map<String, List<CookieInfo>> cookiesByDomain = selectedRecord.allCookies.stream()
                        .collect(Collectors.groupingBy(c -> c.domain != null ? c.domain : "N/A"));

                    cookiesByDomain.forEach((domain, cookies) -> {
                        sb.append("Domain: ").append(domain).append("\n");

                        // Group by path within the domain
                        Map<String, List<CookieInfo>> cookiesByPath = cookies.stream()
                            .collect(Collectors.groupingBy(c -> c.path != null ? c.path : "/"));

                        cookiesByPath.forEach((path, pathCookies) -> {
                            sb.append("  Path: ").append(path).append("\n");
                            for (CookieInfo cookie : pathCookies) {
                                sb.append("    ").append(cookie.name).append("=").append(cookie.value).append("\n");
                            }
                        });
                    });
                    sb.append("\n");
                }


                if (selectedRecord.requestCookies != null && !selectedRecord.requestCookies.isEmpty()) {
                    sb.append("Request Cookies (Sent in 'Cookie' header):\n");
                    for (CookieInfo cookie : selectedRecord.requestCookies) {
                        sb.append("  ").append(cookie.name).append("=").append(cookie.value).append("\n");
                    }
                    sb.append("\n");
                }

                if (selectedRecord.responseCookies != null && !selectedRecord.responseCookies.isEmpty()) {
                    sb.append("Response Cookies (Set-Cookie):\n");
                    for (CookieInfo cookie : selectedRecord.responseCookies) {
                        sb.append("  ").append(cookie.name).append("=").append(cookie.value);
                        if (cookie.domain != null) sb.append("; Domain=").append(cookie.domain);
                        if (cookie.path != null) sb.append("; Path=").append(cookie.path);
                        if (cookie.secure) sb.append("; Secure");
                        if (cookie.httpOnly) sb.append("; HttpOnly");
                        sb.append("\n");
                    }
                }

                cookiesArea.setText(sb.toString());
            } else {
                cookiesArea.setText("");
            }
        }
    }

    private void updateBodyTabs() {
        // Request Payload
        if (requestPayloadArea != null) {
            if (selectedRecord != null && selectedRecord.requestPayload != null && !selectedRecord.requestPayload.isEmpty()) {
                requestPayloadArea.setText(prettyJson(selectedRecord.requestPayload));
                requestPayloadArea.setCaretPosition(0);
            } else {
                requestPayloadArea.setText("");
            }
        }
        // Response Body
        if (responseBodyArea != null) {
            if (selectedRecord != null && selectedRecord.responseBody != null && !selectedRecord.responseBody.isEmpty()) {
                responseBodyArea.setText(prettyJson(selectedRecord.responseBody));
                responseBodyArea.setCaretPosition(0);
            } else {
                responseBodyArea.setText("");
            }
        }
    }

    // JSON 美化：若能解析为 JSON，则格式化；否则返回原文
    private String prettyJson(String raw) {
        try {
            JsonElement el = JsonParser.parseString(raw);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(el);
        } catch (Exception ignore) {
            return raw;
        }
    }

    private JPanel createInfoRow(String label, String value) {
        // 使用 BorderLayout，左侧固定标签，右侧值区域放入可水平滚动的文本域，避免因超长内容撑大整个窗口
        JPanel row = new JPanel(new BorderLayout(8, 2));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(lblLabel.getFont().deriveFont(Font.BOLD));
        // 固定标签区域宽度与高度
        lblLabel.setPreferredSize(new Dimension(120, GENERAL_ROW_HEIGHT));
        lblLabel.setMinimumSize(new Dimension(120, GENERAL_ROW_HEIGHT));
        lblLabel.setMaximumSize(new Dimension(120, GENERAL_ROW_HEIGHT));
        row.add(lblLabel, BorderLayout.WEST);

        JTextArea valueArea = new JTextArea(value != null ? value : "");
        valueArea.setEditable(false);
        valueArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        // 单行显示；过长时通过水平滚动条查看
        valueArea.setRows(1);
        valueArea.setLineWrap(false);
        valueArea.setWrapStyleWord(false);

        JScrollPane sp = new JScrollPane(valueArea);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        // 关键：固定值区域容器高度，宽度由父容器决定
        sp.setPreferredSize(new Dimension(0, GENERAL_ROW_HEIGHT));
        sp.setMinimumSize(new Dimension(0, GENERAL_ROW_HEIGHT));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, GENERAL_ROW_HEIGHT));
        row.add(sp, BorderLayout.CENTER);

        // 阻止 BoxLayout 在纵向拉伸此行：最大高度=固定行高
        Dimension rowPref = row.getPreferredSize();
        if (rowPref == null) rowPref = new Dimension(0, GENERAL_ROW_HEIGHT);
        row.setPreferredSize(new Dimension(rowPref.width, GENERAL_ROW_HEIGHT));
        row.setMinimumSize(new Dimension(0, GENERAL_ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, GENERAL_ROW_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        return row;
    }

    private void updateStatusBar() {
        int totalRequests = tableModel.getRowCount();
        int visibleRequests = requestTable.getRowCount();

        statusLabel.setText("Ready");
        countLabel.setText(visibleRequests + "/" + totalRequests + " requests");
    }

    private void loadExistingRecords() {
        List<RequestRecord> existingRecords = RequestManager.getInstance().getRecords();
        for (RequestRecord record : existingRecords) {
            tableModel.addRequest(record);
        }
        updateStatusBar();
        autoResizeColumns();
    }

    private void exportRequests() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("network_requests.jsonl"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                RequestManager.getInstance().saveToFile(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Requests exported successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting requests: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRequestDetailDialog() {
        if (selectedRecord == null) return;

        // 使用 Window 作为 owner，避免在 JDialog 环境下的 Frame 强转异常
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Request Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        // 构建只读详情标签页
        JTabbedPane tabs = new JTabbedPane();

        // General
        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
        generalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        generalPanel.add(createInfoRow("Request URL:", selectedRecord.requestUrl));
        generalPanel.add(createInfoRow("Request Method:", selectedRecord.requestMethod));
        generalPanel.add(createInfoRow("Status Code:",
            selectedRecord.statusCode != null ? selectedRecord.statusCode + " " + selectedRecord.statusText : "Failed"));
        generalPanel.add(createInfoRow("Remote Address:", selectedRecord.remoteAddress));
        generalPanel.add(createInfoRow("Protocol:", selectedRecord.protocol));
        generalPanel.add(createInfoRow("Referrer Policy:", selectedRecord.referrerPolicy));
        generalPanel.add(createInfoRow("Duration:", selectedRecord.durationMs + " ms"));
        generalPanel.add(createInfoRow("Request Size:", formatBytes(selectedRecord.requestSize)));
        generalPanel.add(createInfoRow("Response Size:", formatBytes(selectedRecord.responseSize)));
        generalPanel.add(createInfoRow("Task ID:", selectedRecord.taskId));
        if (selectedRecord.error != null) {
            generalPanel.add(createInfoRow("Error:", selectedRecord.error));
        }
        JScrollPane generalSP = new JScrollPane(generalPanel);
        generalSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        generalSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabs.addTab("General", generalSP);

        // Request Headers
        JTextArea reqHeaders = new JTextArea();
        reqHeaders.setEditable(false);
        reqHeaders.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        if (selectedRecord.requestHeaders != null) {
            StringBuilder sb = new StringBuilder();
            selectedRecord.requestHeaders.forEach((name, values) -> {
                for (String value : values) sb.append(name).append(": ").append(value).append("\n");
            });
            reqHeaders.setText(sb.toString());
            reqHeaders.setCaretPosition(0);
        }
        JScrollPane reqHeadersSP = new JScrollPane(reqHeaders);
        reqHeadersSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        reqHeadersSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabs.addTab("Request Headers", reqHeadersSP);

        // Response Headers
        JTextArea respHeaders = new JTextArea();
        respHeaders.setEditable(false);
        respHeaders.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        if (selectedRecord.responseHeaders != null) {
            StringBuilder sb = new StringBuilder();
            selectedRecord.responseHeaders.forEach((name, values) -> {
                for (String value : values) sb.append(name).append(": ").append(value).append("\n");
            });
            respHeaders.setText(sb.toString());
            respHeaders.setCaretPosition(0);
        }
        JScrollPane respHeadersSP = new JScrollPane(respHeaders);
        respHeadersSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        respHeadersSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabs.addTab("Response Headers", respHeadersSP);

        // Cookies
        JTextArea cookiesAreaLocal = new JTextArea();
        cookiesAreaLocal.setEditable(false);
        cookiesAreaLocal.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        {
            StringBuilder sb = new StringBuilder();

            // Display all cookies from the jar, grouped by domain and path
            if (selectedRecord.allCookies != null && !selectedRecord.allCookies.isEmpty()) {
                sb.append("All Cookies in Jar (at time of request):\n");

                // Group by domain
                Map<String, List<CookieInfo>> cookiesByDomain = selectedRecord.allCookies.stream()
                    .collect(Collectors.groupingBy(c -> c.domain != null ? c.domain : "N/A"));

                cookiesByDomain.forEach((domain, cookies) -> {
                    sb.append("Domain: ").append(domain).append("\n");

                    // Group by path within the domain
                    Map<String, List<CookieInfo>> cookiesByPath = cookies.stream()
                        .collect(Collectors.groupingBy(c -> c.path != null ? c.path : "/"));

                    cookiesByPath.forEach((path, pathCookies) -> {
                        sb.append("  Path: ").append(path).append("\n");
                        for (CookieInfo cookie : pathCookies) {
                            sb.append("    ").append(cookie.name).append("=").append(cookie.value).append("\n");
                        }
                    });
                });
                sb.append("\n");
            }

            if (selectedRecord.requestCookies != null && !selectedRecord.requestCookies.isEmpty()) {
                sb.append("Request Cookies (Sent in 'Cookie' header):\n");
                for (CookieInfo cookie : selectedRecord.requestCookies) {
                    sb.append("  ").append(cookie.name).append("=").append(cookie.value).append("\n");
                }
                sb.append("\n");
            }
            if (selectedRecord.responseCookies != null && !selectedRecord.responseCookies.isEmpty()) {
                sb.append("Response Cookies (Set-Cookie):\n");
                for (CookieInfo cookie : selectedRecord.responseCookies) {
                    sb.append("  ").append(cookie.name).append("=").append(cookie.value);
                    if (cookie.domain != null) sb.append("; Domain=").append(cookie.domain);
                    if (cookie.path != null) sb.append("; Path=").append(cookie.path);
                    if (cookie.secure) sb.append("; Secure");
                    if (cookie.httpOnly) sb.append("; HttpOnly");
                    sb.append("\n");
                }
            }

            cookiesAreaLocal.setText(sb.toString());
            cookiesAreaLocal.setCaretPosition(0);
        }
        JScrollPane cookiesSP = new JScrollPane(cookiesAreaLocal);
        cookiesSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cookiesSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabs.addTab("Cookies", cookiesSP);

        // Request Payload
        RSyntaxTextArea reqBody = new RSyntaxTextArea();
        reqBody.setEditable(false);
        reqBody.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        reqBody.setCodeFoldingEnabled(true);
        reqBody.setAntiAliasingEnabled(true);
        reqBody.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reqBody.setLineWrap(false);
        reqBody.setWrapStyleWord(false);
        if (selectedRecord.requestPayload != null && !selectedRecord.requestPayload.isEmpty()) {
            reqBody.setText(prettyJson(selectedRecord.requestPayload));
            reqBody.setCaretPosition(0);
        }
        WidthCappedScrollPane reqBodySP = new WidthCappedScrollPane(reqBody);
        reqBodySP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        reqBodySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabs.addTab("Request Payload", reqBodySP);

        // Response Body
        RSyntaxTextArea respBody = new RSyntaxTextArea();
        respBody.setEditable(false);
        respBody.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        respBody.setCodeFoldingEnabled(true);
        respBody.setAntiAliasingEnabled(true);
        respBody.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        respBody.setLineWrap(false);
        respBody.setWrapStyleWord(false);
        if (selectedRecord.responseBody != null && !selectedRecord.responseBody.isEmpty()) {
            respBody.setText(prettyJson(selectedRecord.responseBody));
            respBody.setCaretPosition(0);
        }
        WidthCappedScrollPane respBodySP = new WidthCappedScrollPane(respBody);
        respBodySP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        respBodySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabs.addTab("Response Body", respBodySP);

        dialog.add(tabs);
        dialog.setVisible(true);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // =============== RequestManager.RequestListener 实现 ===============

    @Override
    public void onRequestAdded(RequestRecord record) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRequest(record);
            updateStatusBar();

            // 自动滚动到最新请求
            int lastRow = requestTable.getRowCount() - 1;
            if (lastRow >= 0) {
                requestTable.scrollRectToVisible(requestTable.getCellRect(lastRow, 0, true));
            }
            autoResizeColumns();
        });
    }

    @Override
    public void onRecordsCleared() {
        SwingUtilities.invokeLater(() -> {
            tableModel.clear();
            selectedRecord = null;
            updateDetailPanel();
            updateStatusBar();
            autoResizeColumns();
        });
    }

    // =============== 内部类：表格模型 ===============

    private static class RequestTableModel extends AbstractTableModel {
        private final List<RequestRecord> requests = new ArrayList<>();
        private final String[] columnNames = {
            "Status", "Method", "URL", "Protocol", "Size", "Time", "Task ID", "Remote Address"
        };

        @Override
        public int getRowCount() {
            return requests.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RequestRecord record = requests.get(rowIndex);

            switch (columnIndex) {
                case 0: return record.statusCode != null ? record.statusCode.toString() : "Failed";
                case 1: return record.requestMethod;
                case 2: return record.requestUrl;
                case 3: return record.protocol;
                case 4: return formatBytes(record.responseSize);
                case 5: return record.durationMs + "ms";
                case 6: return record.taskId;
                case 7: return record.remoteAddress;
                default: return "";
            }
        }

        public void addRequest(RequestRecord record) {
            requests.add(record);
            fireTableRowsInserted(requests.size() - 1, requests.size() - 1);
        }

        public void clear() {
            int size = requests.size();
            requests.clear();
            if (size > 0) {
                fireTableRowsDeleted(0, size - 1);
            }
        }

        public RequestRecord getRequestAt(int index) {
            return requests.get(index);
        }
    }

    // =============== 内部类：表格单元格渲染器 ===============

    private static class RequestTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // 根据状态码设置颜色
                if (column == 0 && value != null) {
                    String status = value.toString();
                    if (status.equals("Failed")) {
                        setForeground(Color.RED);
                    } else {
                        try {
                            int statusCode = Integer.parseInt(status);
                            if (statusCode >= 200 && statusCode < 300) {
                                setForeground(new Color(0, 150, 0)); // 绿色
                            } else if (statusCode >= 300 && statusCode < 400) {
                                setForeground(new Color(255, 140, 0)); // 橙色
                            } else if (statusCode >= 400) {
                                setForeground(Color.RED);
                            }
                        } catch (NumberFormatException e) {
                            setForeground(Color.BLACK);
                        }
                    }
                } else {
                    setForeground(Color.BLACK);
                }

                // 斑马线效果
                if (row % 2 == 0) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(new Color(248, 248, 248));
                }
            }

            return this;
        }
    }
}
