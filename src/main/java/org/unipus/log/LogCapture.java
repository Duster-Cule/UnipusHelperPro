package org.unipus.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class LogCapture {

    private static final Logger log = LogManager.getLogger(LogCapture.class);

    public interface LogListener { void onEvent(LogEvent event); }

    private static final List<LogListener> listeners = new CopyOnWriteArrayList<>();
    private static final Deque<LogEvent> history = new ArrayDeque<>();
    private static final int HISTORY_CAPACITY = 5000;
    private static volatile boolean installed = false;
    private static final String APPENDER_NAME = "SwingBroadcastAppender";
    private static volatile boolean contextListenerRegistered = false;
    private static volatile boolean monitorStarted = false;
    private static ScheduledExecutorService monitor;

    private LogCapture() {}

    private static final PropertyChangeListener CONTEXT_CHANGE_LISTENER = new PropertyChangeListener() {
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if ("config".equals(evt.getPropertyName())) {
                installed = false; // 复位标志
                install();
            }
        }
    };

    /**
     * 安装或确保已安装捕获 Appender，并启动健康监控以应对后续配置重载。
     */
    public static void install() {
        synchronized (LogCapture.class) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration cfg = ctx.getConfiguration();

            if (!contextListenerRegistered) {
                try {
                    ctx.addPropertyChangeListener(CONTEXT_CHANGE_LISTENER);
                    contextListenerRegistered = true;
                } catch (Throwable e) {
                    log.debug("LoggerContext does not support property listeners or registration failed: {}", e.toString());
                }
            }

            if (installed && cfg.getAppenders().containsKey(APPENDER_NAME)) {
                startMonitorIfNeeded();
                return;
            }
            if (installed && !cfg.getAppenders().containsKey(APPENDER_NAME)) {
                installed = false;
            }
            if (!installed) {
                try {
                    if (!cfg.getAppenders().containsKey(APPENDER_NAME)) {
                        Appender appender = new BroadcastAppender(APPENDER_NAME, null, null);
                        appender.start();
                        cfg.addAppender(appender);
                        LoggerConfig root = cfg.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                        root.addAppender(appender, null, null);
                        ctx.updateLoggers();
                    }
                    installed = true;
                } catch (Throwable e) {
                    log.warn("Logger installation failed", e);
                }
            }
            startMonitorIfNeeded();
        }
    }

    private static void startMonitorIfNeeded() {
        if (monitorStarted) return;
        monitorStarted = true;
        monitor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "LogCaptureMonitor");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            }
        });
        monitor.scheduleAtFixedRate(() -> {
            try {
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                Configuration cfg = ctx.getConfiguration();
                if (!cfg.getAppenders().containsKey(APPENDER_NAME)) {
                    // 尝试重新安装
                    install();
                }
            } catch (Throwable ignored) { /* silent */ }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    public static void addListener(LogListener l) { if (l != null && !listeners.contains(l)) listeners.add(l); }
    public static void removeListener(LogListener l) { listeners.remove(l); }

    public static List<LogEvent> snapshotHistory() {
        synchronized (history) { return new ArrayList<>(history); }
    }
    public static List<LogEvent> getMessagesSnapshot(){ return snapshotHistory(); }
    public static int getHistorySize() { synchronized (history) { return history.size(); } }

    private static final class BroadcastAppender extends AbstractAppender {
        protected BroadcastAppender(String name, Filter filter, Layout<?> layout) { super(name, filter, layout, true, null); }
        @Override public void append(LogEvent event) {
            LogEvent immutable = event != null ? event.toImmutable() : null;
            if (immutable == null) return;
            synchronized (history) {
                if (history.size() >= HISTORY_CAPACITY) history.removeFirst();
                history.addLast(immutable);
            }
            for (LogListener l : listeners) l.onEvent(immutable);
        }
    }
}
