package com.cheng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThreadPool {

    BlockingQueue<Runnable> blockingQueue;

    private final int corePoolSize;

    private final int maxSize;

    private final int timeout;

    private final TimeUnit timeUnit;
    private final RejectHandle rejectHandle;
    
    // 添加线程池状态枚举
    private enum PoolState {
        RUNNING,    // 运行状态，接受新任务并处理队列中的任务
        SHUTDOWN,   // 关闭状态，不接受新任务但处理队列中的任务
        STOP,       // 停止状态，不接受新任务，不处理队列中的任务，中断正在执行的任务
        TERMINATED  // 终止状态，所有任务都已终止，workerCount为0
    }
    
    // 添加线程池状态字段
    private volatile PoolState state = PoolState.RUNNING;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue, RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
        this.rejectHandle = rejectHandle;
    }


    // 我们的线程池中应该有多少个线程
    List<Thread> coreList = new ArrayList<>();

    List<Thread> supportList = new ArrayList<>();

    // 是有线程安全问题的
    public void execute(Runnable command) {
        // 检查线程池状态，如果已关闭则拒绝任务
        if (state != PoolState.RUNNING) {
            rejectHandle.reject(command, this);
            return;
        }
        
        if (coreList.size() < corePoolSize) {
            Thread thread = new coreThread();
            coreList.add(thread);
            thread.start();
        }

        if (blockingQueue.offer(command)) {
            System.out.println("成功放入阻塞队列");
            return;
        }

        if (coreList.size() + supportList.size() < maxSize) {
            System.out.println("队列扩容");
            Thread thread = new supportThread();
            supportList.add(thread);
            thread.start();
        }

        if (!blockingQueue.offer(command)) {
            System.out.println("阻塞队列满了");
            rejectHandle.reject(command, this);
        }
    }

    class coreThread extends Thread {
        @Override
        public void run() {
            while (state != PoolState.STOP) {
                try {
                    Runnable command = blockingQueue.take();
                    command.run();
                    //     等待阻塞队列被填充元素的过程中，如果thread被中断了，不会继续等待，而去处理异常
                    //     几乎所有需要线程等待的函数，都有这个异常
                } catch (InterruptedException e) {
                    // 线程被中断，可能是因为shutdown()或shutdownNow()被调用
                    if (state == PoolState.STOP) {
                        break;
                    }
                }
            }
            synchronized (coreList) {
                coreList.remove(this);
                if (isTerminated()) {
                    state = PoolState.TERMINATED;
                }
            }
            System.out.println(Thread.currentThread().getName() + "核心线程结束了");
        }
    }

    class supportThread extends Thread {
        @Override
        public void run() {
            while (state != PoolState.STOP) {
                try {
                    Runnable command = blockingQueue.poll(timeout, timeUnit);
                    if (command == null) {
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    // 线程被中断，可能是因为shutdown()或shutdownNow()被调用
                    if (state == PoolState.STOP) {
                        break;
                    }
                }
            }
            synchronized (supportList) {
                supportList.remove(this);
                if (isTerminated()) {
                    state = PoolState.TERMINATED;
                }
            }
            System.out.println(Thread.currentThread().getName() + "辅助线程结束了");
        }
    }
    
    /**
     * 平滑关闭线程池，不再接受新任务，但会处理队列中已有的任务
     */
    public void shutdown() {
        state = PoolState.SHUTDOWN;
        
        // 尝试中断空闲的辅助线程
        for (Thread thread : supportList) {
            if (thread != null && !thread.isInterrupted()) {
                thread.interrupt();
            }
        }
    }

    /**
     * 立即关闭线程池，尝试停止所有正在执行的任务，不再处理队列中等待的任务
     * @return 未执行的任务列表
     */
    public List<Runnable> shutdownNow() {
        state = PoolState.STOP;
        
        // 中断所有线程
        for (Thread thread : coreList) {
            if (thread != null && !thread.isInterrupted()) {
                thread.interrupt();
            }
        }
        
        for (Thread thread : supportList) {
            if (thread != null && !thread.isInterrupted()) {
                thread.interrupt();
            }
        }
        
        // 返回未执行的任务
        List<Runnable> unexecutedTasks = new ArrayList<>();
        blockingQueue.drainTo(unexecutedTasks);
        return unexecutedTasks;
    }

    /**
     * 检查线程池是否已关闭
     * @return 如果线程池已关闭返回true，否则返回false
     */
    public boolean isShutdown() {
        return state != PoolState.RUNNING;
    }

    /**
     * 检查线程池是否已终止
     * @return 如果线程池已终止返回true，否则返回false
     */
    public boolean isTerminated() {
        return (state == PoolState.SHUTDOWN || state == PoolState.STOP) 
                && coreList.isEmpty() && supportList.isEmpty() && blockingQueue.isEmpty();
    }
    
    /**
     * 等待线程池终止，直到超时或者线程池终止
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 如果线程池在超时之前终止，返回true，否则返回false
     * @throws InterruptedException 如果等待过程中线程被中断
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        
        while (nanos > 0) {
            if (isTerminated()) {
                return true;
            }
            if (nanos > 100_000_000L) { // 100ms
                Thread.sleep(100);
            } else {
                Thread.sleep(1);
            }
            nanos = deadline - System.nanoTime();
        }
        
        return isTerminated();
    }
}
