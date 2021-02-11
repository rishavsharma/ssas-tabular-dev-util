/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.test;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Rishav
 */
public class SimpleTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String json="{\"expression\": [\n" +
"              \"D_CORP_COST_CTR_D[BUSN_DT] & \\\"~\\\"\",\n" +
"              \"    & D_CORP_COST_CTR_D[CORP_COST_CTR_CD]\"\n" +
"            ]\n" +
"			}";
        
        JSONObject a = new JSONObject(json);
        JSONArray aa = new JSONArray();
        System.out.println();
        a.getJSONArray("expression").forEach((exp)->{
        System.out.println(exp);
        });
        
    }
    
}
