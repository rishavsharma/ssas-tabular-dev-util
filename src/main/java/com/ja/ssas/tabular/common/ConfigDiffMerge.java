/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class ConfigDiffMerge {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("ConfigDiffMerge");

    public static <E extends Enum<E>> JSONArray getDiffArray(JSONArray master, JSONArray feature, Class<E> sheetColumn, String featureFileName) {
        Enum<E>[] colVal = sheetColumn.getEnumConstants();
        String sheetName = Model.ExcelSheets.getSheetNameFromClass(sheetColumn);
        JSONArray returnval = new JSONArray();
        HashMap<String, JSONObject> masterMap = new HashMap<>();
        HashMap<String, JSONObject> featureMap = new HashMap<>();
        int srcLength = master.length();
        int tgtLength = feature.length();
        int maxLength = Math.max(srcLength, tgtLength);
        for (int i = 0; i < maxLength; i++) {
            if (srcLength > i) {
                JSONObject val = master.getJSONObject(i);
                String masterKey = val.optString(Model.ExcelMetaData.__KEY.toString(), "");
                if (masterMap.containsKey(masterKey)) {
                    String row = val.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                    String workbook = val.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                    logger.log(Level.WARNING, "[{0}] Duplicate Key exist in {1}[{2}] in sheet {3}", new Object[]{masterKey, workbook, row, sheetName});
                }
                masterMap.put(masterKey, val);
            }

            if (tgtLength > i) {
                JSONObject val = feature.getJSONObject(i);
                String featureKey = val.optString(Model.ExcelMetaData.__KEY.toString(), "");
                if (featureMap.containsKey(featureKey)) {
                    String row = val.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                    String workbook = val.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                    logger.log(Level.WARNING, "[{0}] Duplicate Key exist in {1}[{2}] in sheet {3}", new Object[]{featureKey, workbook, row, sheetName});
                }
                featureMap.put(featureKey, val);
            }
        }
        Set<String> allKeys = masterMap.keySet().stream().collect(Collectors.toSet());
        Set<String> tgtKeys = featureMap.keySet().stream().collect(Collectors.toSet());
        allKeys.addAll(tgtKeys);
        for (String key : allKeys) {
            JSONObject srcVal = masterMap.get(key);
            JSONObject tgtVal = featureMap.get(key);
            if (srcVal == null) {
                String row = tgtVal.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                String workbook = tgtVal.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                String comment = "New Row From:" + workbook + "[" + row + "]";
                //ModelUtil.putErrorInfo(tgtVal, comment, Model.ExcelMetaData.__INFO.toString());
                tgtVal.put(Model.MergeMetaData.__ROWCOMENT.toString(), comment);
                tgtVal.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__INSERT.toString());
                returnval.put(tgtVal);
            } else if (tgtVal == null) {
                String workbook = srcVal.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                //ModelUtil.putErrorInfo(srcVal, "Deleted Row from:" + workbook, Model.ExcelMetaData.__ERROR.toString());
                srcVal.put(Model.MergeMetaData.__ROWCOMENT.toString(), "Deleted Row from:" + workbook);
                srcVal.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__DELETE.toString());
                srcVal.put(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), featureFileName);
                returnval.put(srcVal);
            } else if (!srcVal.optString(Model.ExcelMetaData.__HASH.toString(), "").equals(tgtVal.optString(Model.ExcelMetaData.__HASH.toString(), ""))) {
                JSONObject formatingObject = new JSONObject();
                for (Enum<E> col : colVal) {
                    String colName = col.toString();
                    String srcCell = srcVal.optString(colName, "");
                    String tgtCell = tgtVal.optString(colName, "");
                    if (!srcCell.equals(tgtCell)) {
                        formatingObject.put(colName, srcCell);
                    }
                }
                String workbook = tgtVal.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                String row = tgtVal.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                tgtVal.put(Model.MergeMetaData.__ROWCOMENT.toString(), "Updated Row from:" + workbook + "[" + row + "]");
                tgtVal.put(Model.ExcelMetaData.__DIFF.toString(), formatingObject);
                tgtVal.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__UPDATE.toString());
                if (R.UPDATE_AS_NEW) {
                    String srcworkbook = srcVal.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                    String srcrow = srcVal.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                    srcVal.put(Model.MergeMetaData.__ROWCOMENT.toString(), "Orginal Row from:" + srcworkbook + "[" + srcrow + "]");
                    srcVal.put(Model.ExcelMetaData.__DIFF.toString(), formatingObject);
                    srcVal.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__ORIGINAL.toString());
                    returnval.put(srcVal);
                }
                returnval.put(tgtVal);
            }
        }
        return returnval;
    }

    public static JSONArray getMergeArray(JSONArray master, List<JSONArray> diffFeatures, Class<? extends Enum> sheetColumn, boolean emitConflicts) {
        Enum[] colVal = sheetColumn.getEnumConstants();
        JSONArray returnval = new JSONArray();
        HashMap<String, List<JSONObject>> conflicts = new HashMap<>();
        HashMap<String, JSONObject> dirtyChangeRow = new HashMap<>();
        HashMap<String, JSONObject> masterMap = new HashMap<>();
        for (int i = 0; i < master.length(); i++) {
            JSONObject val = master.getJSONObject(i);
            String rowKey = val.optString(Model.ExcelMetaData.__KEY.toString(), "");
            masterMap.put(rowKey, val);
        }
        for (JSONArray diffArray : diffFeatures) {
            for (int i = 0; i < diffArray.length(); i++) {
                JSONObject row = diffArray.getJSONObject(i);
                String rowKey = row.optString(Model.ExcelMetaData.__KEY.toString(), "");

                Model.MergeMetaData op = Model.MergeMetaData.valueOf(row.getString(Model.MergeMetaData.__OP_TYPE.toString()));
                if (dirtyChangeRow.containsKey(rowKey)) {
                    JSONObject exitingChange = dirtyChangeRow.get(rowKey);
                    Model.MergeMetaData oldOp = Model.MergeMetaData.valueOf(exitingChange.getString(Model.MergeMetaData.__OP_TYPE.toString()));
                    if (op.equals(oldOp)) {
                        if (op.equals(Model.MergeMetaData.__DELETE)) {
                            dirtyChangeRow.put(rowKey, row);
                        } else {
                            JSONObject masterRow = masterMap.get(rowKey);
                            if (threeWayMerge(masterRow, row, exitingChange, sheetColumn)) {
                                dirtyChangeRow.put(rowKey, row);
                            } else {
                                List<JSONObject> conflict = conflicts.get(rowKey);
                                if (conflict == null) {
                                    conflict = new ArrayList<>();
                                    conflicts.put(rowKey, conflict);
                                }
                                conflict.add(row);
                                conflict.add(exitingChange);
                                String work1 = row.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                                String work2 = exitingChange.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                                String work1row = row.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                                String work2Row = exitingChange.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                                String err = "Conflicts for :" + rowKey + " in workbooks:" + work1 + "[" + work1row + "][" + op.toString() + "] and " + work2 + "[" + work2Row + "][" + oldOp.toString() + "]";
                                row.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__CONFLICT.toString());
                                row.put(Model.MergeMetaData.__ROWCOMENT.toString(), err);
                                exitingChange.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__CONFLICT.toString());
                                exitingChange.put(Model.MergeMetaData.__ROWCOMENT.toString(), err);
                                logger.log(Level.SEVERE, err);
                            }
                        }
                    } else {
                        List<JSONObject> conflict = conflicts.get(rowKey);
                        if (conflict == null) {
                            conflict = new ArrayList<>();
                            conflicts.put(rowKey, conflict);
                        }
                        conflict.add(row);
                        conflict.add(exitingChange);
                        String work1 = row.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                        String work2 = exitingChange.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
                        String work1row = row.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                        String work2Row = exitingChange.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
                        String err = "Conflicts for :" + rowKey + " in workbooks:" + work1 + "[" + work1row + "][" + op.toString() + "] and " + work2 + "[" + work2Row + "][" + oldOp.toString() + "]";
                        row.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__CONFLICT.toString());
                        row.put(Model.MergeMetaData.__ROWCOMENT.toString(), err);
                        exitingChange.put(Model.MergeMetaData.__OP_TYPE.toString(), Model.MergeMetaData.__CONFLICT.toString());
                        exitingChange.put(Model.MergeMetaData.__ROWCOMENT.toString(), err);
                        logger.log(Level.SEVERE, err);
                    }

                } else {
                    dirtyChangeRow.put(rowKey, row);
                }
                //conflicts.get(rowKey).accumulate(Model.MergeMetaData.__CONFLICT.toString(), row);

            }

        }

        if (emitConflicts) {
            if (conflicts.isEmpty()) {
                logger.log(Level.INFO, "No conflicts for :{0}", sheetColumn.getName());
            } else {
                conflicts.values().forEach((conflict) -> {
                    for (JSONObject row : conflict) {
                        returnval.put(row);
                    }
                });
            }

        } else {
            for (int i = 0; i < master.length(); i++) {
                JSONObject val = master.getJSONObject(i);
                String rowKey = val.optString(Model.ExcelMetaData.__KEY.toString(), "");
                if (!dirtyChangeRow.containsKey(rowKey) && !conflicts.containsKey(rowKey)) {
                    returnval.put(val);
                }
            }

            dirtyChangeRow.values().forEach((changedRow) -> {
                Model.MergeMetaData op = Model.MergeMetaData.valueOf(changedRow.getString(Model.MergeMetaData.__OP_TYPE.toString()));
                String rowKey = changedRow.optString(Model.ExcelMetaData.__KEY.toString(), "");
                if (!op.equals(Model.MergeMetaData.__DELETE) && !conflicts.containsKey(rowKey)) {
                    returnval.put(changedRow);
                }
            });

            conflicts.values().forEach((conflict) -> {
                JSONObject conflictRow = conflict.get(conflict.size() - 1);
                returnval.put(conflictRow);
            });
        }
        return returnval;
    }

    public static boolean isEqualJSONObject(JSONObject src, JSONObject tgt, Class<? extends Enum> sheetColumn) {
        Enum[] colVal = sheetColumn.getEnumConstants();
        JSONObject formatingObject = new JSONObject();
        boolean retVal = true;
        for (Enum enum1 : colVal) {
            String key = enum1.toString();
            if (!src.optString(key, "").equals(tgt.optString(key, ""))) {
                formatingObject.put(key, tgt.optString(key, ""));
                retVal = false;
            }
        }
        if (!retVal) {
            src.put(Model.ExcelMetaData.__DIFF.toString(), formatingObject);
            tgt.put(Model.ExcelMetaData.__DIFF.toString(), formatingObject);
        }
        return retVal;
    }

    public static boolean threeWayMerge(JSONObject master, JSONObject row, JSONObject existingChange, Class<? extends Enum> sheetColumn) {
        Enum[] colVal = sheetColumn.getEnumConstants();
        JSONObject formatingObject = new JSONObject();
        boolean retVal = true, mergeRow=false;
        
        for (Enum enum1 : colVal) {
            String key = enum1.toString();
            String rowColVal = row.optString(key, "");
            String existingColVal = existingChange.optString(key, "");
            if (!rowColVal.equals(existingColVal)) {
                String masterColVal = master.optString(key, "");
                if (masterColVal.equals(existingColVal)) {
                    formatingObject.put(key, master.optString(key, ""));
                } else if (masterColVal.equals(rowColVal)) {
                    formatingObject.put(key, master.optString(key, ""));
                    
                    row.put(key, existingColVal);   
                    mergeRow = true;
                } else {
                    formatingObject.put(key, existingChange.optString(key, ""));
                    retVal = false;
                }
            }
        }
        row.put(Model.ExcelMetaData.__DIFF.toString(), formatingObject);
        //existingChange.put(Model.ExcelMetaData.__DIFF.toString(), formatingObject);
        if(mergeRow){
            String work2 = existingChange.optString(Model.ExcelMetaData.__WORKBOOK_NAME.toString(), "");
            String work2row = existingChange.optString(Model.ExcelMetaData.__ROWNUM.toString(), "");
            String rowComment = row.optString(Model.MergeMetaData.__ROWCOMENT.toString(),"");
            row.put(Model.MergeMetaData.__ROWCOMENT.toString(), rowComment+"\n"+work2+"["+work2row+"]");
        }
        return retVal;
    }
}
