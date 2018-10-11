package com.caliburn.rxbus2;


public enum ThreadMode {
    SINGLE,
    COMPUTATION,
    IO,
    TRAMPOLINE,
    NEW_THREAD,
    MAIN, //Android UI线程
    POSTING,//当前线程
    MAIN_ORDERED, //Android UI线程 + 队列
    BACKGROUND, //异步IO
    ASYNC // 异步IO
}