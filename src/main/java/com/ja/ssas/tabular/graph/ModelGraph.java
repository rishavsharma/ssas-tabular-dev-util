/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.graph;

import com.ja.ssas.tabular.common.LogCountHandler;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.common.ModelUtil;
import com.ja.ssas.tabular.rename.TabularRename;
import java.util.Collection;

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
    private final JSONObject model;
    private final JSONArray tables;
    private JSONArray relationships;
    private String name;
    private final HashMap<String, JSONObject> allColumnMap;

    public ModelGraph(JSONObject model, String name) {
        super(CustomEdge.class);
        logger.log(Level.FINE, "New Model created {0}", name);
        tableMap = new HashMap<>();
        tablePhysicalMap = new HashMap<>();
        edgeMap = new HashMap<>();
        this.model = model;
        this.name = name;
        this.tables = new JSONArray();
        this.relationships = new JSONArray();
        this.allColumnMap = new HashMap<>();
        this.aliasMap = new HashMap<>();
        init();
    }

    private void init() {
        JSONObject inModel = model.getJSONObject("model");
        if (inModel.has("tables")) {
            addVertexArray(inModel.getJSONArray("tables"));
        }
        inModel.put("tables", this.tables);
        if (inModel.has("relationships")) {
            addExcelEdgeArray(inModel.getJSONArray("relationships"));
        }
        inModel.put("relationships", this.relationships);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public JSONArray getTables() {
        return this.tables;
    }

    public JSONObject getblankModel() {
        JSONObject blankModel = new JSONObject(model.toString());
        blankModel.getJSONObject("model").put("tables", new JSONArray());
        blankModel.getJSONObject("model").put("relationships", new JSONArray());
        return blankModel;
    }

    public ModelGraph getblankGraph() {
        JSONObject blankModel = new JSONObject(model.toString());
        blankModel.getJSONObject("model").put("tables", new JSONArray());
        blankModel.getJSONObject("model").put("relationships", new JSONArray());
        return new ModelGraph(blankModel, "");
    }

    public TableVertex getVertex(String vertex) {
        return tableMap.get(vertex);
    }

    public TableVertex createAlias(String fromVertex, String aliasVertex) {
        logger.log(Level.FINE, "Creating alias in Model {0}, From table {1} to new {2}", new Object[]{name, fromVertex, aliasVertex});
        TableVertex fromV = tableMap.get(fromVertex);
        if (fromV == null) {
            fromV = tablePhysicalMap.get(fromVertex);
            if (fromV == null) {
                String errorMsg = String.format("Table %1$s not found in model %2$s", fromVertex, name);
                logger.log(Level.SEVERE, errorMsg);
                return null;
            }
        }
        TableVertex createAlias = fromV.createAlias(aliasVertex, name);
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
            TableVertex tv = new TableVertex(inTables.getJSONObject(i), this.name);
            addVertex(tv);
        }
    }

    public String getModel() {
        return model.toString(2);
    }

    public void writer(String outFile) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(outFile)) {
            out.println(model.toString(2));
        }
    }

    public void scriptWriter(String outFile, String prefix) throws FileNotFoundException {
        String modelName = prefix + this.getName().replace("&", "and");
        model.put("name", modelName);
        String tmsl = "{\"createOrReplace\": { \"object\": {\"database\": \"" + modelName + "\"},\"database\":" + model.toString(2) + " }}";
        try (PrintWriter out = new PrintWriter(outFile)) {
            out.println(tmsl);
        }
    }

    @Override
    public boolean addVertex(TableVertex vertex) {
        TableVertex lkpVertex = tableMap.get(vertex.getLogicalName());
        logger.log(Level.FINE, "Adding  table {0} in model {1}", new Object[]{vertex.getLogicalName(), name});
        if (lkpVertex == null) {
            logger.log(Level.FINE, "Its a new table, creating new...");
            String logicalName = vertex.getLogicalName();
            String dbName = vertex.getDbName();
            if (!tablePhysicalMap.containsKey(dbName)) {
                tablePhysicalMap.put(dbName, vertex);
            }
            tableMap.put(logicalName, vertex);
            tables.put(vertex.getTable());
            vertex.setModelName(name);
            vertex.getAllColumnMap().forEach((name, column) -> {
                String key = logicalName + "[" + name + "]";
                allColumnMap.put(key, column);
                key = "'" + logicalName + "'[" + name + "]";
                allColumnMap.put(key, column);
            });
            super.addVertex(vertex);
            return true;
        } else {
            logger.log(Level.FINE, "Model has this table, returning...");
        }
        return false;

    }

    public void addAllColumnKeys(JSONArray derivedArray) {
        for (int j = 0; j < derivedArray.length(); j++) {
            JSONObject derived = derivedArray.getJSONObject(j);
            String tableName = derived.getString(Model.DerivedColumn.TABLE_NAME.toString());
            String columnName = derived.getString(Model.DerivedColumn.name.toString());
            String columnType = derived.getString(Model.DerivedColumn.COLUMN_TYPE.toString());
            if (Model.ColumnType.MEASURE.toString().equals(columnType)) {
                String measureKey = "[" + columnName + "]";
                allColumnMap.put(measureKey, derived);
            } else {
                String derivedKey = tableName + "[" + columnName + "]";
                allColumnMap.put(derivedKey, derived);
                String derivedKey1 = "'" + tableName + "'[" + columnName + "]";
                allColumnMap.put(derivedKey1, derived);
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
            errorInfo.append(String.format("Model %1$s : From table %2$s is not found in relationship %3$s", name, e.getFromTable(), e.toString())).append("\n");
            logError = true;
        }
        if (targetVertex == null) {
            errorInfo.append(String.format("Model %1$s : To table %2$s is not found in relationship %3$s", name, e.getToTable(), e.toString())).append("\n");
            logError = true;
        }
        if (logError) {
            logger.log(Level.SEVERE, errorInfo.toString());
            ModelUtil.putErrorInfo(e.getExcelRelation(), errorInfo.toString(), Model.ExcelMetaData.__ERROR.toString());
            return false;
        }
        if (!sourceVertex.getAllColumnMap().containsKey(e.getFrom())) {
            errorInfo.append(String.format("Model %1$s : From table column %2$s is not found in relationship %3$s", name, e.getFrom(), e.toString())).append("\n");
            logError = true;
        }

        if (!targetVertex.getAllColumnMap().containsKey(e.getTo())) {
            errorInfo.append(String.format("Model %1$s : To table column %2$s is not found in relationship %3$s", name, e.getTo(), e.toString())).append("\n");
            logError = true;
        }

        if (logError) {
            logger.log(Level.SEVERE, errorInfo.toString());
            ModelUtil.putErrorInfo(e.getExcelRelation(), errorInfo.toString(), Model.ExcelMetaData.__ERROR.toString());
            return false;
        }

        if (edgeMap.containsKey(e.toString())) {
            errorInfo.append(String.format("Already have relation in model %1$s : %2$s", name, e.toString())).append("\n");
            logger.log(Level.SEVERE, errorInfo.toString());
            ModelUtil.putErrorInfo(e.getExcelRelation(), errorInfo.toString(), Model.ExcelMetaData.__INFO.toString());
            return false;
        }

        this.getAllEdges(sourceVertex, targetVertex).forEach((edge) -> {
            if (edge.isActive()) {
                ModelUtil.putErrorInfo(e.getExcelRelation(), "Already have active relationship in model setting to inactive: " + e.toString(), Model.ExcelMetaData.__WARNING.toString());
                logger.log(Level.FINE, "Already have active relationship in model {0} : {1}", new Object[]{name, edge.toString()});
                e.setActive(false);
                logger.log(Level.FINE, "Setting to inactive active relationship in model {0} : {1}", new Object[]{name, e.toString()});
            }
        });
        super.addEdge(sourceVertex, targetVertex, e);
        edgeMap.put(e.toString(), e);
        this.relationships.put(e.getRelation());
        logger.log(Level.FINE, "Relationship added in model {0} : {1}", new Object[]{name, e.toString()});
        ModelUtil.putErrorInfo(e.getExcelRelation(), String.format("Relationship added : %1$s", e.toString()), Model.ExcelMetaData.__INFO.toString());
        return true;
    }

    public boolean addExcelEdge(JSONObject excelRow) {
        JSONObject relation = new JSONObject(excelRow, Model.Relation.getModelValueArray());
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

    public void renameModel(HashMap<String, String> tablecolExp) {
        logger.log(Level.INFO, "Starting renaming for for model {0}", name);
        new TabularRename(model,this.name).rename(tablecolExp);
        logger.log(Level.INFO, "Finished renaming for for model {0}", name);
    }

    public void RenameSchemax(HashMap<String, String> tablecolExp) {
        tableMap.values().forEach((table) -> {
            String tableName = table.getLogicalName();
            String key = tableName.toLowerCase() + ".schema";
            String renameSchema = tablecolExp.get(key);
            logger.log(Level.INFO, "Changing Schema name of table:{0} to:{1}", new Object[]{tableName, renameSchema});
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
        logger.log(Level.INFO, "Getting relationships for model {0}", name);
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject rel = relationships.getJSONObject(i);
            JSONObject retVal = new JSONObject(rel, Model.Relation.getStringValues());
            String fromTable = rel.getString(Model.Relation.fromTable.toString());
            String toTable = rel.getString(Model.Relation.toTable.toString());
            String fromPhysical = this.tableMap.get(fromTable).getDbName();
            String toPhysical = this.tableMap.get(toTable).getDbName();
            retVal.put(Model.Relation.FROM_PHYSICAL.toString(), fromPhysical);
            retVal.put(Model.Relation.TO_PHYSICAL.toString(), toPhysical);
            retArray.put(retVal);
        }
        return retArray;
    }

    public void setExcelRelationships(JSONArray excelRel) {
        logger.log(Level.INFO, "Setting relationships for model {0}", name);
        for (int i = 0; i < excelRel.length(); i++) {
            JSONObject excelRow = excelRel.getJSONObject(i);
            addExcelEdge(excelRow);
        }

        logger.log(Level.INFO, "Finished Setting relationships for model {0}", name);
    }

    public JSONArray getExcelHierarchiesArray() {
        logger.log(Level.INFO, "Getting hierarchies for model {0}", name);
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
        logger.log(Level.INFO, "Setting hierarchies for model {0}", name);
        excelTableMap.forEach((tableName, hierMap) -> {
            hierMap.forEach((hierName, levelsArray) -> {
                TableVertex vertex = tableMap.get(tableName);
                if (vertex != null) {
                    vertex.setExcelHierarchies(hierName, levelsArray);
                } else {
                    String errorMsg = String.format("Table %1$s not found for model %2$s", tableName, name);
                    logger.log(Level.SEVERE, errorMsg);
                    ModelUtil.putErrorInfo(levelsArray, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                }

            });

        });
        logger.log(Level.INFO, "Finished Setting hierarchies for model {0}", name);
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
                if (!allColumnMap.containsKey(columnName)) {
                    sb.append("All Column name ").append(columnName).append(" not found").append("\n");
                }
            });
            //check table exist
            ModelUtil.getTableFromExp(exp).forEach((tableNameExp) -> {
                if (!tableMap.containsKey(tableNameExp.replaceAll("'", ""))) {
                    sb.append("Table name ").append(tableNameExp).append(" not found").append("\n");
                }
            });
            if (sb.length() > 0 && !columType.equals(Model.ColumnType.MEASURE.toString())) {
                ModelUtil.putErrorInfo(derived, sb.toString(), Model.ExcelMetaData.__ERROR.toString());
                logger.log(Level.SEVERE, "Table {0} Error Creating derived column/measure {1} : {2}", new Object[]{tableName, columName, sb.toString()});
                return;
            }
            if (vertex.setExcelDerivedColumn(derived, exp)) {
                allColumnMap.put(tableName + "[" + columName + "]", model);
            }

        } else {
            logger.log(Level.SEVERE, "Table {0} not found in model {1}", new Object[]{tableName, getName()});
            ModelUtil.putErrorInfo(derived, "Table not found " + tableName, Model.ExcelMetaData.__ERROR.toString());
        }
    }

    public JSONArray getExcelLineage() {
        JSONArray retArray = new JSONArray();
        tableMap.forEach((logicalName, vertex) -> {
            vertex.getDerivedColumnMap().forEach((name, derivedC) -> {
                String exp = derivedC.getColumn().getString(Model.DerivedColumn.expression.toString());
                JSONObject retColumn = new JSONObject();
                retColumn.put(Model.LineageColumn.MODEL_NAME.toString(), this.getName());
                retColumn.put(Model.LineageColumn.TABLE_NAME.toString(), logicalName);
                retColumn.put(Model.LineageColumn.COLUMN_TYPE.toString(), derivedC.getType());
                retColumn.put(Model.LineageColumn.TERM_NAME.toString(), derivedC.getName());
                retColumn.put(Model.LineageColumn.FORMULA.toString(), exp);
                
                if (derivedC.getType().equals(Model.ColumnType.CALCULATED_COLUMN.toString())) {
                    ModelUtil.getColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn,Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        String dbTableName = s[0].replaceAll("'", "");
                        if(this.aliasMap.containsKey(dbTableName)){
                            dbTableName = this.aliasMap.get(dbTableName);
                        }
                        expCol.put(Model.LineageColumn.DB_TABLE.toString(), dbTableName);
                        expCol.put(Model.LineageColumn.DB_COLUMN.toString(), s[1]);
                        retArray.put(expCol);
                    });
                    ModelUtil.getLocalColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn,Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        expCol.put(Model.LineageColumn.DB_TABLE.toString(), vertex.getDbName());
                        expCol.put(Model.LineageColumn.DB_COLUMN.toString(), s[1]);
                        retArray.put(expCol);
                    });
                } else {
                    ModelUtil.getColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn,Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        String dbTableName = s[0].replaceAll("'", "");
                        if(this.aliasMap.containsKey(dbTableName)){
                            dbTableName = this.aliasMap.get(dbTableName);
                        }
                        expCol.put(Model.LineageColumn.DB_TABLE.toString(), dbTableName);
                        expCol.put(Model.LineageColumn.DB_COLUMN.toString(), s[1]);
                        retArray.put(expCol);
                    });
                    ModelUtil.getLocalColumnsFromExp(exp).forEach((col) -> {
                        JSONObject expCol = new JSONObject(retColumn,Model.LineageColumn.getStringValues());
                        String[] s = col.split("[\\[\\]]");
                        String term = s[1];
                        if(vertex.getBaseColumnMap().containsKey(term)){
                            expCol.put(Model.LineageColumn.DB_TABLE.toString(), vertex.getDbName());
                            expCol.put(Model.LineageColumn.DB_COLUMN.toString(), term);
                        }else{
                            expCol.put(Model.LineageColumn.REFERENCE.toString(), term);
                        }                        
                        retArray.put(expCol);
                    });
                }
            });
        });
        return retArray;
    }
}
