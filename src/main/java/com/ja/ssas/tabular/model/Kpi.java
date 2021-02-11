
package com.ja.ssas.tabular.model;

import java.util.List;


/**
 * KPI object of Tabular Object Model (TOM)
 * 
 */
public class Kpi {

    private Object description;
    private String targetDescription;
    private Object targetExpression;
    private String targetFormatString;
    private String statusGraphic;
    private String statusDescription;
    private Object statusExpression;
    private String trendGraphic;
    private String trendDescription;
    private Object trendExpression;
    private List<Annotation> annotations = null;

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    public Object getTargetExpression() {
        return targetExpression;
    }

    public void setTargetExpression(Object targetExpression) {
        this.targetExpression = targetExpression;
    }

    public String getTargetFormatString() {
        return targetFormatString;
    }

    public void setTargetFormatString(String targetFormatString) {
        this.targetFormatString = targetFormatString;
    }

    public String getStatusGraphic() {
        return statusGraphic;
    }

    public void setStatusGraphic(String statusGraphic) {
        this.statusGraphic = statusGraphic;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Object getStatusExpression() {
        return statusExpression;
    }

    public void setStatusExpression(Object statusExpression) {
        this.statusExpression = statusExpression;
    }

    public String getTrendGraphic() {
        return trendGraphic;
    }

    public void setTrendGraphic(String trendGraphic) {
        this.trendGraphic = trendGraphic;
    }

    public String getTrendDescription() {
        return trendDescription;
    }

    public void setTrendDescription(String trendDescription) {
        this.trendDescription = trendDescription;
    }

    public Object getTrendExpression() {
        return trendExpression;
    }

    public void setTrendExpression(Object trendExpression) {
        this.trendExpression = trendExpression;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

}
