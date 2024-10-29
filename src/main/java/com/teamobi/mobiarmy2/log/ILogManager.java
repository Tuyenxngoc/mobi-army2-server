package com.teamobi.mobiarmy2.log;

/**
 * @author tuyen
 */
public interface ILogManager {

    void log(String message);

    void success(String message);

    void warning(String message);

    void error(String message);

    void logException(Class<?> exceptionClass, Exception exception, String... additionalMessages);
}
