package com.cheng;

public class ThrowRejectHandle implements RejectHandle {
    @Override
    public void reject(Runnable command, MyThreadPool threadPool) {
        throw new RuntimeException("阻塞队列满了"+command);
    }
}
