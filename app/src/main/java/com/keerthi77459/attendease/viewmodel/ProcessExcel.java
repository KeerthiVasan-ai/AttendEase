package com.keerthi77459.attendease.viewmodel;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import com.keerthi77459.attendease.db.DbHelper;
import com.keerthi77459.attendease.utils.Utils;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;

public class ProcessExcel {

    Context context;
    DbHelper dbHelper;
    Utils utils = new Utils();

    public ProcessExcel(Context context) {
        this.context = context;
        if (context != null) {
            dbHelper = new DbHelper(context);
        } else {
            System.out.println("Error");
        }
    }


    public long readXLSXFile(final Uri file,final String tableName, String degreeText, String classText, String yearText) {
        final long[] result = {0};

        AsyncTask.execute(() -> {
            try {
                XSSFWorkbook workbook;
                try (InputStream inputStream = context.getContentResolver().openInputStream(file)) {
                    workbook = new XSSFWorkbook(inputStream);
                }
                XSSFSheet sheet = workbook.getSheetAt(0);
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                int rowsCount = sheet.getPhysicalNumberOfRows();
                if (rowsCount > 0) {
                    for (int r = 1; r < rowsCount; r++) {
                        Row row = sheet.getRow(r);
                        if (row.getPhysicalNumberOfCells() == 4) {
                            String A = getCellData(row, 0, formulaEvaluator);
                            String B = getCellData(row, 1, formulaEvaluator);
                            String C = getCellData(row, 2, formulaEvaluator);
                            String D = getCellData(row, 3, formulaEvaluator);

                            System.out.println(A);
                            System.out.println(B);
                            System.out.println(C);
                            System.out.println(D);


                            try {
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues contentValue = new ContentValues();
                                contentValue.put("rollNo", A);
                                contentValue.put("name", B);
                                contentValue.put("degree", degreeText);
                                contentValue.put("class", classText);
                                contentValue.put("year", yearText);
                                contentValue.put("phoneNumber", C);
                                contentValue.put("mode", D);
                                db.insert(tableName, null, contentValue);
//                                ContentValues contentValue2 = new ContentValues();
//                                contentValue2.put("rollNo", A);
//                                result[0] = db.insert(utils.getTABLE_ATTENDANCE_DETAIL(), null, contentValue2);
                            } catch (SQLiteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return result[0];
    }


    private long readXLSFile(final Uri file,final String tableName, String degreeText, String classText, String yearText) {
        final long[] result = {0};
        AsyncTask.execute(() -> {
            try {
                HSSFWorkbook workbook;
                try (InputStream inputStream = context.getContentResolver().openInputStream(file)) {
                    workbook = new HSSFWorkbook(inputStream);
                }
                HSSFSheet sheet = workbook.getSheetAt(0);
                HSSFFormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                int rowsCount = sheet.getPhysicalNumberOfRows();
                if (rowsCount > 0) {
                    for (int r = 1; r < rowsCount; r++) {
                        HSSFRow row = sheet.getRow(r);
                        if (row.getPhysicalNumberOfCells() == 4) {
                            String A = getCellData(row, 0, formulaEvaluator);
                            String B = getCellData(row, 1, formulaEvaluator);
                            String C = getCellData(row, 2, formulaEvaluator);
                            String D = getCellData(row, 3, formulaEvaluator);

                            System.out.println(A);
                            System.out.println(B);
                            System.out.println(C);
                            System.out.println(D);

                            try {
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues contentValue = new ContentValues();
                                contentValue.put("rollNo", A);
                                contentValue.put("name", B);
                                contentValue.put("degree", degreeText);
                                contentValue.put("class", classText);
                                contentValue.put("year", yearText);
                                contentValue.put("mode",D);
                                contentValue.put("phoneNumber", C);
                                db.insert(tableName, null, contentValue);
                            } catch (SQLiteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return result[0];
    }


    private String getCellData(Row row, int cellPosition, FormulaEvaluator formulaEvaluator) {
        String value = "";
        Cell cell = row.getCell(cellPosition);
        switch (cell.getCellType()) {

            case Cell.CELL_TYPE_BOOLEAN:
                return value + cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return value + BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case Cell.CELL_TYPE_STRING:
                return value + cell.getStringCellValue();
            default:
                return value;
        }
    }

    public boolean validateFile(
            Uri fileName,
            String tableName,
            String extension,
            String degreeText,
            String classText,
            String yearText) {
        System.out.println(extension);
        if (fileName != null) {
            if (Objects.equals(extension, "xlsx")) {
                Long done = readXLSXFile(fileName,tableName, degreeText, classText, yearText);
                return true;
            } else if (Objects.equals(extension, "xls")) {
                Long done = readXLSFile(fileName,tableName, degreeText, classText, yearText);
                return true;
            }

        } else {
            return false;
        }
        return false;
    }
}