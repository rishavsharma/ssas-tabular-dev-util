/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.test;

import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author venris
 */
public class OptionCheck {

    /**
     * @param args the command line arguments
     */
    
    OptionCheck(String[] args){
        Options options = new Options();
        Option option = new Option("a", "add");
        option.setArgs(Option.UNLIMITED_VALUES);
        //option.setValueSeparator(',');
        options.addOption(option);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            System.out.println(Arrays.asList(cmd.getOptionValues("a")));
        }catch(Exception e){
            e.printStackTrace();
            formatter.printHelp("ExcelConfigDiff", options);
        }
    }
    public static void main(String[] args) {
        String r = "-a ar -a ff -a sds";
        new OptionCheck((r.split(" ")));
    }
    
}
