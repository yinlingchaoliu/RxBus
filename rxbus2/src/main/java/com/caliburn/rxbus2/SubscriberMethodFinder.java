package com.caliburn.rxbus2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * 寻找当前类所有注解
 */
public class SubscriberMethodFinder {
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    public List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = findUsingReflection(subscriberClass);
        if (subscriberMethods.isEmpty()) {
            throw new RxBusException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        }
        return subscriberMethods;
    }

    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<>();
        Method[] methods = subscriberClass.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
                        ThreadMode threadMode = subscribeAnnotation.threadMode();
                        Class<?> eventType = parameterTypes[0];
                        int priority = subscribeAnnotation.priority();
                        boolean sticky = subscribeAnnotation.sticky();
                        subscriberMethods.add(new SubscriberMethod(method, eventType, getThreadMode(threadMode),priority,sticky));
                    }
                }
            }
        }
        return subscriberMethods;
    }

    private Scheduler getThreadMode(ThreadMode threadMode) {
        Scheduler scheduler;
        switch (threadMode) {
            case MAIN:
            case MAIN_ORDERED: //发送是异步的，处理main
                scheduler = AndroidSchedulers.mainThread();
                break;
            case IO:
            case BACKGROUND:
            case ASYNC:
                scheduler = Schedulers.io();
                break;
            case COMPUTATION:
                scheduler = Schedulers.computation();
                break;
            case SINGLE:
                scheduler = Schedulers.single();
                break;
            case TRAMPOLINE://当前进程
            case POSTING:
                scheduler = Schedulers.trampoline();
                break;
            case NEW_THREAD:
                scheduler = Schedulers.newThread();
                break;
            default:
                scheduler = AndroidSchedulers.mainThread();
                break;
        }
        return scheduler;
    }
}