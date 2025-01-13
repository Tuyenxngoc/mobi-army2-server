package com.teamobi.mobiarmy2.server;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    private static class SingletonHelper {
        private static final ApplicationContext INSTANCE = new ApplicationContext();
    }

    public static ApplicationContext getInstance() {
        return ApplicationContext.SingletonHelper.INSTANCE;
    }

    private final Map<Class<?>, Object> beans = new HashMap<>();

    public <T> void registerBean(Class<T> type, T instance) {
        beans.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }

    public void clearDependencies() {
        beans.clear();
    }

}
