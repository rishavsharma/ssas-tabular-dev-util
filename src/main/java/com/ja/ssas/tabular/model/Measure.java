
package com.ja.ssas.tabular.model;

import java.util.List;


/**
 * Measure object of Tabular Object Model (TOM)
 * 
 */
public class Measure {

    private String name;
    private Object description;
    private Object expression;
    private String formatString;
    private Boolean isHidden;
    private Boolean isSimpleMeasure;
    private String displayFolder;
    /**
     * KPI object of Tabular Object Model (TOM)
     * 
     */
    private Kpi kpi;
    private List<Annotation> annotations = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getExpression() {
        return expression;
    }

    public void setExpression(Object expression) {
        this.expression = expression;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Boolean getIsSimpleMeasure() {
        return isSimpleMeasure;
    }

    public void setIsSimpleMeasure(Boolean isSimpleMeasure) {
        this.isSimpleMeasure = isSimpleMeasure;
    }

    public String getDisplayFolder() {
        return displayFolder;
    }

    public void setDisplayFolder(String displayFolder) {
        this.displayFolder = displayFolder;
    }

    /**
     * KPI object of Tabular Object Model (TOM)
     * 
     */
    public Kpi getKpi() {
        return kpi;
    }

    /**
     * KPI object of Tabular Object Model (TOM)
     * 
     */
    public void setKpi(Kpi kpi) {
        this.kpi = kpi;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

}
