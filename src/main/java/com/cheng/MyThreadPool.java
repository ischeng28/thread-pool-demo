package com.cheng;

import java.util.ArrayList;
import java.util.List;

public class MyThreadPool {

    List<Thread> threadList=new ArrayList<>();

    // 1.线程什么时候创建
    // 2.线程的runnable是什么?是我们提交的command吗?
    public void execute(Runnable command) {
        // 这种方式存在的问题
        // 1.频繁创建线程消耗资源
        // 2.创建的线程没有被管理起来
        Thread thread = new Thread(command);
        threadList.add(thread);
        thread.start();
    }
}
