/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.rename;

import com.ja.ssas.tabular.common.EnumDataTypes;
import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.common.ModelUtil;
import com.ja.ssas.tabular.common.R;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class TabularRename {

    private Logger logger = null;
    private final JSONObject model;
    private final JSONArray tables;
    private final JSONArray relationShips;
    private final JSONArray roles;
    HashMap<String, String> tablecolExp;
    HashMap<String, HashMap<String, String>> localMap = new HashMap<>();
    HashMap<String, String> measureMap = new HashMap<>();
    private final String pattern = "[\']*[A-Za-z][A-Za-z_0-9 ]{3,100}+[\']*\\[[^\\[]+\\]";
    private final String patternLocal = "(?<![\']?[A-Za-z][A-Za-z_0-9 ]{3,100}+[\']?)\\[[^\\[\\]]+\\]";
    private final String patternTable = "\'[^\'\\[]+\'(?=[^\\[])";
    private boolean doRename;

    private TabularRename(JSONObject model, String modelname) {
        this.model = model;
        this.doRename = true;
        JSONObject jsonObjectModel = model.getJSONObject("model");
        this.tables = jsonObjectModel.getJSONArray("tables");
        logger = LogCountHandler.getInstance().getLogger("Model Name:" + modelname);
        this.relationShips = jsonObjectModel.getJSONArray("relationships");
        if (jsonObjectModel.has("roles")) {
            this.roles = jsonObjectModel.getJSONArray("roles");
        } else {
            this.roles = new JSONArray();
        }
    }

    public TabularRename(JSONObject model, String modelname, boolean doRename) {
        this(model, modelname);
        this.doRename = doRename;
    }

    public void rename(String outFile, HashMap<String, String> tablecolExp) throws FileNotFoundException {
        rename(tablecolExp);
        try (PrintWriter out = new PrintWriter(outFile)) {
            out.println(model.toString(2));
        }
    }

    public JSONObject rename(HashMap<String, String> tablecolExp) {
        this.tablecolExp = tablecolExp;
        renameColumns();
        renameCalculatedColumns();
        renameRelationships();
        renameRoles();
        renameTables();
        return model;
    }

    private void renameTables() {
        for (int i = 0; i < tables.length(); i++) {
            JSONObject table = tables.getJSONObject(i);
            String tableName = table.getString("name");
            String tableKey = tableName.toLowerCase();
            String renameTable = tablecolExp.get(tableKey);
            String dataCategory = tablecolExp.get(tableKey + "#dataCategory");
            String isHidden = tablecolExp.get(tableKey + "#isHidden");
            String description = tablecolExp.get(tableKey + "#description");
            logger.log(Level.FINE, "Changing name of table:[{0}] to:[{1}]", new Object[]{tableName, renameTable});
            if (renameTable != null) {
                if (this.doRename) {
                    table.put("name", renameTable);
                }
            } else {
                logger.log(Level.WARNING, "Rename table name is null for table:[{0}] to:[{1}]", new Object[]{tableName, renameTable});
            }
            if (dataCategory != null) {
                logger.log(Level.FINEST, "Renaming table:[{0}] adding dataCategory to :[{1}]", new Object[]{tableName, dataCategory});
                table.put("dataCategory", dataCategory);
            }

            if (isHidden != null) {
                logger.log(Level.FINEST, "Renaming table:[{0}] adding isHidden to :[{1}]", new Object[]{tableName, isHidden});
                setValue(table, "isHidden", isHidden);
            }

            if (description != null) {
                logger.log(Level.FINEST, "Renaming table:[{0}] adding description to :[{1}]", new Object[]{tableName, description});
                table.put("description", description);
            }
        }
    }

    private void renameColumns() {
        String commentKey = Model.Comments._COMMENTS.toString();
        for (int i = 0; i < tables.length(); i++) {
            JSONObject table = tables.getJSONObject(i);
            String tableName = table.getString("name");
            HashMap<String, String> tableLocal = new HashMap<>();
            logger.log(Level.FINE, "Changing columns for:[{0}]", tableName);
            JSONArray columns = table.getJSONArray("columns");
            for (int j = 0; j < columns.length(); j++) {
                JSONObject column = columns.getJSONObject(j);
                
                String columnName = column.getString("name");
                String newColumnName = columnName;
                String key = (tableName + "#" + columnName).toLowerCase();
                if(R.COMMENTS){
                    String comments = tablecolExp.getOrDefault(key+"#"+commentKey, "");
                    ModelUtil.setComments(column, comments);
                }
                if (!column.has("isNameInferred")) {
                    String newValue = tablecolExp.get(key);
                    if (newValue != null) {
                        newColumnName = newValue;
                        if (this.doRename) {
                            column.put("name", newColumnName);
                        }
                    } else {
                        logger.log(Level.WARNING, "Not Found in Tables for [{0}]:[{1}]", new Object[]{tableName, key});
                    }
                }
                if (column.has("type") && column.getString("type").equals("calculated")) {
                    setCalculatedValues(column, key);
                } else {
                    setAdditionalValues(column, key);
                }

                String keyL = ("[" + columnName + "]").toLowerCase();
                tableLocal.put(keyL, "[" + newColumnName + "]");
            }
            localMap.put(tableName, tableLocal);
            //Measure need lookup as it is at model level
            if (table.has("measures")) {
                JSONArray measures = table.getJSONArray("measures");
                for (int j = 0; j < measures.length(); j++) {
                    JSONObject measure = measures.getJSONObject(j);
                    String columnName = measure.getString("name");
                    String localKey = ("[" + columnName + "]").toLowerCase();
                    measureMap.put(localKey, tableName);
                }
            }

        }
    }

    private void renameCalculatedColumns() {
        for (int i = 0; i < tables.length(); i++) {
            JSONObject table = tables.getJSONObject(i);
            String tableName = table.getString("name");
            logger.log(Level.FINE, ">>>>>>>>Processing Table:[{0}]", tableName);
            logger.log(Level.FINE, "Changing Calculated columns for:[{0}]", tableName);
            JSONArray columns = table.getJSONArray("columns");
            HashMap<String, String> tableLocal = new HashMap<>();
            tableLocal.putAll(localMap.get(tableName));
            for (int j = 0; j < columns.length(); j++) {
                JSONObject column = columns.getJSONObject(j);
                String reColumnName = column.optString("name", "NO_NAME");
                if (column.has("type") && column.getString("type").equals("calculated")) {
                    Object expression = column.get("expression");
                    expressionParse(expression, column, "expression", tableLocal, tableName);

                }
                if (column.has("sortByColumn")) {
                    String columnName = column.getString("sortByColumn");
                    String key = (tableName + "#" + columnName).toLowerCase();
                    String newValue = tablecolExp.get(key);
                    if (newValue != null) {
                        if (this.doRename) {
                            column.put("sortByColumn", newValue);
                        }
                    } else {
                        logger.log(Level.WARNING, "Not Found in sortByColumn for [{0}] in column [{1}]:[{2}]", new Object[]{tableName, reColumnName, key});
                    }
                }

            }
            logger.log(Level.FINE, "Changing measures for:[{0}]", tableName);
            if (table.has("measures")) {
                JSONArray measures = table.getJSONArray("measures");
                for (int j = 0; j < measures.length(); j++) {
                    JSONObject measure = measures.getJSONObject(j);
                    String columnName = measure.getString("name");
                    String newColumnName = "";
                    String key = (tableName + "#" + columnName).toLowerCase();
                    String newValue = tablecolExp.get(key);
                    if (newValue != null) {
                        newColumnName = newValue;
                        if (this.doRename) {
                            measure.put("name", newColumnName);
                        }
                    } else {
                        logger.log(Level.WARNING, "Not Found in Table [{0}] in measure [{1}]:[{2}]", new Object[]{tableName, columnName, key});
                    }

                    setAdditionalMeasureValue(measure, key);

                    String localKey = ("[" + columnName + "]").toLowerCase();
                    tableLocal.put(localKey, "[" + newColumnName + "]");

                }
                logger.log(Level.FINE, "Changing measures Expression for:[{0}]", tableName);
                for (int j = 0; j < measures.length(); j++) {
                    JSONObject measure = measures.getJSONObject(j);
                    Object expression = measure.get("expression");
                    expressionParse(expression, measure, "expression", tableLocal, tableName);
                }
            }

            if (table.has("hierarchies")) {
                logger.log(Level.FINE, "Changing hierarchies Columns for:[{0}]", tableName);
                JSONArray hierarchies = table.getJSONArray("hierarchies");
                for (int j = 0; j < hierarchies.length(); j++) {
                    JSONObject hierarchy = hierarchies.getJSONObject(j);
                    String hierName = hierarchy.getString("name");
                    String hKey = (tableName + "." + hierName).toLowerCase();
                    String newValue = tablecolExp.get(hKey);
                    if (newValue != null) {
                        if (this.doRename) {
                            hierarchy.put("name", newValue);
                        }
                    } else {
                        logger.log(Level.WARNING, "Not Found in hierarchies for [{0}]:[{1}]", new Object[]{hierName, hKey});
                    }
                    setAdditionalHierarchyValue(hierarchy, hKey);
                    if (hierarchy.has("levels")) {
                        JSONArray levels = hierarchy.getJSONArray("levels");
                        for (int k = 0; k < levels.length(); k++) {
                            JSONObject level = levels.getJSONObject(k);
                            String columnName = level.getString("column");
                            String key = (tableName + "#" + columnName).toLowerCase();
                            newValue = tablecolExp.get(key);
                            if (newValue != null) {
                                if (this.doRename) {
                                    level.put("column", newValue);
                                    level.put("name", newValue);
                                }
                            } else {
                                logger.log(Level.WARNING, "Not Found in hierarchies for [{0}]:[{1}]", new Object[]{tableName, key});
                            }
                        }
                    }

                }
            }
        }
    }

    private void renameRoles() {

        for (int i = 0; i < roles.length(); i++) {
            JSONObject role = roles.getJSONObject(i);
            String roleName = role.getString("name");
            //System.out.println(tableName);
            if (role.has("tablePermissions")) {
                JSONArray tablePermissions = role.getJSONArray("tablePermissions");
                for (int j = 0; j < tablePermissions.length(); j++) {
                    JSONObject tablePermission = tablePermissions.getJSONObject(j);
                    String tableName = tablePermission.getString("name");
                    String tableKey = tableName.toLowerCase();
                    String renameTable = tablecolExp.get(tableKey);
                    logger.log(Level.FINE, "Changing name of table in Role [{2}]:[{0}] to:[{1}]", new Object[]{tableName, renameTable, roleName});
                    if (renameTable != null) {
                        if (this.doRename) {
                            tablePermission.put("name", renameTable);
                        }
                    } else {
                        logger.log(Level.WARNING, "Table name not found in Role [{2}] for table:[{0}] to:[{1}]", new Object[]{tableName, renameTable, roleName});
                    }

                    logger.log(Level.FINE, "Changing Role:[{0}]", roleName);
                    logger.log(Level.FINE, "Changing filterExpression:[{0}]", roleName);
                    if (tablePermission.has("filterExpression")) {
                        Object filterExpression = tablePermission.get("filterExpression");
                        expressionParse(filterExpression, tablePermission, "filterExpression", new HashMap<>(), "Role:" + roleName);
                    }
                    logger.log(Level.FINE, "Changing columnPermissions:[{0}]", roleName);
                    if (tablePermission.has("columnPermissions")) {
                        JSONArray columnPermissions = tablePermission.getJSONArray("columnPermissions");
                        for (int k = 0; k < columnPermissions.length(); ++k) {
                            JSONObject columnPermission = columnPermissions.getJSONObject(i);
                            String columnName = columnPermission.getString("name");
                            String key = tableName + "#" + columnName;
                            String newValue = tablecolExp.get(key.toLowerCase());
                            if (newValue != null) {
                                if (this.doRename) {
                                    columnPermission.put("name", newValue);
                                }
                            } else {
                                logger.log(Level.WARNING, "Column not found in Role [{2}] for table:[{0}] key:[{1}]", new Object[]{tableName, key, roleName});
                            }
                        }

                    }

                }
            }

        }
    }

    private void renameRelationships() {
        for (int i = 0; i < relationShips.length(); i++) {
            JSONObject relation = relationShips.getJSONObject(i);
            String fromTable = relation.getString("fromTable");
            String fromColumn = relation.getString("fromColumn");
            String toTable = relation.getString("toTable");
            String toColumn = relation.getString("toColumn");
            String keyFrom = fromTable + "#" + fromColumn;
            String keyTo = toTable + "#" + toColumn;
            logger.log(Level.FINE, "Relationship:[{0}]", keyFrom + "->" + keyTo);
            String newValue = tablecolExp.get(keyFrom.toLowerCase());
            if (newValue != null) {
                if (this.doRename) {
                    relation.put("fromColumn", newValue);
                }
            } else {
                logger.log(Level.WARNING, "Not Found from Column for Relationship[{1}]:[{0}]", new Object[]{keyFrom, keyFrom + "->" + keyTo});
            }
            newValue = tablecolExp.get(keyTo.toLowerCase());
            if (newValue != null) {
                if (this.doRename) {
                    relation.put("toColumn", newValue);
                }
            } else {
                logger.log(Level.WARNING, "Not Found to Column for Relationship[{1}]:[{0}]", new Object[]{keyTo, keyFrom + "->" + keyTo});
            }

            newValue = tablecolExp.get(fromTable.toLowerCase());
            if (newValue != null) {
                if (this.doRename) {
                    relation.put("fromTable", newValue);
                }
            } else {
                logger.log(Level.WARNING, "Not Found from Table for Relationship[{1}]:[{0}]", new Object[]{fromTable, keyFrom + "->" + keyTo});
            }
            newValue = tablecolExp.get(toTable.toLowerCase());
            if (newValue != null) {
                if (this.doRename) {
                    relation.put("toTable", newValue);
                }
            } else {
                logger.log(Level.WARNING, "Not Found to Table for Relationship[{1}]:[{0}]", new Object[]{toTable, keyFrom + "->" + keyTo});
            }

        }
    }

    private void setAdditionalValues(JSONObject column, String key) {
        String isHidden = tablecolExp.get(key + "#" + "isHidden");
        setValue(column, "isHidden", isHidden);

        String format = tablecolExp.get(key + "#" + "formatString");
        setValue(column, "formatString", format);

        String displayFolder = tablecolExp.get(key + "#" + "displayFolder");
        setValue(column, "displayFolder", displayFolder);

        String summarizeBy = tablecolExp.get(key + "#" + "summarizeBy");
        setValue(column, "summarizeBy", summarizeBy);

        String dataType = tablecolExp.get(key + "#" + "dataType");
        setOptValue(column, "dataType", dataType);

        String sourceProviderType = tablecolExp.get(key + "#" + "sourceProviderType");
        setOptValue(column, "sourceProviderType", sourceProviderType);

        String isKey = tablecolExp.get(key + "#" + "isKey");
        setOptValue(column, "isKey", isKey);

        String description = tablecolExp.get(key + "#" + "description");
        setOptValue(column, "description", description);

        String sortByColumn = tablecolExp.get(key + "#" + "sortByColumn");
        setOptValue(column, "sortByColumn", sortByColumn);

        String isNullable = tablecolExp.get(key + "#" + "isNullable");
        setOptValue(column, "isNullable", isNullable);
    }

    private void setCalculatedValues(JSONObject column, String key) {
        String isHidden = tablecolExp.get(key + "#" + "isHidden");
        setOptValue(column, "isHidden", isHidden);

        String format = tablecolExp.get(key + "#" + "formatString");
        setOptValue(column, "formatString", format);

        String displayFolder = tablecolExp.get(key + "#" + "displayFolder");
        setOptValue(column, "displayFolder", displayFolder);

        String summarizeBy = tablecolExp.get(key + "#" + "summarizeBy");
        setOptValue(column, "summarizeBy", summarizeBy);

        String dataType = tablecolExp.get(key + "#" + "dataType");
        setOptValue(column, "dataType", dataType);

        String isKey = tablecolExp.get(key + "#" + "isKey");
        setOptValue(column, "isKey", isKey);

        String description = tablecolExp.get(key + "#" + "description");
        setOptValue(column, "description", description);

        String sortByColumn = tablecolExp.get(key + "#" + "sortByColumn");
        setOptValue(column, "sortByColumn", sortByColumn);
    }

    private void setAdditionalMeasureValue(JSONObject column, String key) {
        String isHidden = tablecolExp.get(key + "#" + "isHidden");
        setOptValue(column, "isHidden", isHidden);

        String format = tablecolExp.get(key + "#" + "formatString");
        setOptValue(column, "formatString", format);

        String displayFolder = tablecolExp.get(key + "#" + "displayFolder");
        setOptValue(column, "displayFolder", displayFolder);

        String dataType = tablecolExp.get(key + "#" + "dataType");
        setOptValue(column, "dataType", dataType);

        String description = tablecolExp.get(key + "#" + "description");
        setOptValue(column, "description", description);
    }

    private void setAdditionalHierarchyValue(JSONObject hierarchy, String key) {
        String isHidden = tablecolExp.get(key + "#" + "isHidden");
        setValue(hierarchy, "isHidden", isHidden);
        String displayFolder = tablecolExp.get(key + "#" + "displayFolder");
        setValue(hierarchy, "displayFolder", displayFolder);
    }

    private void setValue(JSONObject column, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            String dataType = EnumDataTypes.getDataType(key);
            if (EnumDataTypes.BOOLEAN.equals(dataType)) {
                column.put(key, Boolean.parseBoolean(value));
            } else if (EnumDataTypes.INT.equals(dataType)) {
                column.put(key, Integer.parseInt(value));
            } else {
                column.put(key, value);
            }

        } else {
            if (column.has(key)) {
                column.remove(key);
            }
        }
    }

    private void setOptValue(JSONObject column, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            String dataType = EnumDataTypes.getDataType(key);
            if (EnumDataTypes.BOOLEAN.equals(dataType)) {
                column.put(key, Boolean.parseBoolean(value));
            } else if (EnumDataTypes.INT.equals(dataType)) {
                column.put(key, Integer.parseInt(value));
            } else {
                column.put(key, value);
            }

        }
    }

    private void expressionParse(Object expression, JSONObject column, String key, HashMap<String, String> localMap, String tableName) {
        String columnName = column.optString("name", "NO_NAME");
        if (expression instanceof JSONArray) {
            // it's an array
            JSONArray expressionArray = (JSONArray) expression;
            List<Object> val = expressionArray.toList();
            ArrayList<String> newExpArray = new ArrayList<>();
            for (int k = 0; k < val.size(); ++k) {
                String exp = (String) val.get(k);
                exp = replaceExpression(exp, localMap, tableName, columnName);
                newExpArray.add(exp);
            }
            expressionArray = new JSONArray(newExpArray);
            column.put(key, expressionArray);
        } else {
            String exp = (String) expression;
            exp = replaceExpression(exp, localMap, tableName, columnName);
            column.put(key, exp);

        }

    }

    private String replaceExpression(String exp, HashMap<String, String> localMap, String tableName, String columnName) {
        //@TODO if expression contains $ value it will fail
//        if (!this.doRename) {
//            return exp;
//        }
        Pattern p = Pattern.compile(pattern);
        Pattern pLocal = Pattern.compile(patternLocal);
        Pattern pTable = Pattern.compile(patternTable);
        logger.log(Level.FINE, "Actual Expression in tabele [{0}] in calculated column [{1}]:[{2}]", new Object[]{tableName, columnName, exp});
        // check with global names
        Matcher m = p.matcher(exp);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String b = m.group();
            logger.log(Level.FINE, "Matched:[{0}]", b);
            String reTxt = tablecolExp.get(b.toLowerCase().trim());
            if (reTxt != null) {
                if (b.startsWith(" ")) {
                    reTxt = " " + reTxt;
                }
                m.appendReplacement(sb, reTxt);
            } else {
                logger.log(Level.WARNING, "Not Found for table [{0}] in calculated column [{1}]:[{2}]", new Object[]{tableName, columnName, b});
            }
        }
        m.appendTail(sb);
        exp = sb.toString();
        // check with local tables names
        m = pLocal.matcher(exp);
        sb = new StringBuffer();
        while (m.find()) {
            String b = m.group();
            logger.log(Level.FINE, "Matched Local:[{0}]", b);
            String reTxt = localMap.get(b.toLowerCase().trim());
            if (reTxt != null) {
                if (b.startsWith(" ")) {
                    reTxt = " " + reTxt;
                }
                m.appendReplacement(sb, reTxt);
            } else {
                if (measureMap.containsKey(b.toLowerCase().trim())) {
                    logger.log(Level.FINE, "Found Measure for table [{0}] calculated column [{1}]:[{2}]", new Object[]{tableName, columnName, b});
                } else {
                    logger.log(Level.WARNING, "Not Found Local for [{0}] calculated column [{1}]:[{2}]", new Object[]{tableName, columnName, b});
                }

            }
        }
        m.appendTail(sb);
        // check with tables names
        m = pTable.matcher(sb.toString());
        sb = new StringBuffer();
        while (m.find()) {
            String b = m.group();
            logger.log(Level.FINE, "Matched Table Name:[{0}]", b);
            String reTxt = tablecolExp.get(b.toLowerCase().trim());
            if (reTxt != null) {

                if (!reTxt.startsWith("'")) {
                    reTxt = "'" + reTxt + "'";
                }
                if (b.startsWith(" ")) {
                    reTxt = " " + reTxt;
                }
                m.appendReplacement(sb, reTxt);
            } else {
                logger.log(Level.WARNING, "Not Found for table name [{0}] calculated column [{1}]:[{2}]", new Object[]{tableName, columnName, b});
            }
        }
        m.appendTail(sb);
        exp = sb.toString();
        logger.log(Level.FINE, "Final Expression:[{0}]", exp);
        return exp;
    }
}
