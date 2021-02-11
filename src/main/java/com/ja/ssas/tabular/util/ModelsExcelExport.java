/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

import com.ja.ssas.tabular.common.ExcelWriteUtil;
import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.graph.ModelsWrapper;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class ModelsExcelExport {
//private static final Logger logger = LogCountHandler.getInstance().getLogger("ModelsExcelExport");

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
        Logger logger = LogCountHandler.getInstance().getLogger("ModelsExcelExport");
        Options options = new Options();
        options.addRequiredOption("i", "input", true, "input file path or folder where .bim files are placed");
        options.addOption("n", "rename", false, "export renaming columns to excel");
        options.addOption("r", "relation", false, "export relations to excel");
        options.addOption("a", "alias", false, "export aliases to excel");
        options.addOption("d", "derived", false, "export derived Columns to excel");
        options.addOption("h", "hierarchy", false, "export hierarchy Columns to excel");
        options.addOption("o", "output", true, "folder where output would be writen");
        options.addOption("e", "excel", true, "name of the excel file, don't put path here. use -o for folder");
        options.addOption("m", "multiple", false, "generate model relationships in multiple sheets");
        options.addOption("f", "schema", false, "generate model table schema mapping");
        options.addOption("l", "lineage", false, "Generate lineage");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            String inputFileFolderPath = cmd.getOptionValue("input");
            File file = new File(inputFileFolderPath);
            File[] listFiles = null;
            if (file.isDirectory()) {
                listFiles = file.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".bim"));
            } else {
                listFiles = new File[]{file};
            }

            if (listFiles == null || listFiles.length == 0) {
                logger.log(Level.SEVERE, "No files to process: {0}", inputFileFolderPath);
            }
            ModelsWrapper modelWrapper = ModelsWrapper.getInstance();
            for (File listFile : listFiles) {
                String modelName = listFile.getName().replaceFirst("[.][^.]+$", "");
                modelWrapper.newModel(modelName, listFile);
            }
            String outputFolder = ".";
            if (cmd.hasOption("output")) {
                outputFolder = cmd.getOptionValue("output");
            }
            File outFileDir = new File(outputFolder);
            if (!outFileDir.exists()) {
                outFileDir.mkdirs();
            }
            String excelFileName = "ModelsExcel.xls";
            if (cmd.hasOption("excel")) {
                excelFileName = cmd.getOptionValue("excel");
                if (!excelFileName.endsWith(".xls")) {
                    excelFileName += ".xls";
                }
            }

            int excelSheetPlace = 0;

            ExcelWriteUtil r = new ExcelWriteUtil(outFileDir.getAbsolutePath() + File.separator + excelFileName);
            r.writeSheet(Model.ExcelSheets._MODEL_NAMES.toString(), excelSheetPlace++, modelWrapper.getExcelModelsArray(), Model.Models.class);
            if (cmd.hasOption("alias")) {
                logger.log(Level.INFO, "Exporting aliases..");
                r.writeSheet(Model.ExcelSheets._ALIAS_TABLES.toString(), excelSheetPlace++, modelWrapper.getExcelAliasArray(), Model.AliasTable.class);
            }
            if (cmd.hasOption("hierarchy")) {
                logger.log(Level.INFO, "Exporting hierarchies..");
                JSONArray hierarchies = modelWrapper.getExcelHierarchiesArray();
                r.writeSheet(Model.ExcelSheets._HIERARCHIES.toString(), excelSheetPlace++, hierarchies, Model.HierarchyLevel.class);
            }
            if (cmd.hasOption("derived")) {
                logger.log(Level.INFO, "Exporting derived columns..");
                r.writeSheet(Model.ExcelSheets._DERIVED_COLUMNS.toString(), excelSheetPlace++, modelWrapper.getExcelDerivedColumnArray(), Model.DerivedColumn.class);
            }
            logger.log(Level.INFO, "Exporting table list..");
            r.writeSheet(Model.ExcelSheets._RENAME_TABLES.toString(), excelSheetPlace++, modelWrapper.getExcelRenameTableArray(), Model.RenameTable.class);
            if (cmd.hasOption("rename")) {
                logger.log(Level.INFO, "Exporting renaming columns..");
                r.writeSheet(Model.ExcelSheets._RENAME_COLUMNS.toString(), excelSheetPlace++, modelWrapper.getExcelRenameArray(), Model.RenameColumnExp.class);
            }
            if (cmd.hasOption("relation")) {
                logger.log(Level.INFO, "Exporting relationships..");
                if (cmd.hasOption("multiple")) {
                    JSONArray models = modelWrapper.getExcelModelsArray();
                    for (int i = 0; i < models.length(); i++) {
                        JSONObject get = models.getJSONObject(i);
                        String modelName = get.getString(Model.Models.MODEL_NAME.toString());
                        JSONArray relations = modelWrapper.getExcelRelationshipsArray(modelName);
                        r.writeSheet(modelName, excelSheetPlace++, relations, Model.Relation.class);
                    }
                } else {
                    JSONArray models = modelWrapper.getExcelModelsArray();
                    JSONArray excelRelations = new JSONArray();
                    for (int i = 0; i < models.length(); i++) {
                        JSONObject get = models.getJSONObject(i);
                        String modelName = get.getString(Model.Models.MODEL_NAME.toString());
                        JSONArray relations = modelWrapper.getExcelRelationshipsArray(modelName);
                        for (int j = 0; j < relations.length(); j++) {
                            JSONObject relation = relations.getJSONObject(j);
                            relation.put(Model.AllRelation.MODEL_NAME.toString(), modelName);
                            excelRelations.put(relation);
                        }
                    }

                    r.writeSheet(Model.ExcelSheets._RELATIONSHIPS.toString(), excelSheetPlace++, excelRelations, Model.AllRelation.class);
                }
            }
            if (cmd.hasOption("schema")) {
                logger.log(Level.INFO, "Exporting table schemas..");
                JSONArray excelTableSchema = new JSONArray();
                modelWrapper.getAllModels().forEach((model) -> {
                    model.vertexSet().forEach((table) -> {
                        JSONObject retColumn = new JSONObject();
                        retColumn.put(Model.TableSchema.MODEL_NAME.toString(), model.getName());
                        retColumn.put(Model.TableSchema.PHYSICAL_TABLE.toString(), table.getDbName());
                        retColumn.put(Model.TableSchema.TABLE_NAME.toString(), table.getLogicalName());
                        retColumn.put(Model.TableSchema.SCHEMA_NAME.toString(), table.getSchemaName());
                        excelTableSchema.put(retColumn);
                    });
                });

                r.writeSheet(Model.ExcelSheets._TABLE_SCHEMAS.toString(), excelSheetPlace++, excelTableSchema, Model.TableSchema.class);
            }

            if (cmd.hasOption("lineage")) {
                JSONArray retLineage = new JSONArray();
                modelWrapper.getAllModels().forEach((model) -> {
                    JSONArray mLineage = model.getExcelLineage();
                    for (int i = 0; i < mLineage.length(); i++) {
                        retLineage.put(mLineage.get(i));
                    }
                });
                if (!cmd.hasOption("excel")) {
                    excelFileName = "ALL";
                }
                String excelName = outFileDir.getAbsolutePath() + File.separator + "LINEAGE_" + excelFileName;
                ExcelWriteUtil rr = new ExcelWriteUtil(excelName);
                rr.writeSheet("Lineage", 0, retLineage, Model.LineageColumn.class);
                rr.close();
            }
            r.close();
            logger.log(Level.INFO, "Writing files to:{0}", outFileDir.getAbsolutePath());
            logger.log(Level.INFO, "Name of excel file:{0}", outFileDir.getAbsolutePath() + File.separator + excelFileName);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ModelsExcelExport", options);
            System.exit(1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            formatter.printHelp("ModelsExcelExport", options);
            System.exit(1);
        }
    }

}
