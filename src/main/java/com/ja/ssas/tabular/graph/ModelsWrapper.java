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
import com.ja.ssas.tabular.common.ThreadExecuter;
import com.ja.ssas.tabular.util.ExcelReadHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class ModelsWrapper {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("ModelsWrapper");
    private static ModelsWrapper modelsWrapper = null;
    private final HashMap<String, ModelGraph> modelsMap;
    private final HashMap<String, HashMap<String, String>> renameMap;
    //Maps of model then in model maps of tables then maps of hierarchy
    private final HashMap<String, HashMap<String, JSONArray>> hierarchyMap;

    private ModelGraph baseModel;

    private ModelsWrapper() {
        modelsMap = new HashMap<>();
        renameMap = new HashMap<>();
        hierarchyMap = new HashMap<>();

    }

    public static ModelsWrapper getInstance() {
        if (modelsWrapper == null) {
            modelsWrapper = new ModelsWrapper();
        }

        return modelsWrapper;
    }

    public void setBaseModel(ModelGraph baseModel) {
        this.baseModel = baseModel;
    }

    public ModelGraph newModel(String name, ModelGraph graphModel) {
        hierarchyMap.put(name, new HashMap<>());
        return modelsMap.put(name, graphModel);
    }

    public ModelGraph newModel(String name, JSONObject model) {
        return newModel(name, new ModelGraph(model, name));
    }

    public ModelGraph newModel(String name, File loc) throws IOException {
        return newModel(name, ModelUtil.readModel(loc));
    }

    public ModelGraph newModel(File loc) throws IOException {
        String modelName = loc.getName().replaceFirst("[.][^.]+$", "");
        return newModel(modelName, loc);
    }

    public ModelGraph getModel(String name) {
        return modelsMap.get(name);
    }

    public Collection<ModelGraph> getAllModels() {
        return modelsMap.values();
    }

    public boolean isBlank() {
        return modelsMap.isEmpty();
    }

    public ModelGraph getAllInOneModel() {
        if (modelsMap.isEmpty()) {
            logger.log(Level.SEVERE, "There are no models to build from");
            return null;
        }
        ModelGraph retModel = modelsMap.values().iterator().next().getblankGraph();
        retModel.setName("ALL_IN_ONE");
        logger.log(Level.FINE, "Creating blank model from {0}", retModel.getName());
        logger.log(Level.FINE, "Getting all in one model {0}", "AllInOne");
        modelsMap.entrySet().stream().map((entry) -> entry.getValue()).forEachOrdered((graphModel) -> {
            graphModel.edgeSet().forEach((edge) -> {
                retModel.addVertex(edge.getSourceVertex());
                retModel.addVertex(edge.getTargetVertex());
                retModel.addExcelEdge(edge.getRelation());
            });
        });
        return retModel;
    }

    public ModelGraph getAllInOneModel(Map<String, String> inModels) {
        if (modelsMap.isEmpty()) {
            logger.log(Level.SEVERE, "There are no models to build from");
            return null;
        }
        ModelGraph retModel = modelsMap.values().iterator().next().getblankGraph();
        retModel.setName("ALL_IN_ONE");
        logger.log(Level.FINE, "Creating blank model from {0}", retModel.getName());
        logger.log(Level.FINE, "Getting all in one model {0}", "AllInOne");
        modelsMap.entrySet().stream().map((entry) -> entry.getValue()).forEachOrdered((graphModel) -> {
            graphModel.edgeSet().forEach((edge) -> {
                retModel.addVertex(edge.getSourceVertex());
                retModel.addVertex(edge.getTargetVertex());
                retModel.addExcelEdge(edge.getRelation());
            });
        });
        return retModel;
    }

    public JSONArray getExcelDerivedColumnArray() {
        JSONArray retList = new JSONArray();
        modelsMap.values().forEach((model) -> {
            logger.log(Level.FINE, "Getting derived column for model {0}", model.getName());
            model.vertexSet().forEach((table) -> {
                table.getExcelDerivedArray().forEach((row) -> {
                    retList.put(row);
                });
            });
        });
        return retList;
    }

    public void setExcelDerivedColumn(HashMap<String, JSONArray> derivedMap) {
        //ExecutorService executor = Executors.newFixedThreadPool(R.NO_OF_THREADS);
        List<Callable<String>> tasksList = new ArrayList<>();
        derivedMap.forEach((modelName, derivedArray) -> {
            logger.log(Level.FINE, "Setting derived column for model {0}", modelName);
            ModelGraph modelGraph = modelsMap.get(modelName);

            if (modelGraph != null) {
                modelGraph.addAllColumnKeys(derivedArray);

                Callable<String> callableTask1 = () -> {
                    for (int j = 0; j < derivedArray.length(); j++) {
                        JSONObject derived = derivedArray.getJSONObject(j);
                        String tableName = derived.getString(Model.DerivedColumn.TABLE_NAME.toString());
                        logger.log(Level.FINE, "Setting derived column for {0}", tableName);
                        modelGraph.setExcelDerivedColumn(tableName, derived);
                    }
                    logger.log(Level.FINE, "Finished setting derived column for model {0}", modelName);
                    return modelGraph.getName();
                };
                tasksList.add(callableTask1);

            } else {
                ModelUtil.putErrorInfo(derivedArray, "Model not found: " + modelName, Model.ExcelMetaData.__ERROR.toString());
                logger.log(Level.SEVERE, "Model name not found: {0}", modelName);
            }

        });

        try {
            List<Future<String>> results = ThreadExecuter.getInstance().invokeAll(tasksList);

            for (Future<String> result : results) {
                logger.log(Level.INFO, "Thread completed for derived measure :" + result.get());
            }
        } catch (InterruptedException | ExecutionException e1) {
            logger.log(Level.SEVERE, "Thread error derived measure", e1);
        }

    }

    public JSONArray getExcelModelsArray() {
        JSONArray retList = new JSONArray();
        modelsMap.entrySet().stream().map((entry) -> {
            JSONObject row = new JSONObject();
            String modelName = entry.getKey();
            ModelGraph model = entry.getValue();
            logger.log(Level.FINE, "Getting Models: {0}", modelName);
            row.put(Model.Models.MODEL_NAME.toString(), modelName);
            if (R.COMMENTS) {
                row.put(Model.Comments._COMMENTS.toString(), model.getComments());
            }                        
            return row;
        }).forEachOrdered((row) -> {
            retList.put(row);
        });

        return retList;
    }

    public JSONArray getExcelAliasArray() {
        JSONArray retList = new JSONArray();
        modelsMap.entrySet().forEach((Map.Entry<String, ModelGraph> entry) -> {
            String modelName = entry.getKey();
            logger.log(Level.FINE, "Getting Aliases for model {0}", modelName);
            ModelGraph model = entry.getValue();
            Set<TableVertex> vertexSet = model.vertexSet();
            vertexSet.stream().filter((tableVertex) -> (tableVertex.isAlias())).map((tableVertex) -> {
                JSONObject row = new JSONObject();
                row.put(Model.AliasTable.MODEL_NAME.toString(), modelName);
                row.put(Model.AliasTable.PHYSICAL_TABLE.toString(), tableVertex.getDbName());
                row.put(Model.AliasTable.TABLE_NAME.toString(), tableVertex.getLogicalName());
                if(R.COMMENTS){
                    ModelUtil.setCommentsAnnotaion(tableVertex.getTable(), row);
                }
                return row;
            }).forEachOrdered((row) -> {
                retList.put(row);
            });
        });

        return retList;
    }

    public JSONArray getExcelHierarchiesArray() {
        JSONArray retList = new JSONArray();
        modelsMap.values().forEach((model) -> {
            logger.log(Level.FINE, "Getting hierarchies for model {0}", model.getName());
            JSONArray excelHierarchiesArray = model.getExcelHierarchiesArray();
            for (int i = 0; i < excelHierarchiesArray.length(); i++) {
                JSONObject row = excelHierarchiesArray.getJSONObject(i);
                retList.put(row);
            }
        });

        return retList;
    }

    public void setExcelHierarchiesArray(HashMap<String, HashMap<String, HashMap<String, JSONArray>>> hierarchyMap) {
        hierarchyMap.forEach((model, tableMap) -> {
            ModelGraph modelGraph = modelsMap.get(model);
            if (modelGraph == null) {
                logger.log(Level.SEVERE, "Model {0} name not found", model);
            } else {
                modelGraph.setExcelHierarchies(tableMap);
            }
        });
    }

    public JSONArray getExcelRelationshipsArray(String model) {
        JSONArray retArray;
        logger.log(Level.FINE, "Setting relationships for model {0}", model);
        ModelGraph modelGraph = modelsMap.get(model);
        if (modelGraph == null) {
            logger.log(Level.SEVERE, "Model {0} name not found", model);
            retArray = new JSONArray();
        } else {
            retArray = modelGraph.getExcelRelationshipsArray();
        }
        return retArray;
    }

    public void setExcelRelationshipsArray(String model, JSONArray relExcelArray) {
        ModelGraph modelGraph = modelsMap.get(model);
        if (modelGraph == null) {
            String errorMsg = String.format("Model %1$s name not found ", model);
            logger.log(Level.SEVERE, errorMsg);
            ModelUtil.putErrorInfo(relExcelArray, errorMsg, Model.ExcelMetaData.__ERROR.toString());
        } else {
            modelGraph.setExcelRelationships(relExcelArray);
        }
    }

    public HashMap<String, String> getModelRenameMap(String model) {
        if (renameMap.containsKey(model)) {
            return renameMap.get(model);
        } else {
            HashMap<String, String> newMap = new HashMap<>();
            renameMap.put(model, newMap);
            return newMap;
        }
    }

    public void writeModels(File theDir) {
        if (!theDir.exists()) {
            theDir.mkdir();
        }
        modelsMap.values().forEach((model) -> {
            String outFile = theDir.getAbsolutePath() + File.separator + model.getName() + ".bim";
            logger.log(Level.FINE, "Writing model {0} to file:{1}", new Object[]{model.getName(), outFile});
            try {
                model.writer(outFile);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, "Failed to write model:" + outFile, ex);
            }
        });
    }

    public void writeModelsScript(File theDir) {
        if (!theDir.exists()) {
            theDir.mkdir();
        }
        modelsMap.values().forEach((model) -> {
            String outFile = theDir.getAbsolutePath() + File.separator + model.getName() + ".xmla";
            String prefix = System.getenv("MODEL_PREFIX");
            if (prefix == null) {
                prefix = "";
            }
            logger.log(Level.FINE, "Writing model script {0} to file:{1}", new Object[]{model.getName(), outFile});
            try {
                model.scriptWriter(outFile, prefix);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, "Failed to write model script:" + outFile, ex);
            }
        });
    }

    public JSONArray getExcelRenameTableArray() {
        JSONArray retList = new JSONArray();
        modelsMap.entrySet().forEach((Map.Entry<String, ModelGraph> entry) -> {
            String modelName = entry.getKey();
            logger.log(Level.FINE, "Getting rename table array for model {0}", modelName);
            ModelGraph model = entry.getValue();
            Set<TableVertex> vertexSet = model.vertexSet();
            vertexSet.stream().map((tableVertex) -> {
                String tableName = tableVertex.getLogicalName();
                logger.log(Level.FINE, "For model {0} get table {1}", new Object[]{modelName, tableName});
                String tablePhysical = tableVertex.getDbName();
                JSONObject retTable = new JSONObject();
                retTable.put(Model.RenameTable.MODEL_NAME.toString(), modelName);
                //retTable.put(FileEnums.RenameTable.PARENT_TABLE.toString(), tableName);
                retTable.put(Model.RenameTable.NEW_TABLE_NAME.toString(), tableName);
                retTable.put(Model.RenameTable.PHYSICAL_TABLE.toString(), tablePhysical);
                retTable.put(Model.RenameTable.TABLE_NAME.toString(), tableName);
                if (tableName.equalsIgnoreCase(tablePhysical)) {
                    retTable.put(Model.RenameTable.TABLE_TYPE.toString(), Model.TableType.TABLE.toString());
                } else {
                    retTable.put(Model.RenameTable.TABLE_TYPE.toString(), Model.TableType.ALIAS.toString());
                }

                if (R.COMMENTS) {
                    ModelUtil.setCommentsAnnotaion(tableVertex.getTable(), retTable);
                }
                return retTable;
            }).forEachOrdered((retTable) -> {
                retList.put(retTable);
            });
        });

        return retList;
    }

    public JSONArray getExcelRenameArray(boolean onlyColumn) {
        JSONArray retList = new JSONArray();
        modelsMap.values().forEach((model) -> {
            logger.log(Level.FINE, "Getting rename column array for model {0}", model.getName());
            model.vertexSet().forEach((table) -> {
                logger.log(Level.FINE, "Getting rename column array for table {0}", table.getLogicalName());
                table.getExcelRenameArray(onlyColumn).forEach((row) -> {
                    retList.put(row);
                });
            });
        });
        return retList;
    }

    public void renameModelsFromExcel(ExcelReadHelper excelHelper, boolean doRename) throws Exception {
        JSONArray renameSheet = excelHelper.getRenameColumn();
        JSONArray renametable = excelHelper.getRenameTable();
        HashMap<String, JSONArray> derivedArrayMap = excelHelper.getDerivedColumns();
        String commentKey = Model.Comments._COMMENTS.toString();
        logger.log(Level.FINE, "Starting renaming for all models..");
        for (int i = 0; i < renametable.length(); i++) {
            JSONObject row = renametable.getJSONObject(i);
            String modelName = row.getString(Model.RenameTable.MODEL_NAME.toString());
            HashMap<String, String> tablecolExp = getModelRenameMap(modelName);
            //String parentName = row.getString(FileEnums.RenameTable.PARENT_TABLE.toString());
            //String tableNamePhysical = row.getString(FileEnums.RenameTable.PHYSICAL_TABLE.toString());
            String tableName = row.getString(Model.RenameTable.TABLE_NAME.toString());
            //String tableType = row.getString(Model.RenameTable.TABLE_TYPE.toString());
            String renameTable = row.getString(Model.RenameTable.NEW_TABLE_NAME.toString());
            String dataCategory = row.optString(Model.RenameTable.dataCategory.toString());
            String isHidden = row.optString(Model.RenameTable.isHidden.toString());
            String description = row.optString(Model.RenameTable.description.toString());
            String tableKey = ("'" + tableName + "'").toLowerCase();
            if (!doRename) {
                renameTable = tableName;
            }
            tablecolExp.put(tableName.toLowerCase(), renameTable);
            tablecolExp.put(tableKey, "'" + renameTable + "'");
            tablecolExp.put(tableName.toLowerCase() + "#dataCategory", dataCategory);
            tablecolExp.put(tableName.toLowerCase() + "#isHidden", isHidden);
            tablecolExp.put(tableName.toLowerCase() + "#description", description);

        }

        for (int i = 0; i < renameSheet.length(); i++) {
            JSONObject row = renameSheet.getJSONObject(i);
            String modelName = row.getString(Model.RenameColumn.MODEL_NAME.toString());
            HashMap<String, String> tablecolExp = getModelRenameMap(modelName);
            //String tableNamePhysical = row.getString(FileEnums.RenameColumn.PHYSICAL_TABLE.toString());
            String tableName = row.getString(Model.RenameColumn.TABLE_NAME.toString());
            String columnName = row.getString(Model.RenameColumn.COLUMN_NAME.toString());

            String renameTable = tablecolExp.get(tableName.toLowerCase());
            if (renameTable == null) {
                renameTable = tableName;
                logger.log(Level.WARNING, "Rename table not found in {0} for table {1}. Retaining original name {1}", new Object[]{modelName, tableName});
            }
            String renameColumn = row.getString(Model.RenameColumn.NEW_COLUMN_NAME.toString());
            if (!doRename) {
                renameColumn = columnName;
            }
            String tableKey = ("'" + tableName + "'").toLowerCase();
            String colKey = (tableName + "#" + columnName).toLowerCase();
            if (R.COMMENTS) {
                String comments = row.optString(commentKey, "");
                tablecolExp.put(colKey + "#" + commentKey, comments);

            }
            if (tablecolExp.containsKey(tableKey)) {
                String renameExisting = tablecolExp.get(tableKey);
                if (!renameExisting.equalsIgnoreCase("'" + renameTable + "'")) {
                    //throw new Exception("Table :" + tableKey + " has 2 names given:" + renameTable + " and " + renameExisting);
                    logger.log(Level.SEVERE, "Table :{0} has 2 names given:{1} and {2}", new Object[]{tableKey, renameTable, renameExisting});
                }
            } else {
                tablecolExp.put(tableName.toLowerCase(), renameTable);
                tablecolExp.put(tableKey, "'" + renameTable + "'");
            }

            row.keySet().forEach((String key) -> {
                if (Model.RenameColumn.getModelValues().contains(key)) {
                    Object val = row.get(key);
                    if (val != null && !val.toString().equals("")) {
                        tablecolExp.put(colKey + "#" + key, val.toString());
                    }

                }
            });
            logger.log(Level.FINEST, "{0}#{1}->{2}#{3}", new Object[]{tableName, columnName, renameTable, renameColumn});

            tablecolExp.put(colKey, renameColumn);
            tablecolExp.put((tableName + "[" + columnName + "]").toLowerCase(), "'" + renameTable + "'[" + renameColumn + "]");
            tablecolExp.put(("'" + tableName + "'[" + columnName + "]").toLowerCase(), "'" + renameTable + "'[" + renameColumn + "]");
        }
        derivedArrayMap.forEach((modelName, rowArray) -> {
            HashMap<String, String> tablecolExp = getModelRenameMap(modelName);
            for (int i = 0; i < rowArray.length(); i++) {
                JSONObject row = rowArray.getJSONObject(i);
                String nameDerivedColumn = row.getString(Model.DerivedColumn.name.toString());
                String tableName = row.getString(Model.DerivedColumn.TABLE_NAME.toString());
                String lkpTableName = tablecolExp.get(tableName.toLowerCase());
                String colKey = (tableName + "#" + nameDerivedColumn).toLowerCase();
                if (!tablecolExp.containsKey(colKey)) {
                    tablecolExp.put(colKey, nameDerivedColumn);
                    tablecolExp.put((tableName + "[" + nameDerivedColumn + "]").toLowerCase(), "'" + lkpTableName + "'[" + nameDerivedColumn + "]");
                    tablecolExp.put(("'" + tableName + "'[" + nameDerivedColumn + "]").toLowerCase(), "'" + lkpTableName + "'[" + nameDerivedColumn + "]");

                }
            }
        });
        //ExecutorService executor = Executors.newFixedThreadPool(R.NO_OF_THREADS);
        List<Callable<String>> tasksList = new ArrayList<>();
        modelsMap.values().forEach((model) -> {
            HashMap<String, String> reMap = renameMap.get(model.getName());
            Callable<String> callableTask = () -> {
                model.renameModel(reMap, doRename);
                return model.getName();
            };
            tasksList.add(callableTask);

        });

        try {
            List<Future<String>> results = ThreadExecuter.getInstance().invokeAll(tasksList);

            for (Future<String> result : results) {
                logger.log(Level.INFO, "Thread completed for renaming :" + result.get());
            }
        } catch (InterruptedException e1) {
            logger.log(Level.SEVERE, "Thread error naming", e1);
        }
        //executor.shutdown();
        JSONArray modelNames = excelHelper.getModelNames();
        for (int i = 0; i < modelNames.length(); i++) {
            JSONObject jObject = modelNames.getJSONObject(i);
            String modelName = jObject.optString(Model.Models.MODEL_NAME.toString());
            String newModelName = jObject.optString(Model.Models.NEW_MODEL_NAME.toString());
            if (newModelName != null && !newModelName.equals("")) {
                ModelGraph gModel = modelsMap.get(modelName);
                if (gModel != null) {
                    gModel.setName(newModelName);
                }
            }

        }
        logger.log(Level.FINE, "Finished renaming for all models..");
    }

}
