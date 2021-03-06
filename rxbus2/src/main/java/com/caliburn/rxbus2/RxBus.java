package com.caliburn.rxbus2;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * 高仿EventBus
 * 增加黏性事件
 * 线程安全
 * 支持EventBus ThreadMode 所有执行策略
 * 避免内部代码被访问
 *
 * @author chentong
 */
public class RxBus {
    private volatile static RxBus mRxBus;
    private final FlowableProcessor<Object> mFlowableProcessor;
    private final SubscriberMethodFinder mSubscriberMethodFinder;
    private static Map<Class<?>, Map<Class<?>, Disposable>> mDisposableMap = new HashMap<>();
    private final Map<Class<?>, Object> stickyEvents = new ConcurrentHashMap<>(); //增加sticky黏性事件
    private final Map<Object, List<Disposable>> mStickyDisposableMap = new ConcurrentHashMap<>();

    private RxBus() {
        mFlowableProcessor = PublishProcessor.create().toSerialized();
        mSubscriberMethodFinder = new SubscriberMethodFinder();
    }

    public static RxBus getDefault() {
        if (mRxBus == null) {
            synchronized (RxBus.class) {
                if (mRxBus == null) {
                    mRxBus = new RxBus();
                }
            }
        }
        return mRxBus;
    }

    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
     * are no longer interested in receiving events.
     * Subscribers have event handling methods that must be annotated by {@link Subscribe}.
     * The {@link Subscribe} annotation also allows configuration like {@link ThreadMode}.
     */
    public void register(Object subsciber) {
        Class<?> subsciberClass = subsciber.getClass();
        List<SubscriberMethod> subscriberMethods = mSubscriberMethodFinder.findSubscriberMethods(subsciberClass);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            addSubscriber(subsciber, subscriberMethod);
        }
    }

    /**
     * translate the subscriberMethod to a subscriber,and put it in a cancleable container .
     */
    private void addSubscriber(final Object subsciber, final SubscriberMethod subscriberMethod) {
        Class<?> subsciberClass = subsciber.getClass();
        Class<?> eventType = subscriberMethod.getEventType();
        Disposable disposable = mFlowableProcessor.ofType(eventType)
                .observeOn(subscriberMethod.getThreadMode())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        invokeMethod(subsciber, subscriberMethod, o);
                    }
                });

        synchronized (mDisposableMap) {
            Map<Class<?>, Disposable> disposableMap = mDisposableMap.get(subsciberClass);
            if (disposableMap == null) {
                disposableMap = new HashMap<>();
                mDisposableMap.put(subsciberClass, disposableMap);
            }
            disposableMap.put(eventType, disposable);
        }

        //support stick event add by chentong
        if (subscriberMethod.isSticky()) {
            addStickySubscriber(subsciber, subscriberMethod);
        }
    }

    /**
     * 黏性事件处理
     *
     * @param subsciber
     * @param subscriberMethod
     */
    private void addStickySubscriber(final Object subsciber, final SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.getEventType();

        if (subscriberMethod.isSticky()) {
            List<Object> objectList = new ArrayList<>();
            Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
            for (Map.Entry<Class<?>, Object> entry : entries) {
                Object stickyEvent = entry.getValue();
                objectList.add(stickyEvent);
            }

            if (objectList.isEmpty()) return;

            Disposable disposable = Observable.fromIterable(objectList)
                    .ofType(eventType)
                    .observeOn(subscriberMethod.getThreadMode())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            invokeMethod(subsciber, subscriberMethod, o);
                        }
                    });

            synchronized (mStickyDisposableMap) {
                List<Disposable> disposableList = mStickyDisposableMap.get(subsciber);
                if (disposableList == null) {
                    disposableList = new ArrayList<>();
                }
                disposableList.add(disposable);
                mStickyDisposableMap.put(subsciber, disposableList);
            }
        }
    }

    /**
     * call the subscriber method annotationed with receiverd event.
     */
    private void invokeMethod(Object subscriber, SubscriberMethod subscriberMethod, Object obj) {
        try {
            subscriberMethod.getMethod().invoke(subscriber, obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Posts the given event to the RxBus.
     */
    public void post(Object obj) {
        if (mFlowableProcessor.hasSubscribers()) {
            mFlowableProcessor.onNext(obj);
        }
    }

    /**
     * 发送黏性事件
     * add by chentong
     *
     * @param event
     */
    public void postSticky(Object event) {
        synchronized (stickyEvents) {
            stickyEvents.put(event.getClass(), event);
        }
        post(event);
    }

    /**
     * Removes the sticky event if it equals to the given event.
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEvent(Object event) {
        synchronized (stickyEvents) {
            Class<?> eventType = event.getClass();
            Object existingEvent = stickyEvents.get(eventType);
            if (event.equals(existingEvent)) {
                stickyEvents.remove(eventType);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Removes all sticky events.
     */
    public void removeAllStickyEvents() {
        synchronized (stickyEvents) {
            stickyEvents.clear();
        }
    }

    /**
     * Unregisters the given subscriber from all event classes.
     */
    public void unregister(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        synchronized (mDisposableMap) {
            Map<Class<?>, Disposable> disposableMap = mDisposableMap.get(subscriberClass);
            if (disposableMap == null) {
                throw new IllegalArgumentException(subscriberClass.getSimpleName() + " haven't registered RxBus");
            }
            Set<Class<?>> keySet = disposableMap.keySet();
            for (Class<?> evenType : keySet) {
                Disposable disposable = disposableMap.get(evenType);
                disposable.dispose();
            }
            mDisposableMap.remove(subscriberClass);
        }
        releaseStickyDisposable(subscriber);
    }

    /**
     * 释放黏性事件观察者
     *
     * @param subscriber
     */
    private void releaseStickyDisposable(Object subscriber) {
        synchronized (mStickyDisposableMap) {
            List<Disposable> disposableList = mStickyDisposableMap.get(subscriber);
            if (disposableList == null) {
                return;
            }
            for (Disposable disposable : disposableList) {
                disposable.dispose();
            }
            disposableList.clear();
            mStickyDisposableMap.remove(subscriber);
        }
    }

    /**
     * Unregisters the given subscriber of eventType from all event classes.
     */
    public void unregister(Object subscriber, Class<?> eventType) {
        Class<?> subscriberClass = subscriber.getClass();
        synchronized (mDisposableMap) {
            Map<Class<?>, Disposable> disposableMap = mDisposableMap.get(subscriberClass);
            if (disposableMap == null) {
                throw new IllegalArgumentException(subscriberClass.getSimpleName() + " haven't registered RxBus");
            }
            if (!disposableMap.containsKey(eventType)) {
                throw new IllegalArgumentException("The event with type of " + subscriberClass.getSimpleName() + " is not" +
                        " required in " + subscriberClass.getSimpleName());
            }
            Disposable disposable = disposableMap.get(eventType);
            disposable.dispose();
            mDisposableMap.remove(eventType);
        }
    }

}