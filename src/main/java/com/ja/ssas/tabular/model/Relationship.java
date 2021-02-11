package com.ja.ssas.tabular.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SingleColumnRelationship object of Tabular Object Model (TOM)
 *
 */
public class Relationship {

    private String name;
    private Boolean isActive;
    private Relationship.Type type;
    private Relationship.CrossFilteringBehavior crossFilteringBehavior;
    private Relationship.JoinOnDateBehavior joinOnDateBehavior;
    private Boolean relyOnReferentialIntegrity;
    private Relationship.SecurityFilteringBehavior securityFilteringBehavior;
    private Relationship.FromCardinality fromCardinality;
    private Relationship.ToCardinality toCardinality;
    private String fromColumn;
    private String fromTable;
    private String toColumn;
    private String toTable;
    private List<Annotation> annotations = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Relationship.Type getType() {
        return type;
    }

    public void setType(Relationship.Type type) {
        this.type = type;
    }

    public Relationship.CrossFilteringBehavior getCrossFilteringBehavior() {
        return crossFilteringBehavior;
    }

    public void setCrossFilteringBehavior(Relationship.CrossFilteringBehavior crossFilteringBehavior) {
        this.crossFilteringBehavior = crossFilteringBehavior;
    }

    public Relationship.JoinOnDateBehavior getJoinOnDateBehavior() {
        return joinOnDateBehavior;
    }

    public void setJoinOnDateBehavior(Relationship.JoinOnDateBehavior joinOnDateBehavior) {
        this.joinOnDateBehavior = joinOnDateBehavior;
    }

    public Boolean getRelyOnReferentialIntegrity() {
        return relyOnReferentialIntegrity;
    }

    public void setRelyOnReferentialIntegrity(Boolean relyOnReferentialIntegrity) {
        this.relyOnReferentialIntegrity = relyOnReferentialIntegrity;
    }

    public Relationship.SecurityFilteringBehavior getSecurityFilteringBehavior() {
        return securityFilteringBehavior;
    }

    public void setSecurityFilteringBehavior(Relationship.SecurityFilteringBehavior securityFilteringBehavior) {
        this.securityFilteringBehavior = securityFilteringBehavior;
    }

    public Relationship.FromCardinality getFromCardinality() {
        return fromCardinality;
    }

    public void setFromCardinality(Relationship.FromCardinality fromCardinality) {
        this.fromCardinality = fromCardinality;
    }

    public Relationship.ToCardinality getToCardinality() {
        return toCardinality;
    }

    public void setToCardinality(Relationship.ToCardinality toCardinality) {
        this.toCardinality = toCardinality;
    }

    public String getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(String fromColumn) {
        this.fromColumn = fromColumn;
    }

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable(String fromTable) {
        this.fromTable = fromTable;
    }

    public String getToColumn() {
        return toColumn;
    }

    public void setToColumn(String toColumn) {
        this.toColumn = toColumn;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable(String toTable) {
        this.toTable = toTable;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public enum CrossFilteringBehavior {

        ONE_DIRECTION("oneDirection"),
        BOTH_DIRECTIONS("bothDirections"),
        AUTOMATIC("automatic");
        private final String value;
        private final static Map<String, Relationship.CrossFilteringBehavior> CONSTANTS = new HashMap<String, Relationship.CrossFilteringBehavior>();

        static {
            for (Relationship.CrossFilteringBehavior c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CrossFilteringBehavior(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Relationship.CrossFilteringBehavior fromValue(String value) {
            Relationship.CrossFilteringBehavior constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum FromCardinality {

        NONE("none"),
        ONE("one"),
        MANY("many");
        private final String value;
        private final static Map<String, Relationship.FromCardinality> CONSTANTS = new HashMap<String, Relationship.FromCardinality>();

        static {
            for (Relationship.FromCardinality c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FromCardinality(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Relationship.FromCardinality fromValue(String value) {
            Relationship.FromCardinality constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum JoinOnDateBehavior {

        DATE_AND_TIME("dateAndTime"),
        DATE_PART_ONLY("datePartOnly");
        private final String value;
        private final static Map<String, Relationship.JoinOnDateBehavior> CONSTANTS = new HashMap<String, Relationship.JoinOnDateBehavior>();

        static {
            for (Relationship.JoinOnDateBehavior c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        JoinOnDateBehavior(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Relationship.JoinOnDateBehavior fromValue(String value) {
            Relationship.JoinOnDateBehavior constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum SecurityFilteringBehavior {

        ONE_DIRECTION("oneDirection"),
        BOTH_DIRECTIONS("bothDirections");
        private final String value;
        private final static Map<String, Relationship.SecurityFilteringBehavior> CONSTANTS = new HashMap<String, Relationship.SecurityFilteringBehavior>();

        static {
            for (Relationship.SecurityFilteringBehavior c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SecurityFilteringBehavior(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Relationship.SecurityFilteringBehavior fromValue(String value) {
            Relationship.SecurityFilteringBehavior constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum ToCardinality {

        NONE("none"),
        ONE("one"),
        MANY("many");
        private final String value;
        private final static Map<String, Relationship.ToCardinality> CONSTANTS = new HashMap<String, Relationship.ToCardinality>();

        static {
            for (Relationship.ToCardinality c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ToCardinality(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static Relationship.ToCardinality fromValue(String value) {
            Relationship.ToCardinality constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum Type {

        SINGLE_COLUMN("singleColumn");
        private final String value;
        private final static Map<String, Relationship.Type> CONSTANTS = new HashMap<String, Relationship.Type>();

        static {
            for (Relationship.Type c : values()) {
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

        public static Relationship.Type fromValue(String value) {
            Relationship.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
