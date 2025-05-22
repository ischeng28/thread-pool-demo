package com.cheng;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // 使用自定义线程工厂
        ThreadFactory customThreadFactory = new CustomThreadFactory("MyCustomThread");
        
        // 创建使用自定义线程工厂的线程池
        MyThreadPool myThreadPool = new MyThreadPool(
            2, 4, 1, TimeUnit.SECONDS, 
            new ArrayBlockingQueue<>(2), 
            new DiscardRejectHandle(),
            customThreadFactory
        );
        
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
        
        // 演示使用默认线程工厂
        System.out.println("\n使用默认线程工厂的线程池:");
        MyThreadPool defaultPoolThreadPool = new MyThreadPool(
            2, 4, 1, TimeUnit.SECONDS, 
            new ArrayBlockingQueue<>(2), 
            new DiscardRejectHandle()
        );
        
        for (int i = 0; i < 3; i++) {
            final int fi = i;
            defaultPoolThreadPool.execute(() -> {
                System.out.println("默认线程工厂: " + Thread.currentThread().getName() + '/' + fi);
            });
        }
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        defaultPoolThreadPool.shutdown();
    }
}
