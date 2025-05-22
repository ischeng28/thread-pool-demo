package com.cheng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {

    BlockingQueue<Runnable> blockingQueue;

    private final int corePoolSize;

    private final int maxSize;

    private final int timeout;

    private final TimeUnit timeUnit;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
    }


    // 我们的线程池中应该有多少个线程
    List<Thread> coreList = new ArrayList<>();

    List<Thread> supportList = new ArrayList<>();

    // 是有线程安全问题的
    public void execute(Runnable command) {
        if (coreList.size() < corePoolSize) {
            Thread thread = new coreThread();
            coreList.add(thread);
            thread.start();
        }

        if (blockingQueue.offer(command)) {
            return;
        }

        if (coreList.size() + supportList.size() < maxSize) {
            Thread thread = new supportThread();
            supportList.add(thread);
            thread.start();
        }

        if (!blockingQueue.offer(command)) {
            throw new RuntimeException("阻塞队列满了");
        }
    }

    class coreThread extends Thread {
        @Override
        public void run() {
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
        }
    }

    class supportThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable command = blockingQueue.poll(timeout, timeUnit);
                    if (command == null) {
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "辅助线程结束了");
        }
    }
}
