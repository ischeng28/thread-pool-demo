package com.cheng;

/**
 * 线程工厂接口，用于创建新线程
 */
public interface ThreadFactory {
    /**
     * 创建一个新线程
     * @param runnable 线程要执行的任务
     * @return 创建的新线程
     */
    Thread newThread(Runnable runnable);
}