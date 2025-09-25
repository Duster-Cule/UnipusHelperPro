package org.unipus.ui;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.unipus.log.LogCapture;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * 日志面板：展示 log4j2 输出，支持过滤、导出、自动滚动、统计计数。
 * 该面板通过程序化注册的 Appender 订阅日志事件，不需要在 log4j2.xml 中额外配置。
 */
public class LogPanel extends JPanel {

    // 提供一个公共方法，允许在程序启动时安装日志捕获（并记录历史）
    public static void ensureGlobalLogCapture() {
        LogCapture.install();
    }

    // 列定义
    private static final String[] COLS = {"时间", "级别", "线程", "记录器", "消息"};

    // UI 组件
    private final JTable table;
    private final LogTableModel tableModel;
    private final TableRowSorter<LogTableModel> sorter;
    private final JTextField searchField;
    private final JComboBox<String> lineLimitBox;
    private final JCheckBox autoScrollBox;
    private final JCheckBox autoResizeBox;
    // 级别统计/选择 chips
    private final JToggleButton fatalChip;
    private final JToggleButton errorChip;
    private final JToggleButton warnChip;
    private final JToggleButton infoChip;
    private final JToggleButton debugChip;
    private final JToggleButton traceChip;

    // 计数
    private int fatalCount = 0;
    private int errorCount = 0;
    private int warnCount = 0;
    private int infoCount = 0;
    private int debugCount = 0;
    private int traceCount = 0;

    // 过滤状态：可多选
    private final Set<Level> selectedLevels = new HashSet<>();

    // 行数限制，-1 表示不限制
    private int maxEntries = 1000;

    // 时间格式
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss.SSS");

    // 订阅句柄，便于销毁
    private final LogCapture.LogListener listener = this::onLogEvent;

    public LogPanel() {
        this(true);
    }

    public LogPanel(boolean listenForRealtime) {
        setLayout(new BorderLayout());

        // 初始化表格
        tableModel = new LogTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new LogCellRenderer(tableModel));

        sorter = new TableRowSorter<>(tableModel);
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }
        table.setRowSorter(sorter);

        // 顶部工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230,230,230)));

        JButton exportBtn = new JButton("导出");
        exportBtn.setToolTipText("导出当前可见日志到文件");
        exportBtn.addActionListener(e -> exportLogs());

        JButton clearBtn = new JButton("清除");
        clearBtn.setToolTipText("清除日志列表与计数");
        clearBtn.addActionListener(e -> clearLogs());

        searchField = new JTextField(18);
        searchField.setToolTipText("关键字过滤，匹配消息 / 记录器 / 线程");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFiltersAndMaybeResize(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFiltersAndMaybeResize(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFiltersAndMaybeResize(); }
        });

        lineLimitBox = new JComboBox<>(new String[]{"200", "500", "1000", "5000", "无限制"});
        lineLimitBox.setSelectedItem("1000");
        lineLimitBox.setToolTipText("显示行数上限（更高开销）");
        lineLimitBox.addActionListener(e -> { updateLineLimit(); applyFiltersAndMaybeResize(); });

        autoScrollBox = new JCheckBox("自动滚动", true);
        autoResizeBox = new JCheckBox("自动调整列宽", true);
        autoResizeBox.addActionListener(e -> { if (autoResizeBox.isSelected()) autoResizeColumns(); });

        toolbar.add(new JLabel("显示行数"));
        toolbar.add(lineLimitBox);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(new JLabel("过滤"));
        toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(autoScrollBox);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(autoResizeBox);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(exportBtn);
        toolbar.add(clearBtn);

        // 统计条（可点击多选过滤某级别）
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        statsBar.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        fatalChip = makeLevelChip("fatals", new Color(190, 0, 0), Level.FATAL);
        errorChip = makeLevelChip("errors", new Color(220, 53, 69), Level.ERROR);
        warnChip  = makeLevelChip("warns", new Color(255, 140, 0), Level.WARN);
        infoChip  = makeLevelChip("infos", new Color(100, 100, 100), Level.INFO);
        debugChip = makeLevelChip("debugs", new Color(0, 102, 204), Level.DEBUG);
        traceChip = makeLevelChip("traces", new Color(180, 180, 180), Level.TRACE);
        statsBar.add(fatalChip);
        statsBar.add(errorChip);
        statsBar.add(warnChip);
        statsBar.add(infoChip);
        statsBar.add(debugChip);
        statsBar.add(traceChip);

        // 主体
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statsBar, BorderLayout.SOUTH);

        // 确保捕获器已安装，并加载历史
        LogCapture.install();
        java.util.List<LogEvent> history = LogCapture.snapshotHistory();
        for (LogEvent ev : history) {
            // 这里 onLogEvent 是在 UI 线程上调用的，但对于初始加载，直接在当前线程处理更快
            addLogRow(createLogRow(ev));
        }

        // 默认勾选（不包含 TRACE）
        selectedLevels.clear();
        selectedLevels.addAll(Arrays.asList(Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG));
        fatalChip.setSelected(true);  updateChipStyle(fatalChip, new Color(190, 0, 0));
        errorChip.setSelected(true);  updateChipStyle(errorChip, new Color(220, 53, 69));
        warnChip.setSelected(true);   updateChipStyle(warnChip, new Color(255, 140, 0));
        infoChip.setSelected(true);   updateChipStyle(infoChip, new Color(100, 100, 100));
        debugChip.setSelected(true);  updateChipStyle(debugChip, new Color(0, 102, 204));
        traceChip.setSelected(false); updateChipStyle(traceChip, new Color(180, 180, 180));

        updateStatsLabels();
        applyFiltersAndMaybeResize();
        if (autoResizeBox.isSelected()) SwingUtilities.invokeLater(this::autoResizeColumns);

        // 双击某行弹出详情
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = table.convertRowIndexToModel(row);
                        LogRow r = tableModel.getRow(modelRow);
                        if (r != null) showDetailDialog(r);
                    }
                }
            }
        });
    }

    /**
     * 停止监听日志，清理资源
     */
    public void cleanup() {
        LogCapture.removeListener(listener);
    }

    private JToggleButton makeLevelChip(String label, Color baseColor, Level level) {
        JToggleButton b = new JToggleButton("0 " + label);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                new EmptyBorder(4,8,4,8)
        ));
        b.setOpaque(true);
        b.setBackground(new Color(245,245,245));
        b.setForeground(baseColor);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText("点击以(反)选中该级别进行过滤。选中多个=多级别过滤；全部不选=不过滤");
        b.addActionListener(e -> {
            if (b.isSelected()) selectedLevels.add(level); else selectedLevels.remove(level);
            updateChipStyle(b, baseColor);
            applyFiltersAndMaybeResize();
        });
        return b;
    }

    private void updateChipStyle(JToggleButton b, Color baseColor) {
        if (b.isSelected()) {
            b.setBackground(baseColor.darker());
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(baseColor.darker()), new EmptyBorder(4,8,4,8)));
        } else {
            b.setBackground(new Color(245,245,245));
            b.setForeground(baseColor);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220,220,220)), new EmptyBorder(4,8,4,8)));
        }
    }

    private void updateLineLimit() {
        String sel = (String) lineLimitBox.getSelectedItem();
        if (sel == null) return;
        if ("无限制".equals(sel)) {
            maxEntries = -1;
        } else {
            try { maxEntries = Integer.parseInt(sel); } catch (Exception ignored) {}
        }
        tableModel.setMaxEntries(maxEntries);
    }

    private void applyFiltersAndMaybeResize() {
        applyFilters();
        if (autoResizeBox.isSelected()) autoResizeColumns();
    }

    private void applyFilters() {
        String text = searchField.getText();
        Set<Level> levels = new HashSet<>(selectedLevels);
        try {
            sorter.setRowFilter(new RowFilter<>() {
                @Override public boolean include(Entry<? extends LogTableModel, ? extends Integer> entry) {
                    LogRow r = tableModel.getRow(entry.getIdentifier());
                    if (r == null) return false;
                    // 级别过滤：若无选中 -> 不显示任何内容
                    if (levels.isEmpty()) return false;
                    if (r.level == null || !levels.contains(r.level)) return false;
                    // 关键字过滤
                    if (text != null && !text.isEmpty()) {
                        String q = text.toLowerCase();
                        return (r.message != null && r.message.toLowerCase().contains(q))
                                || (r.logger != null && r.logger.toLowerCase().contains(q))
                                || (r.thread != null && r.thread.toLowerCase().contains(q));
                    }
                    return true;
                }
            });
        } catch (PatternSyntaxException ignored) {}
    }

    private LogRow createLogRow(LogEvent event) {
        if (event == null) return null;
        return new LogRow(
                TIME_FMT.format(new Date(event.getTimeMillis())),
                event.getLevel(),
                event.getThreadName(),
                event.getLoggerName(),
                buildMessage(event)
        );
    }

    private void addLogRow(LogRow row) {
        if (row == null) return;
        tableModel.addRow(row);
        // 更新计数
        if (row.level == Level.FATAL) fatalCount++;
        else if (row.level == Level.ERROR) errorCount++;
        else if (row.level == Level.WARN) warnCount++;
        else if (row.level == Level.INFO) infoCount++;
        else if (row.level == Level.DEBUG) debugCount++;
        else if (row.level == Level.TRACE) traceCount++;
        updateStatsLabels();
    }

    private void onLogEvent(LogEvent event) {
        if (event == null) return;
        SwingUtilities.invokeLater(() -> {
            addLogRow(createLogRow(event));

            if (autoScrollBox.isSelected()) scrollToBottom();
            if (autoResizeBox.isSelected()) autoResizeColumns();
        });
    }

    private static String buildMessage(LogEvent event) {
        String msg = event.getMessage() != null ? event.getMessage().getFormattedMessage() : "";
        if (event.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            event.getThrown().printStackTrace(pw);
            pw.flush();
            msg += "\n" + sw;
        }
        return msg;
    }

    private void updateStatsLabels() {
        fatalChip.setText(fatalCount + " fatals");
        errorChip.setText(errorCount + " errors");
        warnChip.setText(warnCount + " warns");
        infoChip.setText(infoCount + " infos");
        debugChip.setText(debugCount + " debugs");
        traceChip.setText(traceCount + " traces");
        // 同步样式，避免外观丢失
        updateChipStyle(fatalChip, new Color(190,0,0));
        updateChipStyle(errorChip, new Color(220,53,69));
        updateChipStyle(warnChip, new Color(255,140,0));
        updateChipStyle(infoChip, new Color(100,100,100));
        updateChipStyle(debugChip, new Color(0,102,204));
        updateChipStyle(traceChip, new Color(180, 180, 180));
    }

    private void scrollToBottom() {
        int last = table.getRowCount() - 1;
        if (last >= 0) table.scrollRectToVisible(table.getCellRect(last, 0, true));
    }

    private void exportLogs() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("logs-export.txt"));
        int r = fc.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try (FileWriter fw = new FileWriter(f)) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                LogRow row = tableModel.getRow(i);
                if (row == null) continue;
                fw.write(String.format("%s [%s] %-5s %s - %s%n",
                        row.time, row.thread, row.level, row.logger, row.message.replace('\n', ' ')));
            }
            fw.flush();
            JOptionPane.showMessageDialog(this, "已导出到: " + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearLogs() {
        tableModel.clear();
        fatalCount = errorCount = warnCount = infoCount = debugCount = traceCount = 0;
        updateStatsLabels();
        if (autoResizeBox.isSelected()) autoResizeColumns();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        LogCapture.addListener(listener);
    }
    @Override
    public void removeNotify() {
        LogCapture.removeListener(listener);
        super.removeNotify();
    }

    // 根据内容自动调整列宽（不含“消息”列），并保证“消息”列至少占视口宽度的50%
    private void autoResizeColumns() {
        if (table.getColumnModel().getColumnCount() < 5) return;

        // 更紧凑的额外留白，避免前几列显得过宽
        final int padding = 4;

        // 先计算 0..3 列的“刚好放下内容”的宽度（包含表头和可见行），尽量轻量
        int[] widths = new int[4];
        TableCellRenderer headerRenderer = table.getTableHeader() != null
                ? table.getTableHeader().getDefaultRenderer() : null;
        int rowCount = table.getRowCount();
        // 为避免超大数据集时的开销，最多扫描前 N 行与后 N 行各一部分（采样）
        int sample = Math.min(rowCount, 500);

        for (int col = 0; col < 4; col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            int width = 0;
            // 表头
            Object headerValue = column.getHeaderValue();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, col);
                width = Math.max(width, headerComp.getPreferredSize().width);
            } else if (headerValue != null) {
                width = Math.max(width, headerValue.toString().length() * table.getFontMetrics(table.getFont()).charWidth('字'));
            }
            // 内容（采样）
            if (sample > 0) {
                // 均匀采样前半部分
                int step = Math.max(1, rowCount / sample);
                for (int r = 0, counted = 0; r < rowCount && counted < sample; r += step, counted++) {
                    TableCellRenderer cellRenderer = table.getCellRenderer(r, col);
                    Component comp = table.prepareRenderer(cellRenderer, r, col);
                    width = Math.max(width, comp.getPreferredSize().width);
                }
            }
            width += padding;
            // 写回：收紧非消息列
            column.setMinWidth(width);
            column.setPreferredWidth(width);
            column.setMaxWidth(width + 16);
            widths[col] = width;
        }

        // 计算视口宽度
        int viewportWidth = 0;
        Container parent = table.getParent();
        if (parent instanceof JViewport) {
            Dimension ext = ((JViewport) parent).getExtentSize();
            viewportWidth = ext != null ? ext.width : parent.getWidth();
        }
        if (viewportWidth <= 0) viewportWidth = Math.max(table.getVisibleRect().width, table.getWidth());

        int sumOthers = widths[0] + widths[1] + widths[2] + widths[3];
        TableColumn msgCol = table.getColumnModel().getColumn(4);
        int minMsg = viewportWidth > 0 ? (int) Math.round(viewportWidth * 0.5) : 300;
        boolean needScroll = viewportWidth > 0 && (sumOthers + minMsg > viewportWidth);

        if (needScroll) {
            // 关闭自动拉伸，强制总宽度 = 前四列 + 至少50%视口的消息列，出现水平滚动条
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            msgCol.setMinWidth(minMsg);
            msgCol.setPreferredWidth(minMsg);
            msgCol.setMaxWidth(Integer.MAX_VALUE);
        } else {
            // 保持自动分配剩余空间给消息列
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            int targetMsg = Math.max(minMsg, viewportWidth - sumOthers);
            msgCol.setMinWidth(80);
            msgCol.setPreferredWidth(targetMsg);
            msgCol.setMaxWidth(Integer.MAX_VALUE);
        }

        table.doLayout();
        table.revalidate();
        table.repaint();
    }

    // ===================== 表格模型与渲染 =====================

    private static class LogRow {
        final String time;
        final Level level;
        final String thread;
        final String logger;
        final String message;
        private LogRow(String time, Level level, String thread, String logger, String message) {
            this.time = time; this.level = level; this.thread = thread; this.logger = logger; this.message = message;
        }
    }

    private static class LogTableModel extends AbstractTableModel {
        private final java.util.List<LogRow> rows = new ArrayList<>();
        private int maxEntries = 1000; // -1 unlimited

        public void setMaxEntries(int max) {
            this.maxEntries = max;
            // 修剪
            if (maxEntries > 0 && rows.size() > maxEntries) {
                int remove = rows.size() - maxEntries;
                for (int i = 0; i < remove; i++) rows.remove(0);
                fireTableDataChanged();
            }
        }
        public void addRow(LogRow row) {
            rows.add(row);
            if (maxEntries > 0 && rows.size() > maxEntries) {
                int remove = rows.size() - maxEntries;
                for (int i = 0; i < remove; i++) rows.remove(0);
                fireTableDataChanged();
            } else {
                int idx = rows.size() - 1;
                fireTableRowsInserted(idx, idx);
            }
        }

        public LogRow getRow(int index) { return index >= 0 && index < rows.size() ? rows.get(index) : null; }

        public void clear() {
            rows.clear();
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int column) { return COLS[column]; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            LogRow r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.time;
                case 1 -> r.level;
                case 2 -> r.thread;
                case 3 -> r.logger;
                case 4 -> r.message;
                default -> "";
            };
        }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 1 ? Level.class : String.class;
        }
    }

    private static class LogCellRenderer extends DefaultTableCellRenderer {
        private final LogTableModel model;
        // 更淡的配色：fatal-红黑(偏红且微灰)、error-红、warn-黄、info-白、debug-淡蓝、trace-极浅灰
        private final Color fatal = new Color(241, 181, 181);
        private final Color error = new Color(255, 216, 216);
        private final Color warn  = new Color(255, 251, 220);
        private final Color info  = Color.WHITE;
        private final Color debug = new Color(222, 237, 250);
        private final Color trace = new Color(240, 240, 240);
        private final Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        public LogCellRenderer(LogTableModel model) { this.model = model; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            LogRow r = model.getRow(modelRow);
            if (!isSelected) {
                Color bg;
                if (r == null || r.level == null) {
                    bg = trace;
                } else {
                    bg = switch (r.level.name()) {
                        case "FATAL" -> fatal;
                        case "ERROR" -> error;
                        case "WARN"  -> warn;
                        case "INFO"  -> info;
                        case "DEBUG" -> debug;
                        case "TRACE" -> trace;
                        default -> trace;
                    };
                }
                c.setBackground(bg);
            }
            if (column == 4) setFont(mono);
            else setFont(table.getFont());
            return c;
        }
    }

    private void showDetailDialog(LogRow row) {
        JTextArea textArea = new JTextArea(20, 80);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setText(row.message);
        JScrollPane sp = new JScrollPane(textArea);
        sp.setPreferredSize(new Dimension(800, 400));
        JOptionPane.showMessageDialog(this, sp,
                String.format("%s [%s] %s", row.level, row.thread, row.logger),
                JOptionPane.PLAIN_MESSAGE);
    }
}
