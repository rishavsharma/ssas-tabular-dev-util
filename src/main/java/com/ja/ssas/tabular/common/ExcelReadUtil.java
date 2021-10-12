/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private boolean readKey = false;
    private HashMap<String, Set<Integer>> sheetSet = new HashMap<String, Set<Integer>>();
    private String workBookName;
    private boolean readComment = false;

    public ExcelReadUtil(File file) {
        try {
            workbook = Workbook.getWorkbook(file);
            this.sheets = Arrays.asList(workbook.getSheetNames());
            this.workBookName = file.getName();
        } catch (IOException | BiffException e) {
            logger.log(Level.SEVERE, "Error with reding file:" + file, e);
        }
    }

    public ExcelReadUtil(File file, boolean readKey) {
        this(file);
        sheetSet = Model.getSheetKeys();
        this.readKey = readKey;
    }

    public void setReadComment(boolean readComment) {
        this.readComment = readComment;
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

            Set ordicalSet = sheetSet.get(sheetName);
            Sheet sheet = workbook.getSheet(sheetName);
            int rows = sheet.getRows();
            int columns = sheet.getColumns();
            HashMap<Integer, Enum> sheetColumnEnum = new HashMap<Integer, Enum>();
            Enum<E>[] colEnums = fileColumns.getEnumConstants();
            HashMap<String, Enum> modelColumns = new HashMap<String, Enum>();
            for (Enum e : colEnums) {
                modelColumns.put(e.toString(), e);
            }
            for (int j = 0; j < columns; j++) {
                String headerValue = sheet.getCell(j, 0).getContents().trim();
                if (modelColumns.containsKey(headerValue)) {
                    sheetColumnEnum.put(j, modelColumns.get(headerValue));
                }

                if (headerValue.equals(Model.Comments._COMMENTS.toString()) && R.COMMENTS) {
                    sheetColumnEnum.put(j, Model.Comments._COMMENTS);
                }
            }

            for (int i = 1; i < rows; i++) {
                JSONObject val = new JSONObject();
                boolean rowRead = false;
                for (int j = 0; j < columns; j++) {
                    if (!sheetColumnEnum.containsKey(j)) {
                        continue;
                    }
                    Enum colEnum = sheetColumnEnum.get(j);
                    String columnName = colEnum.toString();
                    String colValue = "";
                    try {
                        colValue = sheet.getCell(j, i).getContents().trim();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.log(Level.SEVERE, "Error reading column:" + columnName + " in sheet:" + sheetName);
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

//                    if (this.readKey && !columnName.equals(Model.Comments._COMMENTS.toString())) {
//                        if (ordicalSet.contains(colEnum.ordinal())) {
//                            colKey += colValue + " | ";
//                        } else {
//                            allColumnConcat += colValue;
//                        }
//
//                    }

                }
//                if(R.COMMENTS){
//                    try{
//                    String colValue = sheet.getCell(colEnums.length, i).getContents().trim();
//                    val.put(Model.Comments._COMMENTS.toString(), colValue);
//                    }catch(ArrayIndexOutOfBoundsException e){
//                        e.printStackTrace();
//                    }
//                }
                if (rowRead) {
                    if (this.readKey) {
                        String colKey = "";
                        String allColumnConcat = "";
                        for (Enum e : colEnums) {
                            if (ordicalSet.contains(e.ordinal())) {
                                colKey += val.optString(e.toString(), "") + " | ";
                            } else {
                                allColumnConcat += val.optString(e.toString(), "");
                            }
                        }
                        val.put(Model.ExcelMetaData.__KEY.toString(), colKey.toLowerCase());
                        val.put(Model.ExcelMetaData.__HASH.toString(), allColumnConcat.toLowerCase());
                    }
                    val.put(Model.ExcelMetaData.__ROWNUM.toString(), i + 1);
                    val.put(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), workBookName);
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
