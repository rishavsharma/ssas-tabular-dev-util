/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rishav.sharma
 */
public class Model {

    public enum Relation {
        FROM_PHYSICAL,
        fromTable,
        fromColumn,
        TO_PHYSICAL,
        toTable,
        toColumn,
        isActive,
        fromCardinality,
        toCardinality,
        crossFilteringBehavior,
        relyOnReferentialIntegrity,
        securityFilteringBehavior,
        joinOnDateBehavior;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }

    }

    public enum AllRelation {
        MODEL_NAME,
        FROM_PHYSICAL,
        fromTable,
        fromColumn,
        TO_PHYSICAL,
        toTable,
        toColumn,
        isActive,
        fromCardinality,
        toCardinality,
        crossFilteringBehavior,
        relyOnReferentialIntegrity,
        securityFilteringBehavior,
        joinOnDateBehavior;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }

    }

    public enum RenameColumn {
        MODEL_NAME,
        PHYSICAL_TABLE,
        TABLE_NAME,
        COLUMN_TYPE,
        PHYSICAL_COLUMN,
        COLUMN_NAME,
        NEW_COLUMN_NAME,
        isHidden,
        formatString,
        displayFolder,
        summarizeBy,
        dataType,
        sourceProviderType,
        isKey,
        description,
        sortByColumn,
        isNullable;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static Set<String> getModelValues() {
            return _MODEL;
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }
    }
    
    public enum RenameColumnExp {
        MODEL_NAME,
        PHYSICAL_TABLE,
        TABLE_NAME,
        COLUMN_TYPE,
        PHYSICAL_COLUMN,
        COLUMN_NAME,
        NEW_COLUMN_NAME,
        isHidden,
        formatString,
        displayFolder,
        summarizeBy,
        dataType,
        sourceProviderType,
        isKey,
        description,
        sortByColumn,
        expression,
        isNullable;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static Set<String> getModelValues() {
            return _MODEL;
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }
    }

    public enum RenameTable {
        MODEL_NAME,
        TABLE_TYPE,
        PHYSICAL_TABLE,
        TABLE_NAME,
        NEW_TABLE_NAME,
        dataCategory,
        isHidden,
        description
    }

    public enum RenameSchema {
        MODEL_NAME,
        TABLE_NAME,
        PHYSICAL_TABLE,
        SCHEMA_NAME,
        NEW_SCHEMA_NAME
    }

    public enum ColumnType {
        MEASURE,
        COLUMN,
        CALCULATED_COLUMN
    }

    public enum TableType {
        TABLE,
        HIERARCHY,
        ALIAS
    }

    public enum Models {
        MODEL_NAME,
        MODEL_MERGE_NAME,
        NEW_MODEL_NAME
    }

    public enum AliasTable {
        MODEL_NAME,
        PHYSICAL_TABLE,
        TABLE_NAME
    }

    public enum HierarchyLevel {
        MODEL_NAME,
        TABLE_NAME,
        HIERARCHY_NAME,
        NEW_HIERARCHY_NAME,
        name,
        ordinal,
        column;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static Set<String> getModelValues() {
            return _MODEL;
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }
    }

    public enum DerivedColumn {
        MODEL_NAME,
        PHYSICAL_TABLE,
        TABLE_NAME,
        COLUMN_TYPE,
        type,
        name,
        expression,
        dataType,
        isHidden,
        formatString,
        displayFolder,
        isDataTypeInferred,
        description,
        sortByColumn,
        summarizeBy;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static Set<String> getModelValues() {
            return _MODEL;
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }
    }
    
    public enum LineageColumn {
        MODEL_NAME,
        TABLE_NAME,
        COLUMN_TYPE,
        TERM_NAME,
        FORMULA,
        DB_TABLE,
        DB_COLUMN,
        REFERENCE;

        private final static Set<String> _MODEL = new HashSet<>();

        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }

        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public static Set<String> getModelValues() {
            return _MODEL;
        }

        public static String[] getModelValueArray() {
            return _MODEL.stream().toArray(String[]::new);
        }
    }

    public enum TableSchema {
        MODEL_NAME,
        PHYSICAL_TABLE,
        TABLE_NAME,
        SCHEMA_NAME,
        NEW_SCHEMA_NAME
    }

    public enum ExcelSheets {
        _MODEL_NAMES,
        _ALIAS_TABLES,
        _HIERARCHIES,
        _DERIVED_COLUMNS,
        _RENAME_TABLES,
        _RENAME_COLUMNS,
        _RELATIONSHIPS,
        _TABLE_SCHEMAS

    }

    public enum TableAnnotations {
        _TM_ExtProp_DbTableName,
        _TM_ExtProp_DbSchemaName
    }

    public enum ExcelMetaData {
        __ERROR_LEVEL,
        __ERROR_COMMENT,
        __ERROR,
        __WARNING,
        __INFO
    }
}
