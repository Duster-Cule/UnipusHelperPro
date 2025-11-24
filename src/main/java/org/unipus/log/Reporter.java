package org.unipus.log;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.unipus.unipus.Task;
import org.unipus.unipus.TaskManager;
import org.unipus.util.FileUtils;
import org.unipus.util.StringProcesser;
import org.unipus.web.RequestManager;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

/**
 * 此类用于生成报告。
 */

public class Reporter {
    private String filePath;
    private List<String> logHistory;
    private List<String> networkHistory;
    private static final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(Reporter.class);

    public static Reporter autoGenerateReports() {
        return autoGenerateReports(null);
    }

    public static Reporter autoGenerateReports(String filePath) {

        logger.info("Generating report...");

        Reporter reporter = new Reporter();
        reporter.filePath = Objects.requireNonNullElseGet(filePath, () -> "logs/report_" + System.currentTimeMillis() + ".json");

        //保证隐私，将手机号等敏感信息从日志中移除
        TaskManager taskManager = TaskManager.getInstance();
        List<String> phoneNums = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Task task : taskManager.getAllTasks()) {
            String[] tmp = task.getTaskId().split("-");
            phoneNums.add(tmp.length >= 2 ? tmp[1] : "");
            names.add(task.getName() != null ? task.getName() : "");
        }

        String phoneNum = phoneNums.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        String name = names.stream()
                .filter(s ->  s!= null && !s.isEmpty())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        // 从BroadcastAppender获取历史记录
        BroadcastAppender appender = BroadcastAppender.getInstance();
        if (appender != null) {
            List<LogEvent> logs = appender.getAllLogs();
            reporter.logHistory = new ArrayList<>();

            for (LogEvent log : logs) {
                String str = StringProcesser.safeReplaceAll(log.getMessage().getFormattedMessage(), phoneNum, "***********");
                str = StringProcesser.safeReplaceAll(str, name, "***");
                reporter.logHistory.add(str);
            }
        } else {
            reporter.logHistory = new ArrayList<>();
        }

        // 从RequestManager获取网络请求日志
        reporter.networkHistory = new ArrayList<>();
        List<RequestManager.RequestRecord> records = RequestManager.getInstance().getRecords();

        for (RequestManager.RequestRecord record : records) {
            // 将每个 RequestRecord 转换为完整的 JSON 对象
            // 过滤掉包含敏感信息的请求
            if (record.requestUrl.equals("https://sso.unipus.cn/sso/0.1/sso/login")) continue;
            if (record.requestUrl.equals("https://uai.unipus.cn/api/account/user/info")) continue;
            if (record.requestUrl.contains("uai.unipus.cn/api/tla/learningDetail/studyRecord/totalAndUnitSituation")) continue;
            String jsonRecord = gson.toJson(record);
            jsonRecord = StringProcesser.safeReplaceAll(jsonRecord, phoneNum, "***********");
            jsonRecord = StringProcesser.safeReplaceAll(jsonRecord, name, "***");
            reporter.networkHistory.add(jsonRecord);
        }

        return reporter;
    }

    //输出文件路径请使用 setFilePath(String filePath) 更改。
    public void saveToFile() throws IOException {

        logger.info("Saving report...");

        long timeMillis = System.currentTimeMillis();
        FileWriter fileWriter = null;
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(filePath))){
            StringBuilder logs = new StringBuilder();
            for (String log : logHistory) {
                logs.append(log);
                logs.append("\n");
            }
            FileUtils.addStringAsZipEntry(zipOut, "logs.log", logs.toString());

            StringBuilder networks = new StringBuilder();
            for (String networkLog : networkHistory) {
                networks.append(networkLog);
                networks.append("\n");
            }
            FileUtils.addStringAsZipEntry(zipOut, "networks.jsonl", networks.toString());
        }
    }

    public static void generateAndSaveReports() throws IOException {
        autoGenerateReports().saveToFile();
    }

    public static void generateAndSaveReports(String filePath) throws IOException {
        autoGenerateReports(filePath).saveToFile();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
