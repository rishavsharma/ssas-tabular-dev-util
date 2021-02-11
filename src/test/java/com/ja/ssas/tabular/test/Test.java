/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rishav.sharma
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String in="([YTD Total IB Assets Rates(\\$) (Variance)] - [YTD Total IB Liabilities Rates(\\$) (Variance)])";
        String pattern = "[\']*[A-Za-z][A-Za-z_0-9 ]{3,100}+[\']*\\[[^\\[]+\\]";
        String measure = "\\[[^\\[]+\\]";
        String patternLocal = "(?<![\']?[A-Za-z][A-Za-z_0-9 ]{3,100}+[\']?)\\[[^\\[\\]]+\\]";
        Pattern pColumn = Pattern.compile(patternLocal);
        Matcher m = pColumn.matcher(in);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String b = m.group();
            System.out.println(b);
            m.appendReplacement(sb, b);
        }
        
        System.out.println(sb.toString());
        
    }

}
