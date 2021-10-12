/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.test;

import com.ja.ssas.tabular.common.ModelUtil;

/**
 *
 * @author rishav.sharma
 */
public class Test {

    
    public static void main(String[] args) throws Exception {
        String exp = "VAR PFS = CALCULATE(SUMX(FILTER('F_DLQCNY_DTL_RW', RELATED('H_GRP_SEGMT'[LVL_03_CD]) = \"C_2030001L03\" && 'F_DLQCNY_DTL_RW'[NPL_IND]<>\"Y\" && 'F_DLQCNY_DTL_RW'[FAC_PDUE_AMT] <> 0 && 'F_DLQCNY_DTL_RW'[EXCESS_PAST_DUE_IND] = \"Y\"), 'F_DLQCNY_DTL_RW'[TOT_ACTL_OUT_FOR_EXCESS_PDUE])) VAR NON_PFS = CALCULATE( SUMX(SUMMARIZE( FILTER('F_DLQCNY_DTL_RW', RELATED('H_GRP_SEGMT'[LVL_03_CD]) <> \"C_2030001L03\" && 'F_DLQCNY_DTL_RW'[NPL_IND]<>\"Y\" && 'F_DLQCNY_DTL_RW'[FAC_PDUE_AMT] <> 0 && 'F_DLQCNY_DTL_RW'[EXCESS_PAST_DUE_IND] = \"Y\" ),'F_DLQCNY_DTL_RW'[PRIM_CUST_EDW_ID],\"Customer Outstanding\", MAX('F_DLQCNY_DTL_RW'[TOT_ACTL_OUT_FOR_EXCESS_PDUE])), [Customer Outstanding] )) RETURN PFS + NON_PFS";
        ModelUtil.getLocalColumnsFromExp(exp).forEach((columnName) -> {
            columnName=columnName.replaceAll("[\\[\\]]", "\"");
            if(exp.contains(columnName)){
            System.out.println(columnName);
            }
        });
    }

}
