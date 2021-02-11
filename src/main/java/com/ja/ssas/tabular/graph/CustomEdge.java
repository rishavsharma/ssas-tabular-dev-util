/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.graph;

import com.ja.ssas.tabular.common.Model;
import java.util.UUID;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class CustomEdge extends DefaultEdge {

    private static final long serialVersionUID = 1L;
    private boolean isActive;
    private JSONObject relation;
    private final String from;
    private final String to;
    private final String fromTable;
    private final String toTable;
    private String name;
    private JSONObject excelRelation;

    public CustomEdge(JSONObject relation) {
        super();
        this.isActive = relation.optBoolean(Model.Relation.isActive.toString(), true);
        this.from = relation.getString(Model.Relation.fromColumn.toString());
        this.to = relation.getString(Model.Relation.toColumn.toString());
        this.fromTable = relation.getString(Model.Relation.fromTable.toString());
        this.toTable = relation.getString(Model.Relation.toTable.toString());
        this.name = relation.optString("name","");
        if(this.name.equals("")){
            this.name=UUID.randomUUID().toString();
            relation.put("name", this.name);
        }
        this.relation = relation;
    }

    public TableVertex getSourceVertex() {
        return (TableVertex) super.getSource();
    }

    public TableVertex getTargetVertex() {
        return (TableVertex) super.getTarget();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFromTable() {
        return fromTable;
    }

    public String getToTable() {
        return toTable;
    }

    public JSONObject getExcelRelation() {
        return excelRelation;
    }

    public void setExcelRelation(JSONObject excelRelation) {
        this.excelRelation = excelRelation;
    }
    
    
    

    public JSONObject getRelation() {
        if (relation == null) {
            relation = new JSONObject();
            relation.put("name", UUID.randomUUID().toString());
            relation.put("fromTable", getSourceVertex().getLogicalName());
            relation.put("fromColumn", from);
            relation.put("toTable", getTargetVertex().getLogicalName());
            relation.put("toColumn", to);
            if (!isActive) {
                relation.put("isActive", false);
            }

        }
        return new JSONObject(relation.toString());
    }

    public void setRelation(JSONObject relation) {
        this.relation = relation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        relation.put(Model.Relation.isActive.toString(), isActive);
    }

    @Override
    public String toString() {
        return "(" + fromTable + "[" + from + "]=" + toTable + "[" + to + "])";
    }
}
