package org.unipus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.unipus.log.BroadcastAppender;
import org.unipus.ui.MainGUI;

/**
 * 乖，我们不看源代码好不好，里面全是屎山，你会遭不住的。
 *                      _
 *                     / )
 *                    ( (
 *      A.-.A  .-""-.  ) )
 *     / , , \/      \/ /
 *    =\  t  ;=    /   /
 *      `--,'  .""|   /
 *          || |  \\ \
 *         ((,_|  ((,_\
 */

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Application starting...");
        setup();
        start();
    }

    private static void setup() {
        // Programmatically register GUI broadcast appender to avoid XML plugin discovery issues in fat JARs
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();

            // If already present, skip duplicate add
            if (config.getAppenders().get("GuiAppender") == null) {
                BroadcastAppender guiAppender = BroadcastAppender.createAndStart("GuiAppender", 5000, null, null);
                config.addAppender(guiAppender);
                LoggerConfig root = config.getRootLogger();
                root.addAppender(guiAppender, Level.TRACE, null);
                ctx.updateLoggers();
            }
        } catch (Throwable t) {
            // Do not fail startup if GUI appender cannot be added; log as error so file/console capture it
            LOGGER.error("Failed to initialize GUI log appender", t);
        }
    }

    public static void start() {
        MainGUI.getInstance();
    }
}