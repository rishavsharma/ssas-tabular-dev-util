/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

import com.ja.ssas.tabular.common.ConfigDiffMerge;
import com.ja.ssas.tabular.common.ExcelWriteUtil;
import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import com.ja.ssas.tabular.common.R;
import com.ja.ssas.tabular.common.ThreadExecuter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;

/**
 *
 * @author rishav.sharma
 */
public class ExcelConfigDiff {

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

        Logger logger = LogCountHandler.getInstance().getLogger("ExcelConfigDiff");
        logger.log(Level.FINE, "Looking configurations at:{0}", log.getAbsolutePath());
        Options options = new Options();
        Option featureFiles = new Option("f", "feature", true, "Feature input Excel config files or directory where feature files are available");
        featureFiles.setRequired(true);
        featureFiles.setArgs(Option.UNLIMITED_VALUES);
        options.addRequiredOption("m", "master", true, "Master input Excel config file");
        options.addOption(featureFiles);
        options.addOption("n", "rename", false, "rename model based on excel config");
        options.addOption("r", "relation", false, "build relationships based on excel config");
        options.addOption("a", "alias", false, "create aliases based on excel config");
        options.addOption("h", "hierarchy", false, "export hierarchy Columns to excel");
        options.addOption("d", "derived", false, "create derived column (calculated or measures) based on excel config");
        options.addRequiredOption("i", "action", true, "diff | merge");
        options.addOption("o", "output", true, "output directory for results");
        options.addOption("c", "conflicts", false, "Export only the conflicts");
        options.addOption("x", "comments", false, "Write Comments");
        options.addOption("u", "updated", false, "Updated record as new row");
        options.addOption("p", "threads", true, "Number of threads spawn 1 is default any valye from 2 to number of cores in the system");
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("comments")) {
                R.COMMENTS = true;
            }
            if(cmd.hasOption("threads")){
                R.NO_OF_THREADS = Integer.parseInt(cmd.getOptionValue("threads"));
            }
            if(cmd.hasOption("updated")){
                R.UPDATE_AS_NEW = true;
            }
            String action = cmd.getOptionValue("action");
            String outputFolder = ".";
            if (cmd.hasOption("output")) {
                outputFolder = cmd.getOptionValue("output");
            }
            File outFileDir = new File(outputFolder);
            if (!outFileDir.exists()) {
                outFileDir.mkdirs();
            }

            String base = cmd.getOptionValue("master");
            File baseFile = new File(base);
            if (!baseFile.exists()) {
                logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", baseFile.getAbsolutePath());
                System.exit(1);
            }
            ExcelReadHelper excelHelperBase = new ExcelReadHelper(baseFile, cmd, true);
            Model.ExcelSheets[] valuesExcelSheets = Model.ExcelSheets.values();
            String[] features = cmd.getOptionValues("feature");
            ConcurrentHashMap<String, List<JSONArray>> sheetDIffArray = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, JSONArray> allSheetFeaturesDiff = new ConcurrentHashMap<>();
            List<Callable<String>> tasksList = new ArrayList<>();
            List<File> featureFileList = new ArrayList<>();
            for (String feature : features) {
                File featureFile = new File(feature);
                if (!featureFile.exists()) {
                    logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", featureFile.getAbsolutePath());
                    System.exit(1);
                } else {
                    if (featureFile.isDirectory()) {
                        Arrays.asList(featureFile.listFiles()).forEach((fFile) -> {
                            if (fFile.getName().toLowerCase().endsWith(".xls") || fFile.getName().toLowerCase().endsWith(".xlsx")) {
                                featureFileList.add(fFile);
                            }
                        });
                    } else {
                        featureFileList.add(featureFile);
                    }
                }

            }
            for (File featureFile : featureFileList) {
                if (!featureFile.exists()) {
                    logger.log(Level.SEVERE, "Config Excel file doesn't exist : {0}", featureFile.getAbsolutePath());
                    System.exit(1);
                }
                Callable<String> callableTask = () -> {
                    ExcelReadHelper excelHelperFeature = new ExcelReadHelper(featureFile, cmd, true);

                    for (int i = 0; i < valuesExcelSheets.length; i++) {
                        Model.ExcelSheets valuesExcelSheet = valuesExcelSheets[i];
                        List<JSONArray> sheetData = sheetDIffArray.get(valuesExcelSheet.toString());
                        if (sheetData == null) {
                            sheetData = new ArrayList<>();
                            sheetDIffArray.put(valuesExcelSheet.toString(), sheetData);
                        }

                        if (excelHelperBase.getSheetMap().containsKey(valuesExcelSheet.toString())) {
                            if (!excelHelperFeature.getSheetMap().containsKey(valuesExcelSheet.toString())) {
                                logger.log(Level.SEVERE, "Config Excel {1} does'nt have sheet : {0}", new Object[]{valuesExcelSheet.toString(), featureFile.getName()});
                            }
                            JSONArray src = excelHelperBase.getSheetMap().get(valuesExcelSheet.toString());
                            JSONArray tgt = excelHelperFeature.getSheetMap().get(valuesExcelSheet.toString());
                            JSONArray diffArray = ConfigDiffMerge.getDiffArray(src, tgt, valuesExcelSheet.classz, featureFile.getName());
                            allSheetFeaturesDiff.put(featureFile.getName() + valuesExcelSheet.toString(), diffArray);
                            sheetData.add(diffArray);

                        }

                    }
                    return featureFile.getName();
                };
                tasksList.add(callableTask);
            }
            try {
                List<Future<String>> results = ThreadExecuter.getInstance().invokeAll(tasksList);

                for (Future<String> result : results) {
                    logger.log(Level.INFO, "Thread completed for diff :" + result.get());
                }
            } catch (InterruptedException e1) {
                logger.log(Level.SEVERE, "Thread error diff", e1);
            }
            if (action.equals("diff")) {
                for (File featureFile : featureFileList) {
                    String fileName = featureFile.getName();
                    ExcelWriteUtil w = new ExcelWriteUtil(outFileDir.getAbsolutePath() + File.separator + "DIFF_" + fileName);
                    for (int i = 0; i < valuesExcelSheets.length; i++) {
                        Model.ExcelSheets valuesExcelSheet = valuesExcelSheets[i];
                        if (excelHelperBase.getSheetMap().containsKey(valuesExcelSheet.toString())) {
                            JSONArray diff = allSheetFeaturesDiff.get(fileName + valuesExcelSheet.toString());
                            w.writeSheet(valuesExcelSheet.toString(), i, diff, valuesExcelSheet.classz);
                        }
                    }
                    w.close();
                }
            } else {
                ExcelWriteUtil w = new ExcelWriteUtil(outFileDir.getAbsolutePath() + File.separator + "MERGE_" + baseFile.getName());
                boolean emitConflicts = false;
                if (cmd.hasOption("conflicts")) {
                    emitConflicts = true;
                }
                for (int i = 0; i < valuesExcelSheets.length; i++) {
                    Model.ExcelSheets valuesExcelSheet = valuesExcelSheets[i];
                    if (excelHelperBase.getSheetMap().containsKey(valuesExcelSheet.toString())) {
                        List<JSONArray> diffArray = sheetDIffArray.get(valuesExcelSheet.toString());
                        JSONArray master = excelHelperBase.getSheetMap().get(valuesExcelSheet.toString());
                        JSONArray mergeArray = ConfigDiffMerge.getMergeArray(master, diffArray, valuesExcelSheet.classz, emitConflicts);
                        w.writeSheet(valuesExcelSheet.toString(), i, mergeArray, valuesExcelSheet.classz);
                    }

                }
                w.close();
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ExcelConfigDiff", options);
            System.exit(1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            formatter.printHelp("ExcelConfigDiff", options);
            System.exit(1);
        } finally {
            if (!ThreadExecuter.getInstance().isShutdown()) {
                ThreadExecuter.getInstance().shutdown();
            }
        }
    }

}
