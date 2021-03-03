/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.common.ModelUtil;
import com.ja.ssas.tabular.common.R;
import com.ja.ssas.tabular.common.ThreadExecuter;
import com.ja.ssas.tabular.graph.ModelGraph;
import com.ja.ssas.tabular.graph.ModelsWrapper;
import com.ja.ssas.tabular.graph.TableVertex;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Rishav
 */
public class ModelExcelImport {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File log = new File("conf\\log.properties");
        if (log.exists()) {
            System.setProperty("java.util.logging.config.file", log.getAbsolutePath());
            System.out.println("Log Config loaded:" + log.getAbsolutePath());
        }
        File logsFolder = new File("./logs/");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        long startTime = System.currentTimeMillis();
        Logger logger = LogCountHandler.getInstance().getLogger("ModelExcelImport");
        logger.log(Level.FINE, "Looking configurations at:{0}", log.getAbsolutePath());

        Options options = new Options();
        OptionGroup inputOptionGroup = new OptionGroup();
        inputOptionGroup.addOption(new Option("i", "input", true, "input .bim file or folder with .bim files to be proccessed"));
        inputOptionGroup.addOption(new Option("t", "template", true, "SSAS model file(.bim) with freshly imported tables to rebuild the whole model from excel config"));
        inputOptionGroup.setRequired(true);
        options.addOptionGroup(inputOptionGroup);
        options.addRequiredOption("e", "excel", true, "input Excel config file");
        options.addOption("n", "rename", false, "rename model based on excel config");
        options.addOption("r", "relation", false, "build relationships based on excel config");
        options.addOption("a", "alias", false, "create aliases based on excel config");
        options.addOption("h", "hierarchy", false, "export hierarchy Columns to excel");
        options.addOption("d", "derived", false, "create derived column (calculated or measures) based on excel config");
        options.addOption("g", "generate", false, "generate combined model as per _MODELS excel sheet");
        options.addOption("o", "output", true, "folder where output would be writen");
        options.addOption("s", "status", false, "Export the excel with failure status highlighted in each sheet");
        //options.addOption("m", "multiple", false, "generate model relationships status in multiple sheets");
        options.addOption("x", "scripts", false, "output TMSL script files to out folder/scripts");
        //options.addOption("f", "schema", false, "change schema for tables");
        //options.addOption("l", "lineage", false, "Generate lineage");
        options.addOption("z", "test", false, "do not generate bim output files");

        options.addOption("p", "threads", true, "Number of threads spawn 1 is default any valye from 2 to number of cores in the system");
        options.addOption("x", "comments", false, "Write Comments");
        options.addOption("m", "master", true, "master bim file for connection role etc when there are multiple files as template");
        //options.addOption("e", "env", true, "change table schema");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        CommandLine cmd;
        boolean doRename = false;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("comments")) {
                R.COMMENTS = true;
            }
            if (cmd.hasOption("threads")) {
                R.NO_OF_THREADS = Integer.parseInt(cmd.getOptionValue("threads"));
            }
            String inputExcelFile = cmd.getOptionValue("excel");
            File excelFile = new File(inputExcelFile);
            if (!excelFile.exists()) {
                logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", excelFile.getAbsolutePath());
                System.exit(1);
            }
            ExcelReadHelper excelHelper = new ExcelReadHelper(excelFile, cmd);

            ModelsWrapper modelWrapper = ModelsWrapper.getInstance();
            String outputFolder = ".";
            if (cmd.hasOption("output")) {
                outputFolder = cmd.getOptionValue("output");
            }
            File outFileDir = new File(outputFolder);
            if (!outFileDir.exists()) {
                outFileDir.mkdirs();
            }
            if (cmd.hasOption("generate")) {
                logger.log(Level.FINE, "Generating Excel for merged model ...");
                //String fileName = outFileDir.getAbsolutePath() + File.separator + "ALL_IN_ONE.bim";
                //modelWrapper.getAllInOneModel().writer(fileName);
                //logger.log(Level.FINE, "File at {0}", fileName);
                excelHelper.writeMergedExcel();
                return;
            }
            logger.log(Level.FINE, "Writing output to:{0}", outFileDir.getAbsolutePath());
            if (cmd.hasOption("template")) {
                logger.log(Level.FINE, "Creating models from template/basemodel..");
                String inputTemplateFile = cmd.getOptionValue("template");
                File templateFile = new File(inputTemplateFile);
                if (!templateFile.exists()) {
                    logger.log(Level.SEVERE, "Template folder or .bim file doesn't exist : {0}", templateFile.getAbsolutePath());
                    System.exit(1);
                }
                ModelGraph modelGraphTemplate = null;
                File[] listFiles = null;
                if (templateFile.isDirectory()) {
                    listFiles = templateFile.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".bim"));
                    Arrays.sort(listFiles);
                    int startFrom = 1;
                    if (listFiles.length > 0) {
                        if (cmd.hasOption("master")) {
                            String masterFilePath = cmd.getOptionValue("master");
                            File masterFile = new File(masterFilePath);
                            if (!masterFile.exists()) {
                                logger.log(Level.SEVERE, "Master .bim file doesn't exist : {0}", masterFile.getAbsolutePath());
                                System.exit(1);
                            }
                            startFrom = 0;
                            modelGraphTemplate = new ModelGraph(ModelUtil.readModel(masterFile), "Template");
                        } else {
                            modelGraphTemplate = new ModelGraph(ModelUtil.readModel(listFiles[0]), "Template");
                        }
                        for (int i = startFrom; i < listFiles.length; i++) {
                            modelGraphTemplate.addTablesFromModel(new ModelGraph(ModelUtil.readModel(listFiles[i]), listFiles[i].getName()));
                        }
                    } else {
                        logger.log(Level.SEVERE, "Template folder .bim files doesn't exist : {0}", templateFile.getAbsolutePath());
                        System.exit(1);
                    }

                } else {
                    modelGraphTemplate = new ModelGraph(ModelUtil.readModel(templateFile), "Template");
                }

                modelWrapper.setBaseModel(modelGraphTemplate);
                ModelGraph blankGraph = modelGraphTemplate.getblankGraph();

                //Create models
                JSONArray excelModels = excelHelper.getModelNames();
                for (int i = 0; i < excelModels.length(); i++) {
                    JSONObject excelModel = excelModels.getJSONObject(i);
                    String modelName = excelModel.getString(Model.Models.MODEL_NAME.toString());

                    ModelGraph newModel = modelWrapper.getModel(modelName);
                    if (newModel == null) {
                        newModel = blankGraph.getblankGraph();
                        newModel.setName(modelName);
                        if (R.COMMENTS) {
                            String comments = excelModel.optString(Model.Comments._COMMENTS.toString(), "");
                            newModel.setComments(comments);
                        }
                        modelWrapper.newModel(modelName, newModel);
                    }
                }
                //Create aliases from template

                JSONArray aliasTables = excelHelper.getAliasTables();
                for (int i = 0; i < aliasTables.length(); i++) {
                    JSONObject aliasTable = aliasTables.getJSONObject(i);
                    String modelName = aliasTable.getString(Model.AliasTable.MODEL_NAME.toString());
                    String tableName = aliasTable.getString(Model.AliasTable.TABLE_NAME.toString());
                    String physicalTable = aliasTable.getString(Model.AliasTable.PHYSICAL_TABLE.toString());
                    TableVertex vertex = modelGraphTemplate.getVertex(physicalTable);
                    ModelGraph newModel = modelWrapper.getModel(modelName);
                    if (newModel == null) {
                        String errorMsg = String.format("Model %1$s not found in the excel config file", modelName);
                        logger.log(Level.SEVERE, errorMsg);
                        ModelUtil.putErrorInfo(aliasTable, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                        continue;
                    }
                    if (vertex == null) {
                        String errorMsg = String.format("Table %1$s not found in the imported bim: %2$s", physicalTable, templateFile.getName());
                        logger.log(Level.SEVERE, errorMsg);
                        ModelUtil.putErrorInfo(aliasTable, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                    } else {
                        TableVertex cloneNew = vertex.clone();
                        cloneNew.setLogicalName(tableName);
                        newModel.addVertex(cloneNew);
                        if (R.COMMENTS) {
                            ModelUtil.setComments(aliasTable, cloneNew.getTable());
                        }
                        ModelUtil.putErrorInfo(aliasTable, "Alias table " + tableName + " created", Model.ExcelMetaData.__INFO.toString());
                    }
                }
                //Create rest of tables and model from template
                JSONArray renametable = excelHelper.getRenameTable();
                for (int i = 0; i < renametable.length(); i++) {
                    JSONObject row = renametable.getJSONObject(i);
                    String modelName = row.getString(Model.RenameTable.MODEL_NAME.toString());
                    //String parentName = row.getString(FileEnums.RenameTable.PARENT_TABLE.toString());
                    String physicalTable = row.getString(Model.RenameTable.PHYSICAL_TABLE.toString());
                    String tableName = row.getString(Model.RenameTable.TABLE_NAME.toString());
                    String tableType = row.getString(Model.RenameTable.TABLE_TYPE.toString());

                    if (tableType.equalsIgnoreCase(Model.TableType.TABLE.toString())) {
                        TableVertex lkpVertex = modelGraphTemplate.getVertex(physicalTable);

                        ModelGraph newModel = modelWrapper.getModel(modelName);
                        if (newModel == null) {
                            String errorMsg = String.format("Model %1$s not found in the excel config file", modelName);
                            logger.log(Level.SEVERE, errorMsg);
                            ModelUtil.putErrorInfo(row, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                            continue;
                        }
                        if (lkpVertex == null) {
                            String errorMsg = String.format("Table %1$s not found in the imported bim: %2$s", physicalTable, templateFile.getName());
                            logger.log(Level.SEVERE, errorMsg);
                            ModelUtil.putErrorInfo(row, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                        } else {
                            TableVertex newVertex = lkpVertex.clone();
                            newVertex.setLogicalName(tableName);
                            if (R.COMMENTS) {
                                ModelUtil.setComments(row, newVertex.getTable());
                            }

                            newModel.addVertex(newVertex);
                            ModelUtil.putErrorInfo(row, "Table " + tableName + " created", Model.ExcelMetaData.__INFO.toString());
                        }
                    }

                }

            } else {
                String inputFileFolder = cmd.getOptionValue("input");
                File bimFileFolder = new File(inputFileFolder);
                File[] listFiles = null;
                if (bimFileFolder.isDirectory()) {
                    listFiles = bimFileFolder.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".bim"));
                } else {
                    listFiles = new File[]{bimFileFolder};
                }

                if (listFiles != null && listFiles.length != 0) {
                    for (File listFile : listFiles) {
                        String modelName = listFile.getName().replaceFirst("[.][^.]+$", "");
                        logger.log(Level.FINE, "Reading model {0} from file:{1}", new Object[]{modelName, listFile.getAbsolutePath()});
                        ModelGraph newModel = modelWrapper.newModel(modelName, listFile);
                    }

                } else {
                    logger.log(Level.SEVERE, "No files to process: {0}", inputFileFolder);
                }

            }
            if (cmd.hasOption("alias")) {
                logger.log(Level.FINE, "Creating aliases...");
                JSONArray aliasTables = excelHelper.getAliasTables();
                for (int i = 0; i < aliasTables.length(); i++) {
                    JSONObject aliasTable = aliasTables.getJSONObject(i);
                    String modelName = aliasTable.getString(Model.AliasTable.MODEL_NAME.toString());
                    String tableName = aliasTable.getString(Model.AliasTable.TABLE_NAME.toString());
                    String physicalTable = aliasTable.getString(Model.AliasTable.PHYSICAL_TABLE.toString());
                    ModelGraph model = modelWrapper.getModel(modelName);
                    logger.log(Level.FINE, "Creating alias {0} from Table {1} in Model {2}", new Object[]{tableName, physicalTable, modelName});
                    if (model != null) {
                        TableVertex vertexAlais = model.getVertex(tableName);
                        if (vertexAlais != null) {
                            String msg = String.format("Table %0$s already found in model %1$s", tableName, modelName);
                            logger.log(Level.FINE, msg);
                            continue;
                        }
                        TableVertex vertex = model.createAlias(physicalTable, tableName);
                        if (R.COMMENTS) {
                            ModelUtil.setComments(aliasTable, vertex.getTable());
                        }
                        if (vertex == null) {
                            String errorMsg = String.format("Table %0$s not found in model %1$s", physicalTable, modelName);
                            logger.log(Level.SEVERE, errorMsg);
                            ModelUtil.putErrorInfo(aliasTable, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                        } else {
                            ModelUtil.putErrorInfo(aliasTable, "Alias table " + tableName + " created", Model.ExcelMetaData.__INFO.toString());
                        }
                    } else {
                        String errorMsg = String.format("Model name %1$s not found", modelName);
                        logger.log(Level.SEVERE, errorMsg);
                        ModelUtil.putErrorInfo(aliasTable, errorMsg, Model.ExcelMetaData.__ERROR.toString());
                    }
                }
            }

            if (cmd.hasOption("derived")) {
                logger.log(Level.FINE, "Creating derived columns...");
                modelWrapper.setExcelDerivedColumn(excelHelper.getDerivedColumns());
            }
            if (cmd.hasOption("relation")) {
                logger.log(Level.FINE, "Creating relationships...");
                excelHelper.getRelationForAllModel().forEach((modelName, relExcelArray) -> {
                    modelWrapper.setExcelRelationshipsArray(modelName, relExcelArray);
                });
            }
            if (cmd.hasOption("hierarchy")) {
                logger.log(Level.FINE, "Creating hierarchies...");
                modelWrapper.setExcelHierarchiesArray(excelHelper.getHierarchiesForAllModel());
            }
//            if (cmd.hasOption("lineage")) {
//                JSONArray retLineage = new JSONArray();
//                modelWrapper.getAllModels().forEach((model) -> {
//                    JSONArray mLineage = model.getExcelLineage();
//                    for (int i = 0; i < mLineage.length(); i++) {
//                        retLineage.put(mLineage.get(i));
//                    }
//                });
//                excelHelper.writeLineageExcel(outFileDir, retLineage);
//            }
            if (cmd.hasOption("schema")) {
                logger.log(Level.FINE, "Setting table schemas..");
                JSONArray _TABLE_SCHEMAS = excelHelper.getTableSchema();
                for (int i = 0; i < _TABLE_SCHEMAS.length(); i++) {
                    JSONObject row = _TABLE_SCHEMAS.getJSONObject(i);
                    String MODEL_NAME = row.getString(Model.TableSchema.MODEL_NAME.toString());
                    String TABLE_NAME = row.getString(Model.TableSchema.TABLE_NAME.toString());
                    String NEW_SCHEMA_NAME = row.getString(Model.TableSchema.NEW_SCHEMA_NAME.toString());
                    String SCHEMA_NAME = row.getString(Model.TableSchema.SCHEMA_NAME.toString());
                    ModelGraph model = modelWrapper.getModel(MODEL_NAME);

                    if (model != null) {
                        TableVertex vertex = model.getVertex(TABLE_NAME);
                        if (vertex != null) {
                            vertex.setSchema(SCHEMA_NAME, NEW_SCHEMA_NAME);
                        } else {
                            logger.log(Level.SEVERE, MODEL_NAME + ": Table not found for schema change: " + TABLE_NAME);
                        }
                    }
                }

            }
            if (cmd.hasOption("rename")) {
                logger.log(Level.FINE, "Starting renaming...");
                doRename = true;
            }
            modelWrapper.renameModelsFromExcel(excelHelper, doRename);
            if (!cmd.hasOption("test")) {
                logger.log(Level.FINE, "Writing models to file ...");
                modelWrapper.writeModels(outFileDir);
            }
            if (cmd.hasOption("scripts")) {
                File outFileScripts = new File(outputFolder + File.separator + "scripts");
                if (!outFileScripts.exists()) {
                    outFileDir.mkdirs();
                }
                modelWrapper.writeModelsScript(outFileScripts);
            }

            if (cmd.hasOption("status")) {
                logger.log(Level.FINE, "Starting status generation...");

                if (!cmd.hasOption("schema")) {
                    logger.log(Level.FINE, "Exporting table schemas to Status File..");
                    JSONArray excelTableSchema = new JSONArray();
                    modelWrapper.getAllModels().forEach((model) -> {
                        model.vertexSet().forEach((table) -> {
                            JSONObject retColumn = new JSONObject();
                            retColumn.put(Model.TableSchema.MODEL_NAME.toString(), model.getName());
                            retColumn.put(Model.TableSchema.PHYSICAL_TABLE.toString(), table.getDbName());
                            retColumn.put(Model.TableSchema.TABLE_NAME.toString(), table.getLogicalName());
                            retColumn.put(Model.TableSchema.SCHEMA_NAME.toString(), table.getSchemaName());
                            retColumn.put(Model.TableSchema.NEW_SCHEMA_NAME.toString(), table.getSchemaName());
                            excelTableSchema.put(retColumn);
                        });
                    });
                    excelHelper.setTableSchema(excelTableSchema);
                }
                excelHelper.writeStatusExcel(outFileDir);
            }
            long timeTaken = (System.currentTimeMillis() - startTime) / 1000;
            LogCountHandler.getInstance().printSummary();
            logger.log(Level.SEVERE, "Total Time taken: " + String.valueOf(timeTaken) + " Seconds");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ModelsExcelImport", options);
            System.exit(1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            formatter.printHelp("ModelsExcelImport", options);
            System.exit(1);
        } finally {
            if (!ThreadExecuter.getInstance().isShutdown()) {
                ThreadExecuter.getInstance().shutdown();
            }

        }
    }

}
