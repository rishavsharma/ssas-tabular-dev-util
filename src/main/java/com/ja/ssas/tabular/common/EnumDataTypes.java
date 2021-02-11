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
public class EnumDataTypes {

    public static String INT = "int";
    public static String BOOLEAN = "boolean";
    public static String STRING = "string";
    public static String EXPRESSION = "expression";

    public enum DataType {
        isHidden(EnumDataTypes.BOOLEAN),
        isActive(EnumDataTypes.BOOLEAN),
        relyOnReferentialIntegrity(EnumDataTypes.BOOLEAN),
        ordinal(EnumDataTypes.INT),
        isDataTypeInferred(EnumDataTypes.BOOLEAN),
        expression(EnumDataTypes.EXPRESSION),
        isKey(EnumDataTypes.BOOLEAN),
        isNullable(EnumDataTypes.BOOLEAN);
        private final String dataType;
        private final static Map<String, DataType> CONSTANTS = new HashMap<>();

        static {
            for (DataType c : values()) {
                CONSTANTS.put(c.toString(), c);
            }
        }

        DataType(String dataType) {
            this.dataType = dataType;

        }

    }

    public static String getDataType(String en) {
        DataType get = DataType.CONSTANTS.get(en);
        if (get == null) {
            return EnumDataTypes.STRING;
        } else {
            return get.dataType;
        }
    }

}
