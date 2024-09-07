package com.teamobi.mobiarmy2.log;

/**
 * @author tuyen
 */
public interface ILogManager {

    void logToFile(String message);

    void logMessage(String message);

    void logWarning(String message);

    void logError(String message);

}
