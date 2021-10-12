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
import com.ja.ssas.tabular.rename.SchemaChange;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class TableVertex {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("TableVertex");
    private String dbName;
    private String logicalName;
    private JSONObject table;
    private String modelName;
    private final String schemaName;
    private Set<String> schemas;
    private final HashMap<String, DerivedColumn> derivedColumnMap;
    private final HashMap<String, JSONObject> allTableColumnMap;
    private final HashMap<String, JSONObject> allHierarchyMap;
    private final HashMap<String, JSONObject> baseColumn;
    private JSONArray columns;
    private JSONArray measures;
    private JSONArray hierarchies;
    private ModelGraph modelGraph;

    public TableVertex(JSONObject table, ModelGraph modelGraph) {
        this.table = table;
        this.modelGraph = modelGraph;
        this.logicalName = table.getString("name");
        this.dbName = ModelUtil.getAnnotation(table, Model.TableAnnotations._TM_ExtProp_DbTableName.toString(), this.logicalName);
        this.modelName = modelGraph.getModelName();
        this.schemaName = ModelUtil.getAnnotation(table, Model.TableAnnotations._TM_ExtProp_DbSchemaName.toString(), "");
        this.derivedColumnMap = new HashMap<>();
        this.allTableColumnMap = new HashMap<>();
        this.allHierarchyMap = new HashMap<>();
        this.baseColumn = new HashMap<>();
        initDerivedColumn();
    }

    private void initDerivedColumn() {
        if (table.has("columns")) {
            columns = table.getJSONArray("columns");
            for (int j = 0; j < columns.length(); j++) {
                JSONObject column = columns.getJSONObject(j);
                String columnName = column.getString("name");
                if (column.has("type") && column.getString("type").equalsIgnoreCase("calculated")) {
                    derivedColumnMap.put(columnName.toLowerCase(), new DerivedColumn(column, Model.ColumnType.CALCULATED_COLUMN.toString(), columnName, this.logicalName));
                    allTableColumnMap.put(columnName.toLowerCase(), column);
                } else {
                    baseColumn.put(columnName.toLowerCase(), column);
                }
                allTableColumnMap.put(columnName.toLowerCase(), column);
            }
        } else {
            this.columns = new JSONArray();
        }

        if (table.has("measures")) {
            measures = table.getJSONArray("measures");
            for (int j = 0; j < measures.length(); j++) {
                JSONObject measure = measures.getJSONObject(j);
                String columnName = measure.getString("name");
                DerivedColumn derivedMeasure = new DerivedColumn(measure, Model.ColumnType.MEASURE.toString(), columnName, this.logicalName);
                derivedColumnMap.put(columnName.toLowerCase(), derivedMeasure);
                this.modelGraph.getMeasureMap().put(columnName.toLowerCase(), derivedMeasure);
                allTableColumnMap.put(columnName.toLowerCase(), measure);
            }
        } else {
            this.measures = new JSONArray();
            table.put("measures", this.measures);
        }

        if (this.table.has("hierarchies")) {
            hierarchies = this.table.getJSONArray("hierarchies");
            for (int j = 0; j < hierarchies.length(); j++) {
                JSONObject hierarchy = hierarchies.getJSONObject(j);
                String hierName = hierarchy.getString("name");
                this.allHierarchyMap.put(hierName.toLowerCase(), hierarchy);
            }
        } else {
            this.hierarchies = new JSONArray();
            table.put("hierarchies", this.hierarchies);
        }
    }

    @Override
    public TableVertex clone() {
        return new TableVertex(new JSONObject(table.toString()), modelGraph);
    }

    public TableVertex createAlias(String name, String model) {
        if (model == null) {
            model = this.modelName;
        }

        TableVertex alias = this.clone();
        alias.setLogicalName(name);
        alias.setModelName(model);
        return alias;
    }

    public JSONObject getTable() {
        return this.table;
    }

    public void setTable(JSONObject table) {
        this.table = table;
    }

    public HashMap<String, JSONObject> getAllTableColumnMap() {
        return this.allTableColumnMap;
    }

    public HashMap<String, JSONObject> getBaseColumnMap() {
        return this.baseColumn;
    }

    public HashMap<String, DerivedColumn> getDerivedColumnMap() {
        return this.derivedColumnMap;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getNumberOfColumn() {
        if (this.table.has("columns")) {
            return this.table.getJSONArray("columns").length();
        }
        return 0;
    }

    public ModelGraph getModelGraph() {
        return modelGraph;
    }

    public void setModelGraph(ModelGraph modelGraph) {
        this.modelGraph = modelGraph;
    }

    public void setSchema(String from, String to) {
        SchemaChange schemaChange = new SchemaChange(table, from, to);
        schemaChange.renameSchema();
        schemaChange = null;

    }

    public void setSchema(HashMap<String, String> map) {
        SchemaChange schemaChange = new SchemaChange(table, map);
        schemaChange.renameSchema();
        schemaChange = null;

    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        if (table != null) {
            table.put("name", logicalName);
        }
        this.logicalName = logicalName;
    }

    public boolean isAlias() {
        return !this.logicalName.equalsIgnoreCase(dbName);
    }

    public JSONArray getExcelDerivedArray() {
        JSONArray retList = new JSONArray();
        derivedColumnMap.forEach((key, column) -> {
            JSONObject retColumn = new JSONObject(column.getColumn(), Model.DerivedColumn.getStringValues());

            retColumn.put(Model.DerivedColumn.MODEL_NAME.toString(), modelName);
            retColumn.put(Model.DerivedColumn.PHYSICAL_TABLE.toString(), dbName);
            retColumn.put(Model.DerivedColumn.TABLE_NAME.toString(), logicalName);
            retColumn.put(Model.DerivedColumn.COLUMN_TYPE.toString(), column.getType());
            if (R.COMMENTS) {
                ModelUtil.setCommentsAnnotaion(column.getColumn(), retColumn);
            }
            retList.put(retColumn);
        });
        return retList;
    }

    public boolean setExcelDerivedColumn(JSONObject column, String exp) {
        String columnName = column.getString("name");
        String columnType = column.getString(Model.DerivedColumn.COLUMN_TYPE.toString());
        logger.log(Level.FINE, "Setting derived column {0} in table {1}", new Object[]{columnName, this.logicalName});
        StringBuilder sb = new StringBuilder();
        ModelUtil.getLocalColumnsFromExp(exp).forEach((localCol) -> {
//            String searchKey = localCol.replaceAll("[\\[\\]]", "");
            String searchKey = this.logicalName + localCol;
//            if (!(allTableColumnMap.containsKey(searchKey.toLowerCase()) || this.modelGraph.getMeasureMap().containsKey(localCol.toLowerCase()))) {
//                sb.append("Column/Measure name ").append(localCol).append(" not found").append("\n");
//            }

            if (!(this.modelGraph.getAllModelColumnMap().containsKey(searchKey.toLowerCase())
                    || this.modelGraph.getAllModelColumnMap().containsKey(localCol.toLowerCase()))) {
                String expColumn = localCol.replaceAll("[\\[\\]]", "\"");
                if (!exp.contains(expColumn)) {
                    sb.append("Column/Measure name ").append(localCol).append(" not found").append("\n");
                }
            }
        });
        if (sb.length() > 0) {
            ModelUtil.putErrorInfo(column, sb.toString(), Model.ExcelMetaData.__ERROR.toString());
            logger.log(Level.SEVERE, "Table {0} Error Creating derived column/measure {1} : {2}", new Object[]{this.logicalName, columnName, sb.toString()});
        }
        if (derivedColumnMap.containsKey(columnName.toLowerCase())) {
            logger.log(Level.FINEST, "Column already exist..");
            derivedColumnMap.get(columnName.toLowerCase()).setColumn(column);
            ModelUtil.putErrorInfo(column, String.format("Derived column %1$s overwritten in Table %2$s", columnName, logicalName), Model.ExcelMetaData.__WARNING.toString());
        } else {
            if (columnType.equalsIgnoreCase(Model.ColumnType.MEASURE.toString()) && this.modelGraph.getMeasureMap().containsKey(columnName.toLowerCase())) {
                DerivedColumn dmModel = this.modelGraph.getMeasureMap().get(columnName.toLowerCase());
                ModelUtil.putErrorInfo(column, String.format("Measure name %1$s in Table %2$s already exist, there are duplicate in Model %3$s and table name %4$s", columnName, logicalName, modelName, dmModel.getTableName()), Model.ExcelMetaData.__ERROR.toString());
                logger.log(Level.SEVERE, "Measure name {0} in Table {1} already exist in Model {2} and table {3}", new Object[]{columnName, logicalName, modelName, dmModel.getTableName()});
                return false;
            }
            if (!allTableColumnMap.containsKey(columnName.toLowerCase())) {
                logger.log(Level.FINEST, "Creating new column...");
                JSONObject cc = new JSONObject(column, Model.DerivedColumn.getModelValueArray());

                DerivedColumn derivedColumn = new DerivedColumn(cc, columnType, columnName, this.logicalName);
                derivedColumnMap.put(columnName.toLowerCase(), derivedColumn);
                allTableColumnMap.put(columnName.toLowerCase(), cc);
                if (columnType.equalsIgnoreCase(Model.ColumnType.CALCULATED_COLUMN.toString())) {
                    columns.put(cc);
                } else if (columnType.equalsIgnoreCase(Model.ColumnType.MEASURE.toString())) {
                    measures.put(cc);
                    this.modelGraph.getMeasureMap().put(columnName.toLowerCase(), derivedColumn);
                }
                if (R.COMMENTS) {
                    ModelUtil.setComments(column, cc);
                }
                ModelUtil.putErrorInfo(column, String.format("New Derived column %1$s created in Table %2$s", columnName, logicalName), Model.ExcelMetaData.__INFO.toString());

            } else {
                ModelUtil.putErrorInfo(column, String.format("Column name %1$s already exist in Table %2$s", columnName, logicalName), Model.ExcelMetaData.__WARNING.toString());
                logger.log(Level.WARNING, "Column name {0} already exist in Table {1}", new Object[]{columnName, logicalName});
            }

        }
        return true;
    }

    public JSONArray getExcelHierarchies() {
        JSONArray retList = new JSONArray();
        this.allHierarchyMap.forEach((hierNameKey, hierarchy) -> {
            String hierName = hierarchy.getString("name");
            logger.log(Level.FINE, "Getting hierarchy {0} in table {1}", new Object[]{hierName, this.logicalName});
            if (hierarchy.has("levels")) {
                JSONArray levels = hierarchy.getJSONArray("levels");
                for (int i = 0; i < levels.length(); i++) {
                    JSONObject level = levels.getJSONObject(i);
                    JSONObject retlevel = new JSONObject(level, Model.HierarchyLevel.getStringValues());
                    retlevel.put(Model.HierarchyLevel.HIERARCHY_NAME.toString(), hierName);
                    retlevel.put(Model.HierarchyLevel.NEW_HIERARCHY_NAME.toString(), hierName);
                    retlevel.put(Model.HierarchyLevel.MODEL_NAME.toString(), modelName);
                    retlevel.put(Model.HierarchyLevel.TABLE_NAME.toString(), logicalName);
                    if (R.COMMENTS) {
                        ModelUtil.setCommentsAnnotaion(level, retlevel);
                    }
                    retList.put(retlevel);
                }
            }
        });
        return retList;
    }

    public void setExcelHierarchies(String hierName, JSONArray excellevels) {
        String newHierarchyName = null;
        JSONArray levels = new JSONArray();
        logger.log(Level.FINE, "Setting hierarchy {0} in table {1}", new Object[]{hierName, this.logicalName});
        for (int i = 0; i < excellevels.length(); i++) {
            JSONObject row = excellevels.getJSONObject(i);
            newHierarchyName = row.getString(Model.HierarchyLevel.NEW_HIERARCHY_NAME.toString());
            String columnName = row.getString(Model.HierarchyLevel.column.toString());
            if (allTableColumnMap.containsKey(columnName.toLowerCase())) {
                JSONObject retlevel = new JSONObject(row, Model.HierarchyLevel.getModelValueArray());
                if (R.COMMENTS) {
                    ModelUtil.setComments(row, retlevel);
                }
                logger.log(Level.FINEST, "Hierarchy {0} level: ", new Object[]{newHierarchyName, retlevel.toString()});
                levels.put(retlevel);
                ModelUtil.putErrorInfo(row, String.format("Level %1$s created for Hierarchy %2$s", columnName, newHierarchyName), Model.ExcelMetaData.__INFO.toString());
            } else {
                String errorMsg = String.format("Column %1$s not found for Hierarchy %2$s", columnName, newHierarchyName);
                logger.log(Level.SEVERE, errorMsg);
                ModelUtil.putErrorInfo(row, errorMsg, Model.ExcelMetaData.__ERROR.toString());
            }

        }
        if (newHierarchyName == null) {
            newHierarchyName = hierName;
        }

        ModelsWrapper.getInstance().getModelRenameMap(this.modelName).put((this.logicalName + "." + newHierarchyName).toLowerCase(), newHierarchyName);
        JSONObject hierarchyObject = this.allHierarchyMap.get(hierName.toLowerCase());
        if (hierarchyObject == null) {
            logger.log(Level.FINEST, "Creating new Hierarchy {0} in table {1}: ", new Object[]{newHierarchyName, this.logicalName});
            hierarchyObject = new JSONObject();
            this.hierarchies.put(hierarchyObject);
        } else {
            ModelUtil.putErrorInfo(excellevels, "Hierarchy " + hierName + " was already present. Overwriten the levels", Model.ExcelMetaData.__INFO.toString());
            this.allHierarchyMap.remove(hierName.toLowerCase());
        }
        hierarchyObject.put("name", newHierarchyName);
        hierarchyObject.put("levels", levels);
        logger.log(Level.FINEST, "Hierarchy {0} has {1} levels", new Object[]{newHierarchyName, levels.length()});
        this.allHierarchyMap.put(newHierarchyName.toLowerCase(), hierarchyObject);

    }

    public JSONArray getExcelRenameArray(boolean onlyColumn) {
        JSONArray retList = new JSONArray();
        logger.log(Level.FINE, "Geting rename columns for table {}", new Object[]{this.logicalName});
        for (int j = 0; j < columns.length(); j++) {
            JSONObject column = columns.getJSONObject(j);
            String columnName = column.getString("name");
            String sourceColumn = column.optString("sourceColumn", columnName);
            JSONObject retColumn = new JSONObject(column, Model.RenameColumn.getStringValues());
            retColumn.put(Model.RenameColumn.PHYSICAL_COLUMN.toString(), sourceColumn);
            retColumn.put(Model.RenameColumn.COLUMN_NAME.toString(), columnName);
            if (column.has("type") && column.getString("type").equalsIgnoreCase("calculated")) {
                if (onlyColumn) {
                    continue;
                }
                retColumn.put(Model.RenameColumn.COLUMN_TYPE.toString(), Model.ColumnType.CALCULATED_COLUMN.toString());
            } else {
                retColumn.put(Model.RenameColumn.COLUMN_TYPE.toString(), Model.ColumnType.COLUMN.toString());
            }

            if (column.has(Model.RenameColumnExp.expression.toString())) {
                retColumn.put(Model.RenameColumnExp.expression.toString(), column.optString(Model.RenameColumnExp.expression.toString(), ""));
            }
            retColumn.put(Model.RenameColumn.MODEL_NAME.toString(), modelName);
            retColumn.put(Model.RenameColumn.NEW_COLUMN_NAME.toString(), columnName);
            //retColumn.put(FileEnums.RenameColumn.NEW_TABLE_NAME.toString(), this.logicalName);
            retColumn.put(Model.RenameColumn.PHYSICAL_TABLE.toString(), this.dbName);
            retColumn.put(Model.RenameColumn.TABLE_NAME.toString(), this.logicalName);
            if (R.COMMENTS) {
                ModelUtil.setCommentsAnnotaion(column, retColumn);
            }
            retList.put(retColumn);
        }

        if (table.has("measures") && !onlyColumn) {
            logger.log(Level.FINE, "Geting rename measures for table {}", new Object[]{this.logicalName});
            JSONArray measures = table.getJSONArray("measures");
            for (int j = 0; j < measures.length(); j++) {
                JSONObject measure = measures.getJSONObject(j);
                String columnName = measure.getString("name");
                JSONObject retColumn = new JSONObject(measure, Model.RenameColumn.getStringValues());
                retColumn.put(Model.RenameColumn.COLUMN_NAME.toString(), columnName);
                retColumn.put(Model.RenameColumn.COLUMN_TYPE.toString(), Model.ColumnType.MEASURE.toString());
                retColumn.put(Model.RenameColumn.MODEL_NAME.toString(), modelName);
                retColumn.put(Model.RenameColumn.NEW_COLUMN_NAME.toString(), columnName);
                //retColumn.put(FileEnums.RenameColumn.NEW_TABLE_NAME.toString(), this.logicalName);
                retColumn.put(Model.RenameColumn.PHYSICAL_TABLE.toString(), this.dbName);
                retColumn.put(Model.RenameColumn.TABLE_NAME.toString(), this.logicalName);
                if (measure.has(Model.RenameColumnExp.expression.toString())) {
                    retColumn.put(Model.RenameColumnExp.expression.toString(), measure.optString(Model.RenameColumnExp.expression.toString(), ""));
                }
                if (R.COMMENTS) {
                    ModelUtil.setCommentsAnnotaion(measure, retColumn);
                }
                retList.put(retColumn);
            }
        }

        return retList;
    }

    @Override
    public boolean equals(Object to) {
        if (to instanceof TableVertex) {
            return this.logicalName.equals(to.toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.logicalName.hashCode();
    }

    @Override
    public String toString() {
        return this.logicalName;
    }

}
