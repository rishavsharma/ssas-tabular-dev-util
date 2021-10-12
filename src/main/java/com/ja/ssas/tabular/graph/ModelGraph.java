/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.graph;

import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.common.ModelUtil;
import com.ja.ssas.tabular.common.R;
import com.ja.ssas.tabular.rename.TabularRename;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.graph.DirectedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Rishav
 */
public final class ModelGraph extends DirectedMultigraph<TableVertex, CustomEdge> {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("ModelGraph");
    private static final long serialVersionUID = 1L;
    private final HashMap<String, TableVertex> tableMap;
    private final HashMap<String, TableVertex> tablePhysicalMap;
    private final HashMap<String, CustomEdge> edgeMap;
    private final HashMap<String, String> aliasMap;
    private final HashMap<String, DerivedColumn> measureMap = new HashMap<>();
    private final JSONObject database;
    private final JSONObject model;
    private final JSONArray tables;
    private JSONArray relationships;
    private String modelName;
    private final HashMap<String, JSONObject> allModelColumnMap;

    public ModelGraph(JSONObject database, String name) {
        super(CustomEdge.class);
        logger.log(Level.FINE, "New Model created {0}", name);
        tableMap = new HashMap<>();
        tablePhysicalMap = new HashMap<>();
        edgeMap = new HashMap<>();
        this.database = database;
        this.modelName = name;
        this.tables = new JSONArray();
        this.relationships = new JSONArray();
        this.allModelColumnMap = new HashMap<>();
        this.aliasMap = new HashMap<>();
        this.model = database.getJSONObject("model");
        init();
    }

    private void init() {
        if (this.model.has("tables")) {
            addVertexArray(this.model.getJSONArray("tables"));
        }
        this.model.put("tables", this.tables);
        if (this.model.has("relationships")) {
            addModelEdgeArray(this.model.getJSONArray("relationships"));
        }
        this.model.put("relationships", this.relationships);
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setRelationships(JSONArray relationships) {
        this.removeAllEdges(edgeMap.values());
        edgeMap.clear();
        this.relationships = relationships;
        addExcelEdgeArray(relationships);
    }

    public JSONArray getRelationships() {
        return this.relationships;
    }

    public void setComments(String comments) {
        ModelUtil.setComments(this.model, comments);
    }

    public String getComments() {
        return ModelUtil.getComments(this.model);
    }

    public JSONArray getTables() {
        return this.tables;
    }

    public JSONObject getblankModel() {
        JSONObject blankModel = new JSONObject(database.toString());
        blankModel.getJSONObject("model").put("tables", new JSONArray());
        blankModel.getJSONObject("model").put("relationships", new JSONArray());
        return blankModel;
    }

    public ModelGraph getblankGraph() {
        JSONObject blankModel = new JSONObject(database.toString());
        blankModel.getJSONObject("model").put("tables", new JSONArray());
        blankModel.getJSONObject("model").put("relationships", new JSONArray());
        return new ModelGraph(blankModel, "");
    }

    public TableVertex getVertex(String vertex) {
        return tableMap.get(vertex);
    }

    public Collection<TableVertex> getAllVertices() {
        return this.tableMap.values();
    }

    public HashMap<String, DerivedColumn> getMeasureMap() {
        return measureMap;
    }

    public TableVertex createAlias(String fromVertex, String aliasVertex) {
        TableVertex fromV = tableMap.get(fromVertex);
        if (fromV == null) {
            fromV = tablePhysicalMap.get(fromVertex);
            if (fromV == null) {
                return null;
            }
        }
        TableVertex createAlias = fromV.createAlias(aliasVertex, modelName);
        addVertex(createAlias);
        this.aliasMap.put(createAlias.getLogicalName(), fromV.getDbName());
        return createAlias;
    }

    public void addAllVertex(Collection<TableVertex> vertices) {
        vertices.forEach((vertice) -> {
            addVertex(vertice.clone());
        });
    }

    public void addVertexArray(JSONArray inTables) {
        for (int i = 0; i < inTables.length(); i++) {
            TableVertex tv = new TableVertex(inTables.getJSONObject(i), this);
            addVertex(tv);
        }
    }

    public String getModel() {
        return database.toString(2);
    }

    public HashMap<String, JSONObject> getAllModelColumnMap() {
        return allModelColumnMap;
    }

    public void writer(String outFile) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(outFile)) {
            out.println(database.toString(2));
        }
    }

    public void scriptWriter(String outFile, String prefix) throws FileNotFoundException {
        String modelNameL = prefix + this.getModelName().replace("&", "and");
        database.put("name", modelNameL);
        if (database.has("id")) {
            database.remove("id");
        }
        String tmsl = "{\"createOrReplace\": { \"object\": {\"database\": \"" + modelNameL + "\"},\"database\":" + database.toString(2) + " }}";
        try (PrintWriter out = new PrintWriter(outFile)) {
            out.println(tmsl);
        }
    }

    @Override
    public boolean addVertex(TableVertex vertex) {
        TableVertex lkpVertex = tableMap.get(vertex.getLogicalName());
        logger.log(Level.FINE, "Adding  table {0} in model {1}", new Object[]{vertex.getLogicalName(), modelName});
        if (null == lkpVertex || R.OVERWRITE_TABLES) {            
            if(null != lkpVertex){
                logger.log(Level.WARNING, "Its a existing table, overwriting...:{0}", vertex.getLogicalName());
                removeVertex(vertex);                
            }else{
                logger.log(Level.FINE, "Its a new table, creating new...");
            }
            logger.log(Level.FINE, "Its a new table, creating new...");
            String logicalName = vertex.getLogicalName();
            String dbName = vertex.getDbName();
            if (!tablePhysicalMap.containsKey(dbName)) {
                tablePhysicalMap.put(dbName, vertex);
            }
            tableMap.put(logicalName, vertex);
            tables.put(vertex.getTable());
            vertex.setModelName(modelName);
            vertex.getAllTableColumnMap().forEach((name, column) -> {
                String key = logicalName + "[" + name + "]";
                allModelColumnMap.put(key.toLowerCase(), column);
                key = "'" + logicalName + "'[" + name + "]";
                allModelColumnMap.put(key.toLowerCase(), column);
            });
            vertex.setModelGraph(this);
            super.addVertex(vertex);
            return true;
        } else {
            logger.log(Level.FINE, "Model has this table, returning...");
        }
        return false;

    }

    @Override
    public boolean removeVertex(TableVertex vertex) {
        logger.log(Level.FINE, "Removing Table:"+vertex.getLogicalName());
        String logicalName = vertex.getLogicalName();
        String dbName = vertex.getDbName();
        if (tablePhysicalMap.containsKey(dbName)) {
            tablePhysicalMap.remove(dbName);
        }
        if (tableMap.containsKey(logicalName)) {
            tableMap.remove(logicalName);
        }
        
        allModelColumnMap.entrySet().removeIf(entry -> entry.getKey().replace("'", "").startsWith(logicalName.toLowerCase()));
        
        return super.removeVertex(vertex);
    }

    public void addAllColumnKeys(JSONArray derivedArray) {
        for (int j = 0; j < derivedArray.length(); j++) {
            JSONObject derived = derivedArray.getJSONObject(j);
            String tableName = derived.getString(Model.DerivedColumn.TABLE_NAME.toString());
            String columnName = derived.getString(Model.DerivedColumn.name.toString());
            String columnType = derived.getString(Model.DerivedColumn.COLUMN_TYPE.toString());
            if (Model.ColumnType.MEASURE.toString().equals(columnType)) {
                String measureKey = "[" + columnName + "]";
                String measureKey1 = tableName + "[" + columnName + "]";
                String measureKey2 = "'" + tableName + "'[" + columnName + "]";
                allModelColumnMap.put(measureKey.toLowerCase(), derived);
                allModelColumnMap.put(measureKey1.toLowerCase(), derived);
                allModelColumnMap.put(measureKey2.toLowerCase(), derived);
            } else {
                String derivedKey = tableName + "[" + columnName + "]";
                allModelColumnMap.put(derivedKey.toLowerCase(), derived);
                String derivedKey1 = "'" + tableName + "'[" + columnName + "]";
                allModelColumnMap.put(derivedKey1.toLowerCase(), derived);
                String derivedKey2 = tableName + "[" + columnName + "]";
                allModelColumnMap.put(derivedKey2.toLowerCase(), derived);
            }
            logger.log(Level.FINEST, "Setting derived column keys {0}:{1}", new Object[]{tableName, columnName});
        }
    }

    @Override
    public CustomEdge addEdge(TableVertex sourceVertex, TableVertex targetVertex) {
        throw new UnsupportedOperationException(
                "Use: addEdge(TableVertex sourceVertex, TableVertex targetVertex, CustomEdge e)");
    }

    @Override
    public boolean addEdge(TableVertex sourceVertex, TableVertex targetVertex, CustomEdge e) {
        StringBuilder errorInfo = new StringBuilder();
        boolean logError = false;
        if (sourceVertex == null) {
            errorInfo.append(String.format("Model %1$s : From table %2$s is not found in relationship %3$s", modelName, e.getFromTable(), e.toString())).append("\n");
            logError = true;
        }
        if (targetVertex == null) {
            errorInfo.append(String.format("Model %1$s : To table %2$s is not found in relationship %3$s", modelName, e.getToTable(), e.toString())).append("\n");
            logError = true;
        }
        if (logError) {
            logger.log(Level.SEVERE, errorInfo.toString());
            ModelUtil.putErrorInfo(e.getExcelRelation(), errorInfo.toString(), Model.ExcelMetaData.__ERROR.toString());
            return false;
        }
        if (!sourceVertex.getAllTableColumnMap().containsKey(e.getFrom().toLowerCase())) {
            errorInfo.append(String.format("Model %1$s : From table column %2$s is not found in relationship %3$s", modelName, e.getFrom(), e.toString())).append("\n");
            logError = true;
        }

        if (!targetVertex.getAllTableColumnMap().containsKey(e.getTo().toLowerCase())) {
            errorInfo.append(String.format("Model %1$s : To table column %2$s is not found in relationship %3$s", modelName, e.getTo(), e.toString())).append("\n");
            logError = true;
        }

        if (logError) {
            logger.log(Level.SEVERE, errorInfo.toString());
            ModelUtil.putErrorInfo(e.getExcelRelation(), errorInfo.toString(), Model.ExcelMetaData.__ERROR.toString());
            return false;
        }

        if (edgeMap.containsKey(e.toString())) {
            errorInfo.append(String.format("Already have relation in model %1$s : %2$s", modelName, e.toString())).append("\n");
            logger.log(Level.SEVERE, errorInfo.toString());
            ModelUtil.putErrorInfo(e.getExcelRelation(), errorInfo.toString(), Model.ExcelMetaData.__ERROR.toString());
            return false;
        }

        this.getAllEdges(sourceVertex, targetVertex).forEach((edge) -> {
            if (edge.isActive()) {
                ModelUtil.putErrorInfo(e.getExcelRelation(), "Already have active relationship in model setting to inactive: " + e.toString(), Model.ExcelMetaData.__WARNING.toString());
                logger.log(Level.FINE, "Already have active relationship in model {0} : {1}", new Object[]{modelName, edge.toString()});
                e.setActive(false);
                logger.log(Level.FINE, "Setting to inactive active relationship in model {0} : {1}", new Object[]{modelName, e.toString()});
            }
        });
        super.addEdge(sourceVertex, targetVertex, e);
        edgeMap.put(e.toString(), e);
        this.relationships.put(e.getRelation());
        logger.log(Level.FINE, "Relationship added in model {0} : {1}", new Object[]{modelName, e.toString()});
        ModelUtil.putErrorInfo(e.getExcelRelation(), String.format("Relationship added : %1$s", e.toString()), Model.ExcelMetaData.__INFO.toString());
        return true;
    }

    public boolean addExcelEdge(JSONObject excelRow) {
        JSONObject relation = new JSONObject(excelRow, Model.Relation.getModelValueArray());
        if (R.COMMENTS) {
            ModelUtil.setComments(excelRow, relation);
        }
        TableVertex fromTable = tableMap.get(relation.getString(Model.Relation.fromTable.toString()));
        TableVertex toTable = tableMap.get(relation.getString(Model.Relation.toTable.toString()));
        CustomEdge cedg = new CustomEdge(relation);
        cedg.setExcelRelation(excelRow);
        return addEdge(fromTable, toTable, cedg);
    }

    public void addExcelEdgeArray(JSONArray relationships) {
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject relation = relationships.getJSONObject(i);
            addExcelEdge(relation);
        }
    }

    public boolean addModelEdge(JSONObject relation) {
        TableVertex fromTable = tableMap.get(relation.getString(Model.Relation.fromTable.toString()));
        TableVertex toTable = tableMap.get(relation.getString(Model.Relation.toTable.toString()));
        CustomEdge cedg = new CustomEdge(relation);
        cedg.setExcelRelation(relation);
        return addEdge(fromTable, toTable, cedg);
    }

    public void addModelEdgeArray(JSONArray relationships) {
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject relation = relationships.getJSONObject(i);
            addModelEdge(relation);
        }
    }

    public void renameModel(HashMap<String, String> tablecolExp, boolean doRename) {
        logger.log(Level.FINE, "Starting renaming for for model {0}", modelName);
        new TabularRename(database, this.modelName, doRename).rename(tablecolExp);
        logger.log(Level.FINE, "Finished renaming for for model {0}", modelName);
    }

    public void RenameSchemax(HashMap<String, String> tablecolExp) {
        tableMap.values().forEach((table) -> {
            String tableName = table.getLogicalName();
            String key = tableName.toLowerCase() + ".schema";
            String renameSchema = tablecolExp.get(key);
            logger.log(Level.FINE, "Changing Schema name of table:{0} to:{1}", new Object[]{tableName, renameSchema});
            if (renameSchema != null) {
                table.setSchema(tablecolExp);
            } else {
                logger.log(Level.WARNING, "Rename schema name is null for table:{0} to:{1}",
                        new Object[]{tableName, renameSchema});
            }
        });
    }

    public JSONArray getExcelRelationshipsArray() {
        JSONArray retArray = new JSONArray();
        logger.log(Level.FINE, "Getting relationships for model {0}", modelName);
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject rel = relationships.getJSONObject(i);
            JSONObject retVal = new JSONObject(rel, Model.Relation.getStringValues());
            String fromTable = rel.getString(Model.Relation.fromTable.toString());
            String toTable = rel.getString(Model.Relation.toTable.toString());
            String fromPhysical = this.tableMap.get(fromTable).getDbName();
            String toPhysical = this.tableMap.get(toTable).getDbName();
            retVal.put(Model.Relation.FROM_PHYSICAL.toString(), fromPhysical);
            retVal.put(Model.Relation.TO_PHYSICAL.toString(), toPhysical);
            if (R.COMMENTS) {
                ModelUtil.setCommentsAnnotaion(rel, retVal);
            }
            retArray.put(retVal);
        }
        return retArray;
    }

    public void setExcelRelationships(JSONArray excelRel) {
        logger.log(Level.FINE, "Setting relationships for model {0}", modelName);
        for (int i = 0; i < excelRel.length(); i++) {
            JSONObject excelRow = excelRel.getJSONObject(i);
            addExcelEdge(excelRow);
        }

        logger.log(Level.FINE, "Finished Setting relationships for model {0}", modelName);
    }

    public JSONArray getExcelHierarchiesArray() {
        logger.log(Level.FINE, "Getting hierarchies for model {0}", modelName);
        JSONArray retArray = new JSONArray();
        tableMap.values().forEach((table) -> {
            JSONArray hierarchies = table.getExcelHierarchies();
            for (int i = 0; i < hierarchies.length(); i++) {
                retArray.put(hierarchies.get(i));
            }
        });
        return retArray;
    }

    public void setExcelHierarchies(HashMap<String, HashMap<String, JSONArray>> excelTableMap) {
        logger.log(Level.FINE, "Setting hierarchies for model {0}", modelName);
        excelTableMap.forEach((tableName, hierMap) -> {
            hierMap.forEach((hierName, levelsArray) -> {
                TableVertex vertex = tableMap.get(tableName);
                if (vertex != null) {
                    vertex.setExcelHierarchies(hierName, levelsArray);
                } else {
                    String errorMsg = String.format("Table %1$s not found for model %2$s", tableName, modelName);
                    logger.log(Level.SEVERE, errorMsg);
                    ModelUtil.putErrorInfo(levelsArray, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                }

            });

        });
        logger.log(Level.FINE, "Finished Setting hierarchies for model {0}", modelName);
    }

    public void setExcelDerivedColumn(String tableName, JSONObject derived) {
        TableVertex vertex = getVertex(tableName);
        if (vertex != null) {
            String exp = derived.getString(Model.DerivedColumn.expression.toString());
            String columName = derived.getString(Model.DerivedColumn.name.toString());
            String columType = derived.getString(Model.DerivedColumn.COLUMN_TYPE.toString());
            if (exp == null) {
                ModelUtil.putErrorInfo(derived, "Exp is null in excel", Model.ExcelMetaData.__ERROR.toString());
                return;
            }
            //check column exist
            StringBuilder sb = new StringBuilder();
            ModelUtil.getColumnsFromExp(exp).forEach((columnName) -> {
                if (!allModelColumnMap.containsKey(columnName.toLowerCase())) {
                    sb.append("All Column name ").append(columnName).append(" not found").append("\n");
                }
            });
            //check table exist
            ModelUtil.getTableFromExp(exp).forEach((tableNameExp) -> {
                if (!tableMap.containsKey(tableNameExp.replaceAll("'", ""))) {
                    sb.append("Table name ").append(tableNameExp).append(" not found").append("\n");
                }
            });

            if (sb.length() > 0 && columType.equals(Model.ColumnType.MEASURE.toString())) {
                ModelUtil.putErrorInfo(derived, sb.toString(), Model.ExcelMetaData.__ERROR.toString());
                logger.log(Level.SEVERE, "Table {0} Error Creating derived measure {1} : {2}", new Object[]{tableName, columName, sb.toString()});
            }
            if (sb.length() > 0 && !columType.equals(Model.ColumnType.MEASURE.toString())) {
                ModelUtil.putErrorInfo(derived, sb.toString(), Model.ExcelMetaData.__ERROR.toString());
                logger.log(Level.SEVERE, "Table {0} Error Creating derived column {1} : {2}", new Object[]{tableName, columName, sb.toString()});
                return;
            }
            vertex.setExcelDerivedColumn(derived, exp);

        } else {
            logger.log(Level.SEVERE, "Table {0} not found in model {1}", new Object[]{tableName, getModelName()});
            ModelUtil.putErrorInfo(derived, "Table not found " + tableName, Model.ExcelMetaData.__ERROR.toString());
        }
    }

    public JSONArray getExcelLineage() {
        JSONArray retArray = new JSONArray();
        tableMap.forEach((logicalName, vertex) -> {
            vertex.getDerivedColumnMap().forEach((key, derivedC) -> {
                //String exp = derivedC.getColumn().getString(Model.DerivedColumn.expression.toString());
                String exp = derivedC.getExpression();
                JSONObject retColumn = new JSONObject();
                retColumn.put(Model.LineageColumn.MODEL_NAME.toString(), this.getModelName());
                retColumn.put(Model.LineageColumn.TABLE_NAME.toString(), logicalName);
                retColumn.put(Model.LineageColumn.COLUMN_TYPE.toString(), derivedC.getType());
                retColumn.put(Model.LineageColumn.TERM_NAME.toString(), derivedC.getName());
                retColumn.put(Model.LineageColumn.FORMULA.toString(), exp);

                if (derivedC.getType().equals(Model.ColumnType.CALCULATED_COLUMN.toString())) {
                    ModelUtil.getColumnsFromExpDash(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn, Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        String dbTableName = s[0].replaceAll("'", "");
                        if (this.aliasMap.containsKey(dbTableName)) {
                            dbTableName = this.aliasMap.get(dbTableName);
                        }
                        expCol.put(Model.LineageColumn.SSAS_TABLE.toString(), dbTableName);
                        expCol.put(Model.LineageColumn.SSAS_COLUMN.toString(), s[1]);
                        retArray.put(expCol);
                    });
                    ModelUtil.getLocalColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn, Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        expCol.put(Model.LineageColumn.SSAS_TABLE.toString(), vertex.getLogicalName());
                        expCol.put(Model.LineageColumn.SSAS_COLUMN.toString(), s[1]);
                        retArray.put(expCol);
                    });
                } else {
                    ModelUtil.getColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn, Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        String dbTableName = s[0].replaceAll("'", "");
                        if (this.aliasMap.containsKey(dbTableName)) {
                            dbTableName = this.aliasMap.get(dbTableName);
                        }
                        expCol.put(Model.LineageColumn.SSAS_TABLE.toString(), dbTableName);
                        expCol.put(Model.LineageColumn.SSAS_COLUMN.toString(), s[1]);
                        retArray.put(expCol);
                    });
                    ModelUtil.getLocalColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn, Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        String term = s[1];
                        if (vertex.getBaseColumnMap().containsKey(term.toLowerCase())) {
                            expCol.put(Model.LineageColumn.SSAS_TABLE.toString(), vertex.getLogicalName());
                            expCol.put(Model.LineageColumn.SSAS_COLUMN.toString(), term);
                        } else {
                            //expCol.put(Model.LineageColumn.REFERENCE.toString(), term);

                            if (vertex.getAllTableColumnMap().containsKey(term.toLowerCase())) {
                                expCol.put(Model.LineageColumn.SSAS_TABLE.toString(), vertex.getLogicalName());
                                expCol.put(Model.LineageColumn.SSAS_COLUMN.toString(), term);
                            } else if (measureMap.containsKey(term.toLowerCase())) {
                                DerivedColumn measure = measureMap.get(term.toLowerCase());
                                expCol.put(Model.LineageColumn.SSAS_TABLE.toString(), measure.getTableName());
                                expCol.put(Model.LineageColumn.SSAS_COLUMN.toString(), term);
                            } else {
                                logger.log(Level.SEVERE, "Measure [{0}] not found in model [{1}]", new Object[]{term, getModelName()});
                            }
                        }
                        retArray.put(expCol);
                    });
                }
            });
        });
        return retArray;
    }

    public void addTablesFromModel(ModelGraph from) {
        this.addAllVertex(from.tableMap.values());
    }
}
