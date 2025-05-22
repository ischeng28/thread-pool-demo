package com.cheng;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        MyThreadPool myThreadPool = new MyThreadPool(2, 4, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), new DiscardRejectHandle());
        for (int i = 0; i < 8; i++) {
            final int fi = i;
            myThreadPool.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("任务被中断: " + fi);
                    return;
                }
                System.out.println(Thread.currentThread().getName() + '/' + fi);
            });
        }
        System.out.println("主线程没有被阻塞");
        
        // 等待一些任务完成
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 平滑关闭线程池
        System.out.println("开始关闭线程池");
        myThreadPool.shutdown();
        
        // 检查线程池状态
        System.out.println("线程池是否已关闭: " + myThreadPool.isShutdown());
        System.out.println("线程池是否已终止: " + myThreadPool.isTerminated());
        
        // 等待线程池终止
        try {
            boolean terminated = myThreadPool.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("线程池是否在超时前终止: " + terminated);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
