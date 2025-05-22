package com.cheng;

public class DiscardRejectHandle implements RejectHandle {
    @Override
    public void reject(Runnable command, MyThreadPool threadPool) {
        threadPool.blockingQueue.poll();
        threadPool.execute(command);
    }
}
