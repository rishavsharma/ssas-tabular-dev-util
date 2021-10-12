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
        boolean ie = true;
        String workingFolder = "D:\\Testing\\ssas-tabular-dev-util-1.2.2\\";
        String templateFile = workingFolder+"SecuredModel_UAT_10apr_InMemory_R5.bim";
        String output_dir = workingFolder + "output";
        String excelConfig = workingFolder+"CFV_Summary_R1.5.xls";
        if (ie) {
            String r = "-t " + templateFile
                    + " -e " + excelConfig
                    + " -o " + output_dir
                    + " -darnh -s";
            ModelExcelImport.main(r.split(" "));
        }else {
            String[] r = {"-i", "\"D:\\Projects\\test\\ssas-tabular-dev-util-1.2\\test1\"", "-e",  "Export_all", "-o", "\"D:\\Projects\\test\\ssas-tabular-dev-util-1.2\"", "-darnh"};
            ModelsExcelExport.main(r);
        }

    }

}