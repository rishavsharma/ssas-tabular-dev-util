/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;


import java.io.File;

/**
 *
 * @author rishav.sharma
 */
public class Run {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(new File(".").getAbsoluteFile());
        boolean ie = false;
        
        if (ie) {
            String r = "-t \"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\splitmodels\" "
                    + "-e \"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\ModelsExcel_Conf_vR1.12.31.xls\" "
                    + "-o \"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\test1\" -darnhx";
            ModelExcelImport.main(r.split(" "));
        }else {
            String[] r = {"-i", "\"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\test1\"", "-e",  "Export_all", "-o", "\"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\"", "-darnhx"};
            ModelsExcelExport.main(r);
        }

    }

}
