package com.caliburn.rxbus2;

import java.lang.reflect.Method;

import io.reactivex.Scheduler;

/**
 * 参考eventbus
 */
public class SubscriberMethod {

    final Scheduler threadMode;
    final Method method;
    final Class<?> eventType;
    final int priority;
    final boolean sticky;

    public SubscriberMethod(Method method, Class<?> eventType, Scheduler threadMode, int priority, boolean sticky) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
        this.sticky = sticky;
    }

    public Scheduler getThreadMode() {
        return threadMode;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSticky() {
        return sticky;
    }

}