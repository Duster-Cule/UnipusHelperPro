package org.unipus.log;

/* (っ*´Д`)っ 小代码要被看光啦 */

import org.apache.logging.log4j.core.LogEvent;

/**
 * A bridge to access the singleton BroadcastAppender instance.
 * UI components can use this class to add or remove log listeners.
 */
public final class LogCapture {

    public interface LogListener {
        void onEvent(LogEvent event);
    }

    private LogCapture() {
    }

    /**
     * Adds a listener to the BroadcastAppender.
     * If the appender is not yet initialized, this call will have no effect.
     * The listener will immediately receive a replay of historical log events.
     *
     * @param listener The listener to add.
     */
    public static void addListener(LogListener listener) {
        BroadcastAppender appender = BroadcastAppender.getInstance();
        if (appender != null) {
            appender.addListener(listener);
        }
    }

    /**
     * Removes a listener from the BroadcastAppender.
     *
     * @param listener The listener to remove.
     */
    public static void removeListener(LogListener listener) {
        BroadcastAppender appender = BroadcastAppender.getInstance();
        if (appender != null) {
            appender.removeListener(listener);
        }
    }
}
