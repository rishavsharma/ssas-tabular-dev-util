/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

/**
 *
 * @author venris
 */
public class DiffTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String dir = "D:\\Projects\\test\\ssas-tabular-dev-util-1.2";
        String diff = "-m %1$s\\ModelsExcel_Conf_vR1.12.38.xls -f %1$s\\Changed.xls -f %1$s\\Changed1.xls -darnhu -i diff -o %1$s\\diff";
        String merge = "-m %1$s\\ModelsExcel_Conf_vR1.12.38.xls -f %1$s\\Changed.xls -f %1$s\\Changed1.xls -darnh -i merge -o %1$s\\merge";
        ExcelConfigDiff.main(String.format(merge,dir).split(" "));
    }
    
}
