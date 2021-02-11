/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Rishav
 */
public class ExcelReadUtil {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("ExcelReadUtil");
    private Workbook workbook = null;
    private List<String> sheets;

    public ExcelReadUtil(String fileName) {
        try {
            workbook = Workbook.getWorkbook(new File(fileName));
            this.sheets = Arrays.asList(workbook.getSheetNames());
        } catch (IOException | BiffException e) {
            logger.log(Level.SEVERE, "Error with reding file:" + fileName, e);
        }
    }

    public ExcelReadUtil(File file) {
        try {
            workbook = Workbook.getWorkbook(file);
            this.sheets = Arrays.asList(workbook.getSheetNames());
        } catch (IOException | BiffException e) {
            logger.log(Level.SEVERE, "Error with reding file:" + file, e);
        }
    }

    public String[] getSheets() {
        return workbook.getSheetNames();
    }

    public boolean haveSheet(String sheet) {
        return sheets.contains(sheet);
    }

    public <E extends Enum<E>> JSONArray getSheetData(String sheetName, Class<E> fileColumns) {
        JSONArray returnval = new JSONArray();
        try {
            if (!haveSheet(sheetName)) {
                logger.log(Level.SEVERE, "Sheet not found:{0}", sheetName);
                return returnval;
            }
            Sheet sheet = workbook.getSheet(sheetName);
            int rows = sheet.getRows();
            Enum<E>[] colVal = fileColumns.getEnumConstants();
            for (int i = 1; i < rows; i++) {
                JSONObject val = new JSONObject();
                boolean rowRead = false;
                for (int j = 0; j < colVal.length; j++) {
                    String columnName = colVal[j].toString();
                    String colValue = "";
                    try {
                        colValue = sheet.getCell(j, i).getContents().trim();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.log(Level.SEVERE, "Error reading column:" +columnName+" in sheet:"+ sheetName);
                    }
                    if (!colValue.equals("")) {
                        rowRead = true;
                        String dataType = EnumDataTypes.getDataType(columnName);
                        if (EnumDataTypes.BOOLEAN.equals(dataType)) {
                            val.put(columnName, Boolean.parseBoolean(colValue));
                        } else if (EnumDataTypes.INT.equals(dataType)) {
                            val.put(columnName, Integer.parseInt(colValue));
                        } else if (EnumDataTypes.EXPRESSION.equals(dataType)) {
                            if (colValue.contains("\n")) {
                                JSONArray expressionArray = new JSONArray();
                                for (String exp : colValue.replace("\r", "").split("\n")) {
                                    expressionArray.put(exp);
                                }

                                val.put(columnName, expressionArray);
                            } else {
                                val.put(columnName, colValue);
                            }
                        } else {
                            val.put(columnName, colValue);
                        }
                    }

                }
                if (rowRead) {
                    returnval.put(val);
                }

            }
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "Error with reding file:" + sheetName, e);
        }
        return returnval;
    }

    public void close() {
        if (workbook != null) {
            workbook.close();
        }
    }

}
