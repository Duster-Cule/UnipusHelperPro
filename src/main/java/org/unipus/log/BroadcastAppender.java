package org.unipus.log;

/* (っ*´Д`)っ 小代码要被看光啦 */

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Plugin(name = "BroadcastAppender", category = "Core", elementType = "appender", printObject = true)
public final class BroadcastAppender extends AbstractAppender {

    private static volatile BroadcastAppender instance;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<LogCapture.LogListener> listeners = new CopyOnWriteArrayList<>();
    private final Deque<LogEvent> history = new ArrayDeque<>();
    private final int historyCapacity;

    private BroadcastAppender(String name, Filter filter, Layout<? extends Serializable> layout,
                              boolean ignoreExceptions, Property[] properties, int historyCapacity) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.historyCapacity = historyCapacity;
        instance = this;
    }

    @PluginFactory
    public static BroadcastAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("historyCapacity") Integer historyCapacity,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for BroadcastAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        int capacity = (historyCapacity == null || historyCapacity <= 0) ? 5000 : historyCapacity;
        return new BroadcastAppender(name, filter, layout, true, null, capacity);
    }

    /**
     * Programmatic factory to create and start the appender without Log4j plugin discovery.
     */
    public static BroadcastAppender createAndStart(String name, Integer historyCapacity,
                                                   Layout<? extends Serializable> layout,
                                                   Filter filter) {
        if (name == null || name.isEmpty()) name = "GuiAppender";
        if (layout == null) layout = PatternLayout.createDefaultLayout();
        int capacity = (historyCapacity == null || historyCapacity <= 0) ? 5000 : historyCapacity;
        BroadcastAppender appender = new BroadcastAppender(name, filter, layout, true, null, capacity);
        appender.start();
        return appender;
    }

    public static BroadcastAppender getInstance() {
        return instance;
    }

    @Override
    public void append(LogEvent event) {
        // The event needs to be immutable before passing to another thread.
        LogEvent logEvent = event.toImmutable();
        lock.writeLock().lock();
        try {
            if (history.size() >= historyCapacity) {
                history.removeFirst();
            }
            history.addLast(logEvent);
        } finally {
            lock.writeLock().unlock();
        }

        for (final LogCapture.LogListener listener : listeners) {
            listener.onEvent(logEvent);
        }
    }

    public void addListener(LogCapture.LogListener listener) {
        listeners.add(listener);
        // Replay history for the new listener
        List<LogEvent> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(history);
        } finally {
            lock.readLock().unlock();
        }
        for (LogEvent event : snapshot) {
            listener.onEvent(event);
        }
    }

    public void removeListener(LogCapture.LogListener listener) {
        listeners.remove(listener);
    }

    /**
     * 获取所有历史日志的快照
     * @return 历史日志列表的副本
     */
    public List<LogEvent> getAllLogs() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(history);
        } finally {
            lock.readLock().unlock();
        }
    }
}
