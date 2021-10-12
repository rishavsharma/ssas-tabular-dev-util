/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author rishav.sharma
 */
public class Model {

    public enum Relation {
        FROM_PHYSICAL(true),
        fromTable(true),
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

        public boolean composite = false;

        Relation() {
            this.composite = false;
        }

        Relation(boolean composite) {
            this.composite = composite;
        }
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

    public enum Models {
        MODEL_NAME(true),
        MODEL_MERGE_NAME,
        NEW_MODEL_NAME;

        public boolean composite = false;

        Models() {
            this.composite = false;
        }

        Models(boolean composite) {
            this.composite = composite;
        }
    }

    public enum AllRelation {
        MODEL_NAME(true),
        FROM_PHYSICAL,
        fromTable(true),
        fromColumn(true),
        TO_PHYSICAL,
        toTable(true),
        toColumn(true),
        isActive,
        fromCardinality,
        toCardinality,
        crossFilteringBehavior,
        relyOnReferentialIntegrity,
        securityFilteringBehavior,
        joinOnDateBehavior;

        public boolean composite = false;

        AllRelation() {
            this.composite = false;
        }

        AllRelation(boolean composite) {
            this.composite = composite;
        }
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

    public enum AliasTable {
        MODEL_NAME(true),
        PHYSICAL_TABLE,
        TABLE_NAME(true);
        public boolean composite = false;

        AliasTable() {
            this.composite = false;
        }

        AliasTable(boolean composite) {
            this.composite = composite;
        }
    }

    public enum HierarchyLevel {
        MODEL_NAME(true),
        TABLE_NAME(true),
        HIERARCHY_NAME(true),
        NEW_HIERARCHY_NAME,
        name(true),
        ordinal,
        column;

        private final static Set<String> _MODEL = new HashSet<>();
        public boolean composite = false;

        HierarchyLevel() {
            this.composite = false;
        }

        HierarchyLevel(boolean composite) {
            this.composite = composite;
        }

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
        MODEL_NAME(true),
        PHYSICAL_TABLE,
        TABLE_NAME(true),
        COLUMN_TYPE,
        type,
        name(true),
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
        public boolean composite = false;

        DerivedColumn() {
            this.composite = false;
        }

        DerivedColumn(boolean composite) {
            this.composite = composite;
        }

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
        MODEL_NAME(true),
        TABLE_TYPE,
        PHYSICAL_TABLE,
        TABLE_NAME(true),
        NEW_TABLE_NAME,
        dataCategory,
        isHidden,
        description,
        isPrivate;
        private final static Set<String> _MODEL = new HashSet<>();
        static {
            for (Enum c : values()) {
                if (Character.isLowerCase(c.toString().codePointAt(0))) {
                    _MODEL.add(c.toString());
                }
            }
        }
        public boolean composite = false;

        RenameTable() {
            this.composite = false;
        }

        RenameTable(boolean composite) {
            this.composite = composite;
        }
        
        public static Set<String> getModelValues() {
            return _MODEL;
        }
        
        public static String[] getStringValues() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }
        
    }

    public enum RenameColumn {
        MODEL_NAME(true),
        PHYSICAL_TABLE,
        TABLE_NAME(true),
        COLUMN_TYPE,
        PHYSICAL_COLUMN,
        COLUMN_NAME(true),
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
        public boolean composite = false;

        RenameColumn() {
            this.composite = false;
        }

        RenameColumn(boolean composite) {
            this.composite = composite;
        }

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
        isNullable,
        expression;

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

    public enum Perspectives {
        PERSPECTIVE_NAME(true),
        TABLE_NAME(true),
        ELEMENT_TYPE(true),
        ELEMENT_NAME(true);
        
        public boolean composite = false;

        Perspectives() {
            this.composite = false;
        }

        Perspectives(boolean composite) {
            this.composite = composite;
        }
    }
    
    public enum ElementType {
        TABLES("tables"),
        COLUMNS("columns"),
        MEAUSURES("measures"),
        HIERARCHIES("hierarchies");
        
        public String value = "";

        ElementType(String value) {
            this.value = value;
        }
        
        @Override
        public String toString(){
            return this.value;
        }
    }
        
    public enum LineageColumn {
        MODEL_NAME,
        TABLE_NAME,
        COLUMN_TYPE,
        TERM_NAME,
        FORMULA,
        SSAS_TABLE,
        SSAS_COLUMN;

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

    public enum ExcelSheets {
        _MODEL_NAMES(Model.Models.class),
        _ALIAS_TABLES(Model.AliasTable.class),
        _HIERARCHIES(Model.HierarchyLevel.class),
        _DERIVED_COLUMNS(Model.DerivedColumn.class),
        _RENAME_TABLES(Model.RenameTable.class),
        _RENAME_COLUMNS(Model.RenameColumn.class),
        _RELATIONSHIPS(Model.AllRelation.class),
        _TABLE_SCHEMAS(Model.TableSchema.class),
        _PERSPECTIVES(Model.Perspectives.class);
        
        public Class<? extends Enum> classz;
        
        ExcelSheets(Class<? extends Enum> classz){
            this.classz=classz;
        }
        
        public static String getSheetNameFromClass(Class<? extends Enum> classz){
            for (ExcelSheets excelSheet : ExcelSheets.values()) {
                if(excelSheet.classz.getName().equals(classz.getName())){
                    return excelSheet.toString();
                }
            }
            
            return "";
        }
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
        __INFO,
        __KEY,
        __HASH,
        __DIFF,
        __ROWNUM,
        __WORKBOOK_NAME
    }
    
    public enum MergeMetaData {
        __ORIGINAL,
        __INSERT,
        __UPDATE,
        __DELETE,
        __OP_TYPE,
        __CONFLICT,
        __ROWCOMENT
    }
    
    public enum Comments {
        _COMMENTS
    }

    public static HashMap<String, Set<Integer>> getSheetKeys() {
        HashMap<String, Set<Integer>> retSet = new HashMap<>();
        Set<Integer> modelsOrdinals = Arrays.stream(Models.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._MODEL_NAMES.toString(), modelsOrdinals);
        
        Set<Integer> aliasOrdinals = Arrays.stream(AliasTable.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._ALIAS_TABLES.toString(), aliasOrdinals);
        
        Set<Integer> hierOrdinals = Arrays.stream(HierarchyLevel.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._HIERARCHIES.toString(), hierOrdinals);
        
        Set<Integer> derivedOrdinals = Arrays.stream(DerivedColumn.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._DERIVED_COLUMNS.toString(), derivedOrdinals);
        
        Set<Integer> renameTableOrdinals = Arrays.stream(RenameTable.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._RENAME_TABLES.toString(), renameTableOrdinals);
        
        Set<Integer> renameColumnOrdinals = Arrays.stream(RenameColumn.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._RENAME_COLUMNS.toString(), renameColumnOrdinals);
        
        Set<Integer> relationColumnOrdinals = Arrays.stream(AllRelation.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._RELATIONSHIPS.toString(), relationColumnOrdinals);
        
        Set<Integer> perspectiveColumnOrdinals = Arrays.stream(Perspectives.values())
                .filter(x -> x.composite == true)
                .map(x -> x.ordinal())
                .collect(Collectors.toSet());
        retSet.put(ExcelSheets._PERSPECTIVES.toString(), perspectiveColumnOrdinals);
        
        return retSet;
    }

    public static void main(String[] args) {
        
        System.out.println(Model.ExcelSheets.valueOf("_MODEL_NAMES").toString());
    }

}
