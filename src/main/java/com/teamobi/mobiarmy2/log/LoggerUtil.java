package com.teamobi.mobiarmy2.log;

import com.teamobi.mobiarmy2.constant.CommonConstant;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author tuyen
 */
public class LoggerUtil implements ILogManager {

    private final boolean isDebugEnabled;

    public LoggerUtil(boolean isDebugEnabled) {
        this.isDebugEnabled = isDebugEnabled;
    }

    private void log(String level, String message) {
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("%s [%s] - %s", formattedDateTime, level, message);

        if (isDebugEnabled) {
            System.out.println(logMessage);
        }
    }

    @Override
    public void logToFile(String message) {
        try (FileWriter fileWriter = new FileWriter(CommonConstant.logFilePath, true);
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
        log("MESSAGE", message);
    }

    @Override
    public void logWarning(String message) {
        log("WARNING", message);
    }

    @Override
    public void logError(String message) {
        log("ERROR", message);
    }

}
