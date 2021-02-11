package com.ja.ssas.tabular.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataColumn object of Tabular Object Model (TOM)
 *
 */
public class Column {

    private String name;
    private Column.DataType dataType;
    private String dataCategory;
    private String description;
    private Boolean isHidden;
    private Boolean isUnique;
    private Boolean isKey;
    private Boolean isNullable;
    private Column.Alignment alignment;
    private Integer tableDetailPosition;
    private Boolean isDefaultLabel;
    private Boolean isDefaultImage;
    private Column.SummarizeBy summarizeBy;
    private Column.Type type;
    private String formatString;
    private Boolean isAvailableInMdx;
    private Boolean keepUniqueRows;
    private Integer displayOrdinal;
    private String sourceProviderType;
    private String displayFolder;
    private String sourceColumn;
    private String sortByColumn;
    private List<Annotation> annotations = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Column.DataType getDataType() {
        return dataType;
    }

    public void setDataType(Column.DataType dataType) {
        this.dataType = dataType;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
    }

    public Boolean getIsKey() {
        return isKey;
    }

    public void setIsKey(Boolean isKey) {
        this.isKey = isKey;
    }

    public Boolean getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    public Column.Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Column.Alignment alignment) {
        this.alignment = alignment;
    }

    public Integer getTableDetailPosition() {
        return tableDetailPosition;
    }

    public void setTableDetailPosition(Integer tableDetailPosition) {
        this.tableDetailPosition = tableDetailPosition;
    }

    public Boolean getIsDefaultLabel() {
        return isDefaultLabel;
    }

    public void setIsDefaultLabel(Boolean isDefaultLabel) {
        this.isDefaultLabel = isDefaultLabel;
    }

    public Boolean getIsDefaultImage() {
        return isDefaultImage;
    }

    public void setIsDefaultImage(Boolean isDefaultImage) {
        this.isDefaultImage = isDefaultImage;
    }

    public Column.SummarizeBy getSummarizeBy() {
        return summarizeBy;
    }

    public void setSummarizeBy(Column.SummarizeBy summarizeBy) {
        this.summarizeBy = summarizeBy;
    }

    public Column.Type getType() {
        return type;
    }

    public void setType(Column.Type type) {
        this.type = type;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public Boolean getIsAvailableInMdx() {
        return isAvailableInMdx;
    }

    public void setIsAvailableInMdx(Boolean isAvailableInMdx) {
        this.isAvailableInMdx = isAvailableInMdx;
    }

    public Boolean getKeepUniqueRows() {
        return keepUniqueRows;
    }

    public void setKeepUniqueRows(Boolean keepUniqueRows) {
        this.keepUniqueRows = keepUniqueRows;
    }

    public Integer getDisplayOrdinal() {
        return displayOrdinal;
    }

    public void setDisplayOrdinal(Integer displayOrdinal) {
        this.displayOrdinal = displayOrdinal;
    }

    public String getSourceProviderType() {
        return sourceProviderType;
    }

    public void setSourceProviderType(String sourceProviderType) {
        this.sourceProviderType = sourceProviderType;
    }

    public String getDisplayFolder() {
        return displayFolder;
    }

    public void setDisplayFolder(String displayFolder) {
        this.displayFolder = displayFolder;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getSortByColumn() {
        return sortByColumn;
    }

    public void setSortByColumn(String sortByColumn) {
        this.sortByColumn = sortByColumn;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public enum Alignment {

        DEFAULT("default"),
        LEFT("left"),
        RIGHT("right"),
        CENTER("center");
        private final String value;
        private final static Map<String, Column.Alignment> CONSTANTS = new HashMap<String, Column.Alignment>();

        static {
            for (Column.Alignment c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Alignment(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Column.Alignment fromValue(String value) {
            Column.Alignment constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum DataType {

        AUTOMATIC("automatic"),
        STRING("string"),
        INT_64("int64"),
        DOUBLE("double"),
        DATE_TIME("dateTime"),
        DECIMAL("decimal"),
        BOOLEAN("boolean"),
        BINARY("binary"),
        UNKNOWN("unknown"),
        VARIANT("variant");
        private final String value;
        private final static Map<String, Column.DataType> CONSTANTS = new HashMap<String, Column.DataType>();

        static {
            for (Column.DataType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DataType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Column.DataType fromValue(String value) {
            Column.DataType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum SummarizeBy {

        DEFAULT("default"),
        NONE("none"),
        SUM("sum"),
        MIN("min"),
        MAX("max"),
        COUNT("count"),
        AVERAGE("average"),
        DISTINCT_COUNT("distinctCount");
        private final String value;
        private final static Map<String, Column.SummarizeBy> CONSTANTS = new HashMap<String, Column.SummarizeBy>();

        static {
            for (Column.SummarizeBy c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SummarizeBy(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Column.SummarizeBy fromValue(String value) {
            Column.SummarizeBy constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum Type {

        DATA("data"),
        CALCULATED("calculated"),
        ROW_NUMBER("rowNumber"),
        CALCULATED_TABLE_COLUMN("calculatedTableColumn");
        private final String value;
        private final static Map<String, Column.Type> CONSTANTS = new HashMap<String, Column.Type>();

        static {
            for (Column.Type c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Column.Type fromValue(String value) {
            Column.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
