/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author venris
 */
public class ThreadExecuter {
    private static ThreadExecuter threadExecuter = null;
    private ExecutorService executor = null;
    private ThreadExecuter(){
        executor = Executors.newFixedThreadPool(R.NO_OF_THREADS);
    }
    public static ThreadExecuter getInstance() {
        if (threadExecuter == null) {
            threadExecuter = new ThreadExecuter();
        }
        return threadExecuter;
    }
    
    public <T> Future<T> submit(Callable<T> task){
        return executor.submit(task);
    }
    
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException{
        return executor.invokeAll(tasks);
    }
    
    public void shutdown(){
        executor.shutdown();
    }
    
    public boolean isShutdown(){
        return executor.isShutdown();
    }
}
