/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rishav.sharma
 */
public class R {
    private static Map<String, String> propMap = new HashMap<>();
    public static int NO_OF_THREADS = 2;
    public static boolean COMMENTS = false;
    public static boolean UPDATE_AS_NEW = false;
    public static boolean OVERWRITE_TABLES = false;
    static {
        
    }
    
    public String getProp(String key){
        return propMap.get(key);
    }
}
