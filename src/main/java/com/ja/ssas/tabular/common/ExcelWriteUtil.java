/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ja.ssas.tabular.common;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rishav.sharma
 */
public class ExcelWriteUtil {

    private static final Logger logger = LogCountHandler.getInstance().getLogger("ExcelWriteUtil");
    private WritableWorkbook workbook = null;
    private WritableCellFormat errorFormat;
    private WritableCellFormat infoFormat;
    private WritableCellFormat warnFormat;
    private WritableCellFormat diffFormat;
    private WritableCellFormat originalFormat;
    private boolean writeComment = false;

    public ExcelWriteUtil(String fileName) {
        try {
            workbook = Workbook.createWorkbook(new File(fileName));
            errorFormat = new WritableCellFormat();
            infoFormat = new WritableCellFormat();
            warnFormat = new WritableCellFormat();
            diffFormat = new WritableCellFormat();
            originalFormat = new WritableCellFormat();
            errorFormat.setBackground(Colour.RED);
            infoFormat.setBackground(Colour.LIGHT_GREEN);
            warnFormat.setBackground(Colour.LIGHT_ORANGE);
            diffFormat.setBackground(Colour.YELLOW);
            originalFormat.setBackground(Colour.GREY_40_PERCENT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, fileName, e);
        }
    }

    public void setWriteComment(boolean writeComment) {
        this.writeComment = writeComment;
    }

    private void sheetAutoFitColumns(WritableSheet sheet) {
        sheet.getSettings().setVerticalFreeze(1);
        for (int i = 0; i < sheet.getColumns(); i++) {
            Cell[] cells = sheet.getColumn(i);
            int longestStrLen = -1;

            if (cells.length == 0) {
                continue;
            }

            /* Find the widest cell in the column. */
            for (int j = 0; j < cells.length; j++) {
                if (cells[j].getContents().length() > longestStrLen) {
                    String str = cells[j].getContents();
                    if (str == null || str.isEmpty()) {
                        continue;
                    }
                    longestStrLen = str.trim().length();
                }
            }

            /* If not found, skip the column. */
            if (longestStrLen == -1) {
                continue;
            }

            /* If wider than the max width, crop width */
            if (longestStrLen > 255) {
                longestStrLen = 255;
            }

            CellView cv = sheet.getColumnView(i);
            cv.setSize(longestStrLen * 256 + 1000);
            /* Every character is 256 units wide, so scale it. */
            sheet.setColumnView(i, cv);
        }
    }

    private void formatCell(WritableCell cell, String level, String errorString) throws WriteException {
        if (level.equals(Model.ExcelMetaData.__INFO.toString())) {
            cell.setCellFormat(infoFormat);
        } else if (level.equals(Model.ExcelMetaData.__WARNING.toString())) {
            cell.setCellFormat(warnFormat);
        } else if (level.equals(Model.ExcelMetaData.__ERROR.toString())) {
            cell.setCellFormat(errorFormat);
        }
        if (!errorString.isEmpty() && cell.getColumn() == 0) {
            WritableCellFeatures errorComment = new WritableCellFeatures();
            errorComment.setComment(errorString);
            cell.setCellFeatures(errorComment);
        }

    }

    private void setComment(WritableCell cell, String commentString) {
        WritableCellFeatures comments = new WritableCellFeatures();
        comments.setComment(commentString);
        cell.setCellFeatures(comments);
    }

    private void formatCellDiff(WritableCell cell, String columnName, JSONObject row) throws WriteException {
        if (row.has(Model.MergeMetaData.__OP_TYPE.toString())) {
            String opStr = row.optString(Model.MergeMetaData.__OP_TYPE.toString(), "");
            String commentString = row.optString(Model.MergeMetaData.__ROWCOMENT.toString(), "");
            if (commentString.isEmpty()) {
                commentString = "<<No Value>>";
            }

            Model.MergeMetaData op = Model.MergeMetaData.valueOf(opStr);
            switch (op) {
                case __ORIGINAL:
                    if (cell.getColumn() == 0) {
                        setComment(cell, commentString);
                    }
                    cell.setCellFormat(originalFormat);
                    break;
                case __INSERT:
                    if (cell.getColumn() == 0) {
                        setComment(cell, commentString);
                    }
                    cell.setCellFormat(infoFormat);
                    break;
                case __UPDATE:
                    JSONObject __DIFF = row.getJSONObject(Model.ExcelMetaData.__DIFF.toString());
                    if (__DIFF.has(columnName)) {
                        String comment = __DIFF.optString(columnName, "No Value");
                        if (comment.length() == 0) {
                            comment = "No Value";
                        }
                        setComment(cell, comment);
                        cell.setCellFormat(diffFormat);
                    }
                    if (cell.getColumn() == 0) {
                        setComment(cell, commentString);
                        cell.setCellFormat(warnFormat);
                    }
                    break;
                case __DELETE:
                    cell.setCellFormat(errorFormat);
                    if (cell.getColumn() == 0) {
                        setComment(cell, commentString);
                    }
                    break;
                case __CONFLICT:
                    if (row.has(Model.ExcelMetaData.__DIFF.toString())) {
                        __DIFF = row.getJSONObject(Model.ExcelMetaData.__DIFF.toString());
                        if (__DIFF.has(columnName)) {
                            String comment = __DIFF.optString(columnName, "No Value");
                            if (comment.length() == 0) {
                                comment = "No Value";
                            }
                            setComment(cell, comment);
                            cell.setCellFormat(diffFormat);
                        }

                        if (cell.getColumn() == 0) {
                            setComment(cell, commentString);
                            cell.setCellFormat(errorFormat);
                        }

                    }
                    break;
                default:
                    logger.log(Level.SEVERE, "No operation found" + row.optString(Model.ExcelMetaData.__KEY.toString(), columnName));
            }
        }

    }

    public <E extends Enum<E>> void writeSheet(String sheet, int location, JSONArray data, Class<E> fileColumns) {
        try {
            WritableSheet sheetWrite = workbook.createSheet(sheet, location);

            Enum<E>[] colVal = fileColumns.getEnumConstants();
            for (int j = 0; j < colVal.length; j++) {
                String columnName = colVal[j].toString();
                sheetWrite.addCell(new Label(j, 0, columnName));
            }
            if (R.COMMENTS) {
                sheetWrite.addCell(new Label(colVal.length, 0, Model.Comments._COMMENTS.toString()));
            }
            for (int i = 0; i < data.length(); i++) {
                boolean errorStatus = false;
                boolean diffStatus = false;
                String errorString = "";
                String errorLevel = "";
                JSONObject row = data.getJSONObject(i);
                if (row.has(Model.ExcelMetaData.__ERROR_LEVEL.toString())) {
                    errorString = row.optString(Model.ExcelMetaData.__ERROR_COMMENT.toString(), "");
                    errorLevel = row.getString(Model.ExcelMetaData.__ERROR_LEVEL.toString());
                    errorStatus = true;
                }

                if (row.has(Model.MergeMetaData.__OP_TYPE.toString())) {
                    diffStatus = true;
                }

                for (int j = 0; j < colVal.length; j++) {
                    String columnName = colVal[j].toString();
                    String value = row.optString(columnName, "");
                    String dataType = EnumDataTypes.getDataType(columnName);
                    WritableCell cell = null;
                    if (EnumDataTypes.BOOLEAN.equals(dataType)) {
                        cell = new Label(j, i + 1, value);
                    } else if (EnumDataTypes.INT.equals(dataType)) {
                        cell = new jxl.write.Number(j, i + 1, Integer.parseInt(value));
                    } else if (EnumDataTypes.EXPRESSION.equals(dataType) && row.has(EnumDataTypes.EXPRESSION)) {
                        Object expression = row.get(columnName);
                        StringBuilder sb = new StringBuilder();
                        if (expression instanceof JSONArray) {
                            JSONArray expressionArray = (JSONArray) expression;
                            expressionArray.toList().forEach((exp) -> {
                                sb.append(exp).append("\r\n");
                            });
                        } else {
                            sb.append(value);
                        }
                        cell = new Label(j, i + 1, sb.toString().trim());
                    } else {
                        cell = new Label(j, i + 1, value);
                    }

                    sheetWrite.addCell(cell);
                    if (diffStatus) {
                        formatCellDiff(cell, columnName, row);
                    }
                    if (errorStatus) {
                        formatCell(cell, errorLevel, errorString);
                    }
                }
                if (R.COMMENTS) {
                    String value = row.optString(Model.Comments._COMMENTS.toString(), "");
                    WritableCell cell = new Label(colVal.length, i + 1, value);
                    sheetWrite.addCell(cell);
                }
            }
            sheetAutoFitColumns(sheetWrite);
        } catch (WriteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        if (workbook != null) {
            try {

                workbook.write();
                workbook.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (WriteException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

}
