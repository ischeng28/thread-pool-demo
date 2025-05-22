package com.cheng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyThreadPool {

    BlockingQueue<Runnable> commandList = new ArrayBlockingQueue<>(1024);

    private final Runnable task = () -> {
        while (true) {
            try {
                Runnable command = commandList.take();
                command.run();
                //     等待阻塞队列被填充元素的过程中，如果thread被中断了，不会继续等待，而去处理异常
                //     几乎所有需要线程等待的函数，都有这个异常
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private int corePoolSize = 10;

    // 我们的线程池中应该有多少个线程
    List<Thread> threadList = new ArrayList<>(corePoolSize);

    public void execute(Runnable command) {
        if (threadList.size() < corePoolSize) {
            Thread thread = new Thread(task);
            threadList.add(thread);
            thread.start();
        }
        boolean offer = commandList.offer(command);
    }
}
