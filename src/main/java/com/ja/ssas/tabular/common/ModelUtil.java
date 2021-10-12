/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Rishav
 */
public class ModelUtil {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("ModelUtil");
    private static final String pattern = "[\']*[A-Za-z][A-Za-z_0-9 ]{3,100}+[\']*\\[[^\\[]+\\]";
    private static final String patternDash = "[\'][A-Za-z][A-Za-z_0-9 -]{3,100}+[\']\\[[^\\[]+\\]";
    private static final String patternLocal = "(?<![\']?[A-Za-z][A-Za-z_0-9 ]{3,100}+[\']?)\\[[^\\[\\]]+\\]";
    private static final String patternTable = "\'[^\'\\[]+\'(?=[^\\[])";
    private static final Pattern pColumn = Pattern.compile(pattern);
    private static final Pattern pLocal = Pattern.compile(patternLocal);
    private static final Pattern pTable = Pattern.compile(patternTable);
    private static final Pattern pColumnDash = Pattern.compile(patternDash);

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static JSONObject readModel(String modelFile) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(modelFile));
        return new JSONObject(new String(encoded, StandardCharsets.UTF_8));
    }

    public static JSONObject readModel(File modelFile) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(modelFile.getAbsolutePath()));
        return new JSONObject(new String(encoded, StandardCharsets.UTF_8));
    }

    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static Set<String> getColumnsFromExp(String exp) {
        Set<String> retSet = new HashSet<>();
        Matcher m = pColumn.matcher(exp);
        while (m.find()) {
            String b = m.group();
            retSet.add(b.trim());
        }
        return retSet;
    }

    public static Set<String> getColumnsFromExpDash(String exp) {
        Set<String> retSet = new HashSet<>();
        Matcher m = pColumnDash.matcher(exp);
        while (m.find()) {
            String b = m.group();
            retSet.add(b.trim());
        }
        return retSet;
    }

    public static Set<String> getLocalColumnsFromExp(String exp) {
        Set<String> retSet = new HashSet<>();
        Matcher m = pLocal.matcher(exp);
        while (m.find()) {
            String b = m.group();
            retSet.add(b.trim());
        }
        return retSet;
    }

    public static Set<String> getTableFromExp(String exp) {
        Set<String> retSet = new HashSet<>();
        Matcher m = pTable.matcher(exp);
        while (m.find()) {
            String b = m.group();
            retSet.add(b.trim());
        }
        return retSet;
    }


    public static String getAnnotation(JSONObject table, String annoName, String defaultValue) {
        String name = defaultValue;
        if (table.has("annotations")) {
            JSONArray anno = table.getJSONArray("annotations");
            for (int i = 0; i < anno.length(); i++) {
                JSONObject an = anno.getJSONObject(i);
                if (an.getString("name").equalsIgnoreCase(annoName)) {
                    return an.getString("value");
                }
            }
        }
        return name;
    }

    public static void setAnnotation(JSONObject object, String annoName, String value) {
        boolean valueSet = false;
        if (value.equals("")) {
            return;
        }
        String json = "{\"name\": \"" + annoName + "\",\"value\": \"" + value + "\"}";
        if (object.has("annotations")) {
            JSONArray anno = object.getJSONArray("annotations");
            for (int i = 0; i < anno.length(); i++) {
                JSONObject an = anno.getJSONObject(i);
                if (an.getString("name").equalsIgnoreCase(annoName)) {
                    an.put("value", value);
                    valueSet = true;
                }
            }
            if (!valueSet) {
                anno.put(new JSONObject(json));
            }
        } else {
            JSONArray ano = new JSONArray();
            ano.put(new JSONObject(json));
            object.put("annotations", ano);
        }
    }

    public static void setComments(JSONObject from, JSONObject to) {
        String comments = from.optString(Model.Comments._COMMENTS.toString(), "");
        setAnnotation(to, Model.Comments._COMMENTS.toString(), comments);
    }

    public static void setCommentsAnnotaion(JSONObject from, JSONObject to) {
        String comments = getAnnotation(from, Model.Comments._COMMENTS.toString(), "");
        to.put(Model.Comments._COMMENTS.toString(), comments);
    }

    public static void setComments(JSONObject to, String comments) {
        setAnnotation(to, Model.Comments._COMMENTS.toString(), comments);
    }

    public static String getComments(JSONObject from) {
        return getAnnotation(from, Model.Comments._COMMENTS.toString(), "");
    }

    public static void putErrorInfo(JSONObject excelRow, String comment, String type) {
        if (excelRow == null) {
            return;
        }
        if (excelRow.has(Model.ExcelMetaData.__ERROR_LEVEL.toString())) {

        } else {
            excelRow.put(Model.ExcelMetaData.__ERROR_LEVEL.toString(), type);
            if (!comment.isEmpty()) {
                excelRow.put(Model.ExcelMetaData.__ERROR_COMMENT.toString(), comment);
            }
        }

    }

    public static void putErrorInfo(JSONArray excelRows, String comment, String level) {
        for (int i = 0; i < excelRows.length(); i++) {
            putErrorInfo(excelRows.getJSONObject(i), comment, level);
        }
    }

    public static JSONArray putKeyValue(JSONArray array, String key, HashMap<String, String> map) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            String value = row.getString(key);
            String newValue = map.get(value);
            if (newValue == null) {
                newValue = value;
            }
            row.put(key, newValue);
        }
        return array;
    }

    public static void main(String[] args) {
        String e = "Var QTR_DT = MAX('D_TIME'[PREV_QTR_END_DT]) RETURN CALCULATE([QTD Cost of Funds (%)],'D_TIME'[CAL_DT]=QTR_DT,D_TIME[Month End Indicator]=\"Y\",ALL('D_TIME'))";
//        ModelUtil.getColumnsFromExp(e).forEach((col)->{
//            String[] s = col.split("[\\[\\]]");
//            System.out.println(s[0]+","+s[1]);
//        });
//\'[^\'\\[]+\'(?=[^\\[])
        Pattern p = Pattern.compile("'[^\\[]+'\\[[^\\[]+\\]");
        Matcher m = p.matcher(e);
        while (m.find()) {
            String b = m.group();
            System.out.println(b);
        }
        
    }
}
