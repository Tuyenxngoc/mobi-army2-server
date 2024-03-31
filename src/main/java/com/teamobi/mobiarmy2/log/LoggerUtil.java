package com.teamobi.mobiarmy2.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil implements ILogManager {

    private static final String logFilePath = "logs/log.txt";
    private final Logger logger = LogManager.getLogger(LoggerUtil.class);
    private final boolean isDebugEnabled;

    public LoggerUtil(boolean isDebugEnabled) {
        this.isDebugEnabled = isDebugEnabled;
    }

    @Override
    public void logToFile(String message) {
        try (FileWriter fileWriter = new FileWriter(logFilePath, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            LocalDateTime now = LocalDateTime.now();
            String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            printWriter.println(formattedDateTime + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logMessage(String message) {
        if (isDebugEnabled) {
            logger.info(message);
        }
    }

    @Override
    public void logWarning(String message) {
        if (isDebugEnabled) {
            logger.warn(message);
        }
    }

    @Override
    public void logError(String message) {
        if (isDebugEnabled) {
            logger.error(message);
        }
    }

}
