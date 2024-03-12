package it.polimi.ds.utils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorInstance {
    private final ExecutorService executorService;
    private static ExecutorInstance instance;

    private ExecutorInstance(){
        this.executorService = Executors.newCachedThreadPool();
    }

    public static ExecutorInstance getInstance(){
        if(Objects.isNull(instance))
            instance = new ExecutorInstance();
        return instance;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
