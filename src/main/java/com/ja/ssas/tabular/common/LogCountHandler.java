/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author venris
 */
public class LogCountHandler extends Handler {

    private static LogCountHandler logCountHandler = null;
    private static final HashMap<String, Integer> logMap = new HashMap<>();

    public static LogCountHandler getInstance() {
        if (logCountHandler == null) {
            logCountHandler = new LogCountHandler();
            logMap.put(Level.WARNING.getName(), 0);
            logMap.put(Level.SEVERE.getName(), 0);            
        }

        return logCountHandler;
    }

    public Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.addHandler(this);
        return logger;
    }

    public void printSummary() {
        Logger logger = Logger.getLogger("LogCountHandler");
        logMap.forEach((level, value) -> {
            if (value > 0) {
                if (level.equals(Level.SEVERE.getName())) {
                    logger.log(Level.SEVERE, "{0} {1} Issues found", new Object[]{value, level});
                } else {
                    logger.log(Level.WARNING, "{0} {1} Issues found", new Object[]{value, level});
                }

            }
        });
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().equals(Level.SEVERE) || record.getLevel().equals(Level.WARNING)) {
            Integer get = logMap.get(record.getLevel().getName());
            logMap.put(record.getLevel().getName(), get.intValue() + 1);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}
