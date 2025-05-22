package com.cheng;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程工厂示例
 */
public class CustomThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    
    public CustomThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
        // 设置为守护线程
        thread.setDaemon(true);
        // 设置线程优先级
        thread.setPriority(Thread.MAX_PRIORITY);
        return thread;
    }
}