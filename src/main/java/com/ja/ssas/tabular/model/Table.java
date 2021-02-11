
package com.ja.ssas.tabular.model;

import java.util.List;


/**
 * Table object of Tabular Object Model (TOM)
 * 
 */
public class Table {

    private String name;
    private String dataCategory;
    private Object description;
    private Boolean isHidden;
    private Object partitions;
    private List<Column> columns = null;
    private List<Measure> measures = null;
    private List<Hierarchy> hierarchies = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Object getPartitions() {
        return partitions;
    }

    public void setPartitions(Object partitions) {
        this.partitions = partitions;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    public List<Hierarchy> getHierarchies() {
        return hierarchies;
    }

    public void setHierarchies(List<Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
    }

}
