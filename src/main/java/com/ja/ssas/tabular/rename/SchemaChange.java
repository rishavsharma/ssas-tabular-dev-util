/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.rename;

/**
 *
 * @author rishav.sharma
 */
import com.ja.ssas.tabular.common.LogCountHandler;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class SchemaChange {

    private final String pattern = "[\\\\]?[\\\"]?[SUPsup][0-9]{2}[^\".\\n]*[\\\"]?(\\.[\\\\]?[\\\"]?[^ \\\"\\.\\n;<]*[\\\\]?[\\\"]?)?";
    private final Pattern p = Pattern.compile(pattern);
    private final JSONObject table;
    private Map<String, String> schemaMap;
    private static final Logger logger = LogCountHandler.getInstance().getLogger("SchemaChange");
    private String from;
    private String to;
    public SchemaChange(JSONObject table, Map<String,String> schemaMap) {
        this.table = table;
        this.schemaMap=schemaMap;
    }
    
     public SchemaChange(JSONObject table, String from, String to) {
        this.table = table;
        this.from = from;
        this.to=to;
    }

    public void renameSchema() {
        //String tableName = table.getString("name");
        if (table.has("partitions")) {
            JSONArray partitions = table.getJSONArray("partitions");
            findPartitions(partitions);
        }
        if (table.has("annotations")) {
            JSONArray annotations = table.getJSONArray("annotations");
            findAnnotation(annotations);
        }
    }

    private String find(String text) {
        return text.replaceAll(from, to);
    }
    
    private String findMulti(String text) {
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String b = m.group().replaceAll("\"", "");
            String getNewName = schemaMap.get(b.toUpperCase());
            if(getNewName==null){
                logger.log(Level.WARNING, "Schema Change value not found:{0}",b);
                getNewName=b;
            }
            if (getNewName.contains(".")) {
                getNewName = "\"" + getNewName.replaceAll("\\.", "\".\"") + "\"";
            }
                       
            m.appendReplacement(sb,getNewName);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void findPartitions(JSONArray partitions) {
        for (int j = 0; j < partitions.length(); j++) {
            JSONObject partition = partitions.getJSONObject(j);
            JSONObject source = partition.getJSONObject("source");
            Object query = source.get("query");
            Object ret = findValue(query);
            source.put("query", ret);
            if (partition.has("annotations")) {
                JSONArray annotations = partition.getJSONArray("annotations");
                findAnnotation(annotations);
            }
        }
    }

    private void findAnnotation(JSONArray annotations) {
        for (int i = 0; i < annotations.length(); i++) {
            JSONObject annotation = annotations.getJSONObject(i);
            Object ret = findValue(annotation.get("value"));
            annotation.put("value", ret);
        }
    }

    private Object findValue(Object value) {
        if (value instanceof JSONArray) {
            JSONArray vals = (JSONArray) value;
            JSONArray retVals = new JSONArray();
            for (int i = 0; i < vals.length(); i++) {
                String val = find(vals.getString(i));
                retVals.put(val);
            }
            return retVals;
        } else {
            String sVal = (String) value;
            sVal = find(sVal);
            return sVal;
        }
    }

    public void main(String[] args) throws IOException {

    }

}
