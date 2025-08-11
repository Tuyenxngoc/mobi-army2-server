package com.teamobi.mobiarmy2.server;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    private ApplicationContext() {
    }

    public static ApplicationContext getInstance() {
        return ApplicationContext.SingletonHelper.INSTANCE;
    }

    public <T> void registerBean(Class<T> type, T instance) {
        beans.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        T bean = (T) beans.get(clazz);
        if (bean == null) {
            throw new IllegalArgumentException("No bean found for class: " + clazz.getName());
        }
        return bean;
    }

    public void clearDependencies() {
        beans.clear();
    }

    private static class SingletonHelper {
        private static final ApplicationContext INSTANCE = new ApplicationContext();
    }
}