package com.cheng;

public interface RejectHandle {

    void reject(Runnable command,MyThreadPool threadPool);
}
