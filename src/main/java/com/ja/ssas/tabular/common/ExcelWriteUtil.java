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

    public ExcelWriteUtil(String fileName) {
        try {
            workbook = Workbook.createWorkbook(new File(fileName));
            errorFormat = new WritableCellFormat();
            infoFormat = new WritableCellFormat();
            warnFormat = new WritableCellFormat();
            errorFormat.setBackground(Colour.RED);
            infoFormat.setBackground(Colour.LIGHT_GREEN);
            warnFormat.setBackground(Colour.LIGHT_ORANGE);
        } catch (Exception e) {
            logger.log(Level.SEVERE, fileName, e);
        }
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
        if(level.equals(Model.ExcelMetaData.__INFO.toString())){
            cell.setCellFormat(infoFormat);
        }else if(level.equals(Model.ExcelMetaData.__WARNING.toString())){
            cell.setCellFormat(warnFormat);
        }else{
            cell.setCellFormat(errorFormat);
        }
        if (!errorString.isEmpty() && cell.getColumn() == 0) {
            WritableCellFeatures errorComment = new WritableCellFeatures();
            errorComment.setComment(errorString);
            cell.setCellFeatures(errorComment);
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
            for (int i = 0; i < data.length(); i++) {
                boolean errorStatus = false;
                String errorString = "";
                String errorLevel = "";
                JSONObject row = data.getJSONObject(i);
                if (row.has(Model.ExcelMetaData.__ERROR_LEVEL.toString())) {
                    errorString = row.optString(Model.ExcelMetaData.__ERROR_COMMENT.toString(), "");
                    errorLevel=row.getString(Model.ExcelMetaData.__ERROR_LEVEL.toString());
                    errorStatus = true;
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
                    if (errorStatus) {
                        formatCell(cell, errorLevel, errorString);
                    }
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
