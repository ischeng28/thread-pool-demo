package com.cheng;

public class Main {
    public static void main(String[] args) {
        MyThreadPool myThreadPool = new MyThreadPool();
        myThreadPool.execute(()->{
            System.out.println("Hello from thread pool!");
        });
    }
}
