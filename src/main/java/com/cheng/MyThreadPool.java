package com.cheng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyThreadPool {

    BlockingQueue<Runnable> commandList = new ArrayBlockingQueue<>(1024);

    Thread thread = new Thread(() -> {
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
    },"唯一线程");

    {
        thread.start();
    }

    // 1.线程什么时候创建
    // 2.线程的runnable是什么?是我们提交的command吗?
    public void execute(Runnable command) {
        // 这种方式存在的问题
        // 1.频繁创建线程消耗资源
        // 2.创建的线程没有被管理起来

        //  offer会通过返回值判断是否成功向队列中添加元素，而不是抛异常
        boolean offer = commandList.offer(command);
    }
}
