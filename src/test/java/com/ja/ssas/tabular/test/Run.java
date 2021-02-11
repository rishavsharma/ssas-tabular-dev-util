/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.test;

import com.ja.ssas.tabular.util.ModelExcelImport;
import com.ja.ssas.tabular.util.ModelsExcelExport;
import java.io.File;

/**
 *
 * @author venris
 */
public class Run {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(new File(".").getAbsoluteFile());
        boolean ie = true;
        if (ie) {
            String r = "-t testFiles\\SecuredModel_UAT_21Jan_v1.41.0.bim -e testFiles\\MERGED_ModelsExcel_Conf_v1.42.0.xls -o testFiles\\output -darnh -s";
            ModelExcelImport.main(r.split(" "));
        } else {
            String r = "-i testFiles\\output -e Allexported -o testFiles -darnh";
            ModelsExcelExport.main(r.split(" "));
        }

    }

}
