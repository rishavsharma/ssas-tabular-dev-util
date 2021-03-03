/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

import com.ja.ssas.tabular.common.ExcelReadUtil;
import com.ja.ssas.tabular.common.ExcelWriteUtil;
import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.common.ModelUtil;
import com.ja.ssas.tabular.graph.ModelsWrapper;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Rishav
 */
public class ExcelReadHelper {
    
    static final Logger logger = LogCountHandler.getInstance().getLogger("ExcelReadHelper");
    
    private JSONArray _MODEL_NAMES = new JSONArray();
    private JSONArray _ALIAS_TABLES = new JSONArray();
    private JSONArray _DERIVED_COLUMNS = new JSONArray();
    private JSONArray _RENAME_TABLES = new JSONArray();
    private JSONArray _RENAME_COLUMNS = new JSONArray();
    private JSONArray _HIERARCHIES = new JSONArray();
    private JSONArray _RELATIONSHIPS = new JSONArray();
    private JSONArray _TABLE_SCHEMAS = new JSONArray();
    private final HashMap<String, JSONArray> relationMap = new HashMap<>();
    private final HashMap<String, JSONArray> sheetMap = new HashMap<>();
    private final HashMap<String, JSONArray> derivedMap = new HashMap<>();
    private final HashMap<String, HashMap<String, HashMap<String, JSONArray>>> hierarchyMap = new HashMap<>();
    private final CommandLine cmd;
    private boolean hasMultipleRelSheet = false;
    private String fileName;
    
    public ExcelReadHelper(File loc, CommandLine cmd, boolean readKey) {
        this.cmd = cmd;
        ExcelReadUtil excelRead = new ExcelReadUtil(loc, readKey);
        if (cmd.hasOption("comments")) {
            excelRead.setReadComment(true);
        }
        this.fileName = loc.getName();
        _MODEL_NAMES = excelRead.getSheetData(Model.ExcelSheets._MODEL_NAMES.toString(), Model.Models.class);
        sheetMap.put(Model.ExcelSheets._MODEL_NAMES.toString(), _MODEL_NAMES);
        _RENAME_TABLES = excelRead.getSheetData(Model.ExcelSheets._RENAME_TABLES.toString(), Model.RenameTable.class);
        sheetMap.put(Model.ExcelSheets._RENAME_TABLES.toString(), _RENAME_TABLES);
        
        if (cmd.hasOption("alias")) {
            _ALIAS_TABLES = excelRead.getSheetData(Model.ExcelSheets._ALIAS_TABLES.toString(), Model.AliasTable.class);
            sheetMap.put(Model.ExcelSheets._ALIAS_TABLES.toString(), _ALIAS_TABLES);
        }
        if (cmd.hasOption("schema")) {
            _TABLE_SCHEMAS = excelRead.getSheetData(Model.ExcelSheets._TABLE_SCHEMAS.toString(), Model.RenameSchema.class);
        }
        if (cmd.hasOption("hierarchy")) {
            _HIERARCHIES = excelRead.getSheetData(Model.ExcelSheets._HIERARCHIES.toString(), Model.HierarchyLevel.class);
            sheetMap.put(Model.ExcelSheets._HIERARCHIES.toString(), _HIERARCHIES);
        }
        if (cmd.hasOption("derived")) {
            _DERIVED_COLUMNS = excelRead.getSheetData(Model.ExcelSheets._DERIVED_COLUMNS.toString(), Model.DerivedColumn.class);
            sheetMap.put(Model.ExcelSheets._DERIVED_COLUMNS.toString(), _DERIVED_COLUMNS);
            for (int i = 0; i < _DERIVED_COLUMNS.length(); i++) {
                JSONObject derivedColumn = _DERIVED_COLUMNS.getJSONObject(i);
                String modelName = derivedColumn.getString(Model.DerivedColumn.MODEL_NAME.toString());
                if (derivedMap.containsKey(modelName)) {
                    derivedMap.get(modelName).put(derivedColumn);
                } else {
                    JSONArray ret = new JSONArray();
                    ret.put(derivedColumn);
                    derivedMap.put(modelName, ret);
                }
            }
        }
        if (cmd.hasOption("relation")) {
            if (excelRead.haveSheet(Model.ExcelSheets._RELATIONSHIPS.toString())) {
                _RELATIONSHIPS = excelRead.getSheetData(Model.ExcelSheets._RELATIONSHIPS.toString(), Model.AllRelation.class);
                sheetMap.put(Model.ExcelSheets._RELATIONSHIPS.toString(), _RELATIONSHIPS);
                for (int i = 0; i < _RELATIONSHIPS.length(); i++) {
                    JSONObject relation = _RELATIONSHIPS.getJSONObject(i);
                    String modelName = relation.getString(Model.AllRelation.MODEL_NAME.toString());
                    JSONArray relArray = relationMap.get(modelName);
                    if (relArray == null) {
                        relArray = new JSONArray();
                        relationMap.put(modelName, relArray);
                    }
                    relArray.put(relation);
                }
            } else {
                hasMultipleRelSheet = true;
                for (int i = 0; i < _MODEL_NAMES.length(); i++) {
                    JSONObject model = _MODEL_NAMES.getJSONObject(i);
                    String modelName = model.getString(Model.Models.MODEL_NAME.toString());
                    JSONArray relations = excelRead.getSheetData(modelName, Model.Relation.class);
                    relationMap.put(modelName, relations);
                }
            }
            
        }
        //if (cmd.hasOption("rename")) {
        _RENAME_COLUMNS = excelRead.getSheetData(Model.ExcelSheets._RENAME_COLUMNS.toString(), Model.RenameColumn.class);
        sheetMap.put(Model.ExcelSheets._RENAME_COLUMNS.toString(), _RENAME_COLUMNS);
        //}

        if (cmd.hasOption("schema")) {
            _TABLE_SCHEMAS = excelRead.getSheetData(Model.ExcelSheets._TABLE_SCHEMAS.toString(), Model.RenameColumn.class);
        }
    }
    
    public ExcelReadHelper(File loc, CommandLine cmd) {
        this(loc, cmd, false);
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public JSONArray getModelNames() {
        return _MODEL_NAMES;
    }
    
    public JSONArray getAliasTables() {
        return _ALIAS_TABLES;
    }
    
    public HashMap<String, JSONArray> getDerivedColumns() {
        return derivedMap;
    }
    
    public HashMap<String, JSONArray> getSheetMap() {
        return sheetMap;
    }
    
    public JSONArray getRenameTable() {
        return _RENAME_TABLES;
    }
    
    public JSONArray getRenameColumn() {
        return _RENAME_COLUMNS;
    }
    
    public JSONArray getTableSchema() {
        return _TABLE_SCHEMAS;
    }
    
    public JSONArray setTableSchema(JSONArray schemaArray) {
        return this._TABLE_SCHEMAS = schemaArray;
    }
    
    public JSONArray getRelationForModel(String model) {
        return relationMap.get(model);
    }
    
    public HashMap<String, JSONArray> getRelationForAllModel() {
        return relationMap;
    }
    
    public HashMap<String, HashMap<String, HashMap<String, JSONArray>>> getHierarchiesForAllModel() {
        for (int i = 0; i < _HIERARCHIES.length(); i++) {
            JSONObject row = _HIERARCHIES.getJSONObject(i);
            String hierName = row.getString(Model.HierarchyLevel.HIERARCHY_NAME.toString());
            String modelName = row.getString(Model.HierarchyLevel.MODEL_NAME.toString());
            String tableName = row.getString(Model.HierarchyLevel.TABLE_NAME.toString());
            HashMap<String, HashMap<String, JSONArray>> modelTables = hierarchyMap.get(modelName);
            if (modelTables == null) {
                modelTables = new HashMap<>();
                hierarchyMap.put(modelName, modelTables);
            }
            HashMap<String, JSONArray> hierarcyMap = modelTables.get(tableName);
            if (hierarcyMap == null) {
                hierarcyMap = new HashMap<>();
                modelTables.put(tableName, hierarcyMap);
            }
            JSONArray levels = hierarcyMap.get(hierName);
            if (levels == null) {
                levels = new JSONArray();
                hierarcyMap.put(hierName, levels);
            }
            //JSONObject retlevel = new JSONObject(row, ModelEnums.Hierarchy.getStringValues());
            levels.put(row);
        }
        return hierarchyMap;
    }
    
    public void writeStatusExcel(File loc) {
        if (cmd.hasOption("excel")) {
            String inputExcelFile = cmd.getOptionValue("excel");
            File excelFile = new File(inputExcelFile);
            if (!excelFile.exists()) {
                logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", excelFile.getAbsolutePath());
            }
            String parent = excelFile.getParent();
            if (parent == null) {
                parent = ".";
            }
            
            String excelName = loc.getAbsolutePath() + File.separator + "STATUS_" + excelFile.getName();
            ExcelWriteUtil r = new ExcelWriteUtil(excelName);
            int excelSheetPlace = 0;
            r.writeSheet(Model.ExcelSheets._MODEL_NAMES.toString(), excelSheetPlace++, _MODEL_NAMES, Model.Models.class);
            r.writeSheet(Model.ExcelSheets._ALIAS_TABLES.toString(), excelSheetPlace++, _ALIAS_TABLES, Model.AliasTable.class);
            r.writeSheet(Model.ExcelSheets._HIERARCHIES.toString(), excelSheetPlace++, _HIERARCHIES, Model.HierarchyLevel.class);
            r.writeSheet(Model.ExcelSheets._DERIVED_COLUMNS.toString(), excelSheetPlace++, _DERIVED_COLUMNS, Model.DerivedColumn.class);
            r.writeSheet(Model.ExcelSheets._RENAME_TABLES.toString(), excelSheetPlace++, _RENAME_TABLES, Model.RenameTable.class);
            
            if (_RENAME_COLUMNS.isEmpty()) {
                boolean onlyColumn = true;
                if (cmd.hasOption("lineage")) {
                    onlyColumn = false;
                }
                _RENAME_COLUMNS = ModelsWrapper.getInstance().getExcelRenameArray(onlyColumn);
            }
            r.writeSheet(Model.ExcelSheets._RENAME_COLUMNS.toString(), excelSheetPlace++, _RENAME_COLUMNS, Model.RenameColumn.class);
            
            if (cmd.hasOption("multiple")) {
                for (Map.Entry<String, JSONArray> entry : relationMap.entrySet()) {
                    String modelName = entry.getKey();
                    JSONArray sheetData = entry.getValue();
                    r.writeSheet(modelName, excelSheetPlace++, sheetData, Model.Relation.class);
                }
                
            } else {
                if (_RELATIONSHIPS.isEmpty()) {
                    relationMap.entrySet().forEach((entry) -> {
                        String modelName = entry.getKey();
                        JSONArray sheetData = entry.getValue();
                        for (int i = 0; i < sheetData.length(); i++) {
                            JSONObject rel = sheetData.getJSONObject(i);
                            rel.put(Model.AllRelation.MODEL_NAME.toString(), modelName);
                            _RELATIONSHIPS.put(rel);
                        }
                    });
                }
                r.writeSheet(Model.ExcelSheets._RELATIONSHIPS.toString(), excelSheetPlace++, _RELATIONSHIPS, Model.AllRelation.class);
                
            }
            //r.writeSheet(Model.ExcelSheets._TABLE_SCHEMAS.toString(), excelSheetPlace++, this._TABLE_SCHEMAS, Model.TableSchema.class);
            r.close();
        }
    }
    
    public void writeLineageExcel(File locFolder, JSONArray lineageArray) {
        if (cmd.hasOption("excel")) {
            String inputExcelFile = cmd.getOptionValue("excel");
            File excelFile = new File(inputExcelFile);
            if (!excelFile.exists()) {
                logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", excelFile.getAbsolutePath());
            }
            String xlsName = excelFile.getName();
            if (!xlsName.endsWith(".xls")) {
                xlsName = xlsName + ".xls";
            }
            String excelName = locFolder.getAbsolutePath() + File.separator + "LINEAGE_" + xlsName;
            ExcelWriteUtil r = new ExcelWriteUtil(excelName);
            int excelSheetPlace = 0;
            r.writeSheet("Lineage", excelSheetPlace, lineageArray, Model.LineageColumn.class);
            r.close();
        }
    }
    
    public void writeMergedExcel() {
        if (cmd.hasOption("excel")) {
            String inputExcelFile = cmd.getOptionValue("excel");
            File excelFile = new File(inputExcelFile);
            if (!excelFile.exists()) {
                logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", excelFile.getAbsolutePath());
            }
            String parent = excelFile.getParent();
            if (parent == null) {
                parent = ".";
            }
            
            String excelName = parent + File.separator + "MERGED_" + excelFile.getName();
            ExcelWriteUtil r = new ExcelWriteUtil(excelName);
            HashMap<String, String> modelRename = new HashMap<>();
            Set<String> newModels = new HashSet<>();
            for (int i = 0; i < _MODEL_NAMES.length(); i++) {
                JSONObject row = _MODEL_NAMES.getJSONObject(i);
                String fromModel = row.getString(Model.Models.MODEL_NAME.toString());
                String toModel = row.optString(Model.Models.MODEL_MERGE_NAME.toString(), fromModel);
                
                newModels.add(toModel);
                modelRename.put(fromModel, toModel);
            }
            JSONArray _MODEL_NAMES_NEW = new JSONArray();
            for (String model : newModels) {
                JSONObject m = new JSONObject();
                m.put(Model.Models.MODEL_NAME.toString(), model);
                _MODEL_NAMES_NEW.put(m);
            }
            int excelSheetPlace = 0;
            r.writeSheet(Model.ExcelSheets._MODEL_NAMES.toString(), excelSheetPlace++, _MODEL_NAMES_NEW, Model.Models.class);
            r.writeSheet(Model.ExcelSheets._ALIAS_TABLES.toString(), excelSheetPlace++, ModelUtil.putKeyValue(_ALIAS_TABLES, Model.Models.MODEL_NAME.toString(), modelRename), Model.AliasTable.class);
            r.writeSheet(Model.ExcelSheets._HIERARCHIES.toString(), excelSheetPlace++, ModelUtil.putKeyValue(_HIERARCHIES, Model.Models.MODEL_NAME.toString(), modelRename), Model.HierarchyLevel.class);
            r.writeSheet(Model.ExcelSheets._DERIVED_COLUMNS.toString(), excelSheetPlace++, ModelUtil.putKeyValue(_DERIVED_COLUMNS, Model.Models.MODEL_NAME.toString(), modelRename), Model.DerivedColumn.class);
            r.writeSheet(Model.ExcelSheets._RENAME_TABLES.toString(), excelSheetPlace++, ModelUtil.putKeyValue(_RENAME_TABLES, Model.Models.MODEL_NAME.toString(), modelRename), Model.RenameTable.class);
            r.writeSheet(Model.ExcelSheets._RENAME_COLUMNS.toString(), excelSheetPlace++, ModelUtil.putKeyValue(_RENAME_COLUMNS, Model.Models.MODEL_NAME.toString(), modelRename), Model.RenameColumn.class);
            r.writeSheet(Model.ExcelSheets._RELATIONSHIPS.toString(), excelSheetPlace++, ModelUtil.putKeyValue(_RELATIONSHIPS, Model.Models.MODEL_NAME.toString(), modelRename), Model.AllRelation.class);
            
            r.close();
        }
    }
    
}
