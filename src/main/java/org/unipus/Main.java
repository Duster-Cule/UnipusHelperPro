package org.unipus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.unipus.ui.MainGUI;
import org.unipus.log.LogCapture;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        setup();
        start();
    }

    private static void setup() {
        // 0. 先强制加载/应用 log4j2 配置，避免稍后覆盖自定义 Appender
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();
        } catch (Throwable ignored) {}
        LogCapture.install();
    }

    public static void start() {
        MainGUI.getInstance();
    }
}