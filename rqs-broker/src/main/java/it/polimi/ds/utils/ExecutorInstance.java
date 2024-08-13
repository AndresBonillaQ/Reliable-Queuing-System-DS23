package it.polimi.ds.utils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorInstance {
    private final ThreadPoolExecutor executor;
    private static ExecutorInstance instance;

    private ExecutorInstance(){
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
    }

    public static ExecutorInstance getInstance(){
        if(Objects.isNull(instance))
            instance = new ExecutorInstance();
        return instance;
    }

    public ExecutorService getExecutorService() {
        return executor;
    }

    public void logging(){
        System.out.println("Thread attivi: " + executor.getActiveCount());
        System.out.println("Task in coda: " + executor.getQueue().size());
        System.out.println("Numero massimo di thread usati: " + executor.getLargestPoolSize());
        System.out.println("Numero di task completati: " + executor.getCompletedTaskCount());
        System.out.println("----------------------------");
    }
}
