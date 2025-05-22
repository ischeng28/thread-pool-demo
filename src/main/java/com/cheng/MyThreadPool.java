package com.cheng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyThreadPool {

    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1024);

    private final Runnable task = () -> {
        while (true) {
            try {
                Runnable command = blockingQueue.take();
                command.run();
                //     等待阻塞队列被填充元素的过程中，如果thread被中断了，不会继续等待，而去处理异常
                //     几乎所有需要线程等待的函数，都有这个异常
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private int corePoolSize = 10;

    private int maxSize = 16;

    // 我们的线程池中应该有多少个线程
    List<Thread> coreList = new ArrayList<>();

    List<Thread> supportList = new ArrayList<>();

    // 是有线程安全问题的
    public void execute(Runnable command) {
        if (coreList.size() < corePoolSize) {
            Thread thread = new Thread(task);
            coreList.add(thread);
            thread.start();
        }

        if (blockingQueue.offer(command)) {
            return;
        }

        if (coreList.size() + supportList.size() < maxSize) {
            Thread thread = new Thread(task);
            supportList.add(thread);
            thread.start();
        }
    }
}
