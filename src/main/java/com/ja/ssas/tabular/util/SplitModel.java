/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.util;

import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.ModelUtil;
import com.ja.ssas.tabular.graph.ModelGraph;
import com.ja.ssas.tabular.graph.TableVertex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author venris
 */
public class SplitModel {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Logger logger = LogCountHandler.getInstance().getLogger("ModelSplit");

        Options options = new Options();
        options.addRequiredOption("i", "input", true, "input .bim file to be proccessed");
        options.addRequiredOption("m", "map", true, "comma delimited Config file TableName,ModelFile");
        options.addRequiredOption("o", "out", true, "output folder");
        

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String filePath = cmd.getOptionValue("map");
            HashMap<String, String> map = new HashMap<String, String>();

            String line;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length >= 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    map.put(key, value);
                } else {
                    System.out.println("ignoring line: " + line);
                }
            }
            String outputFolder = cmd.getOptionValue("out");

            File outFileDir = new File(outputFolder);
            if (!outFileDir.exists()) {
                outFileDir.mkdirs();
            }

            ModelGraph modelGraphTemplate = new ModelGraph(ModelUtil.readModel(cmd.getOptionValue("input")), "Template");
            Map<String, ModelGraph> graphs = new HashMap<String, ModelGraph>();
            for (TableVertex v : modelGraphTemplate.getAllVertices()) {
                String modelName = map.get(v.getLogicalName());
                ModelGraph get = graphs.get(modelName);
                if (get != null) {
                    get.addVertex(v.clone());
                } else if (modelName != null) {
                    ModelGraph n = modelGraphTemplate.getblankGraph();
                    n.setModelName(modelName);
                    n.addVertex(v.clone());
                    graphs.put(modelName, n);
                }
            }
            
            for (ModelGraph graph : graphs.values()) {
                graph.writer(outFileDir.getAbsolutePath()+File.separatorChar+graph.getModelName()+".bim");
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("SplitModel", options);
            System.exit(1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            formatter.printHelp("SplitModel", options);
            System.exit(1);
        }

    }

}
