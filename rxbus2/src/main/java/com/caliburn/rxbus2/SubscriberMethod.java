package com.caliburn.rxbus2;

import java.lang.reflect.Method;

import io.reactivex.Scheduler;

/**
 * 参考eventbus
 */
class SubscriberMethod {

    final Scheduler threadMode;
    final Method method;
    final Class<?> eventType;
    final int priority;
    final boolean sticky;

    SubscriberMethod(Method method, Class<?> eventType, Scheduler threadMode, int priority, boolean sticky) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
        this.sticky = sticky;
    }

    Scheduler getThreadMode() {
        return threadMode;
    }

    Method getMethod() {
        return method;
    }

    Class<?> getEventType() {
        return eventType;
    }

    public int getPriority() {
        return priority;
    }

    boolean isSticky() {
        return sticky;
    }

}