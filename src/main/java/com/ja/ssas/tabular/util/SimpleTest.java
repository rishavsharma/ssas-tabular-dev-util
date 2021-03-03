/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

/**
 *
 * @author Rishav
 */
public class SimpleTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
//            String r = "-m testFiles\\MERGED_ModelsExcel_Conf_vR1.8.xls -f testFiles\\MERGED_ModelsExcel_Conf_v1.9.0.xls -x diff -o testFiles -darnh";
//            String r = "-m testFiles\\X-Border.xls -f testFiles\\X-Border_f1.xls -f testFiles\\X-Border_f2.xls -x merge -o testFiles -darnh";
//            String r = "-m testFiles\\ModelsExcel_Conf_vR1.10.29.xls -f testFiles\\Rishav_ModelsExcel_Conf_vR1.10.29.xls -f testFiles\\Ezhil_ModelsExcel_Conf_vR1.10.29.xls -darnh -x merge -o testFiles\\merge";
//            ExcelConfigDiff.main(r.split(" "));
                String r = "-i \"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\SecuredModel_UAT_18Feb.1.12.31.bim\""
                        + " -m \"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\mapping.txt\""
                        + " -o \"D:\\Projects\\test\\ssas-tabular-dev-util-1.1.2\\splitmodels\"";
                SplitModel.main(r.split(" "));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
