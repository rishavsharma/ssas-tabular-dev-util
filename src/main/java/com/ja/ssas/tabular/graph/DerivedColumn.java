/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.graph;

import com.ja.ssas.tabular.common.LogCountHandler;
import com.ja.ssas.tabular.common.Model;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class DerivedColumn {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("DerivedColumn");
    private final JSONObject column;

    private final String type;
    private final String name;
    private final String tableName;

    public DerivedColumn(JSONObject column, String type, String name, String tableName) {
        this.column = column;
        this.type = type;
        this.name = name;
        this.tableName = tableName;
    }

    public JSONObject getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public String getTableName() {
        return tableName;
    }

    public void setColumn(JSONObject eColumn) {
        for (String key : Model.DerivedColumn.getModelValueArray()) {
            if (eColumn.has(key)) {
                Object value = eColumn.get(key);
                column.put(key, value);
                logger.log(Level.FINEST, "Setting value for column {0} key {1}:{2}",new Object[]{name,key,value.toString()});
            }
        }

    }
}
