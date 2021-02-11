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
 * @author venris
 */
public class R {
    private static Map<String, String> propMap = new HashMap<>();
    static {
        
    }
    
    public String getProp(String key){
        return propMap.get(key);
    }
}
