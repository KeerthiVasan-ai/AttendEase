package com.keerthi77459.attendease.ui;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.db.DbHelper;
import com.keerthi77459.attendease.model.ClassData;
import com.keerthi77459.attendease.utils.Utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GenerateReport extends AppCompatActivity {

    Button downloadReport;
    AutoCompleteTextView inpDegreeName;
    private String inpDegreeText, inpClassText, inpYearText, inpClassTypeText;
    DbHelper dbHelper;
    Utils utils;
    ArrayList<String> overallClassDetails;
    ClassData classData;
    int multiplicationConstant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        Resources resource = getResources();
        dbHelper = new DbHelper(this);
        classData = new ClassData(this);
        utils = new Utils();

        overallClassDetails = classData.mergedClassDetails();

        inpDegreeName = findViewById(R.id.inpDegreeName);
        downloadReport = findViewById(R.id.downloadReport);

        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, overallClassDetails);

        inpDegreeName.setAdapter(degreeAdapter);


        inpDegreeName.setOnItemClickListener((adapterView, view, i, l) -> {
            inpDegreeText = inpDegreeName.getText().toString().split("-")[0];
            inpClassText = inpDegreeName.getText().toString().split("-")[1];
            inpYearText = inpDegreeName.getText().toString().split("-")[2];
            inpClassTypeText = inpDegreeName.getText().toString().split("-")[3];

            if (Objects.equals(inpClassTypeText, resource.getStringArray(R.array.class_type)[0])) {
                multiplicationConstant = 1;
            } else if (Objects.equals(inpClassTypeText, resource.getStringArray(R.array.class_type)[1])) {
                multiplicationConstant = 3;
            }
            Log.d("Multiplication Constant", String.valueOf(multiplicationConstant));
        });

        downloadReport.setOnClickListener(view -> {
            inpDegreeName.setError(null);

            boolean valid = validate(inpDegreeText);
            if (valid) {
                getAttendanceFromDB(inpDegreeText, inpClassText, inpYearText, inpClassTypeText);
            }
        });
    }

    public void getAttendanceFromDB(String inpDegreeName, String inpClassName, String inpYearName, String inpClassTypeText) {

        makeDir();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Workbook workbook = new HSSFWorkbook();

        String tableName = inpDegreeName + "_" + inpClassName + "_" + inpYearName + "_" + inpClassTypeText;

// -------------------------------------------------------------------------------------------------

        Sheet daySheet = workbook.createSheet("DayWiseDetails");
        String query = dbHelper.fetchAttendanceDetails(tableName);

        Log.d("DailyWiseQuery", query);
        Cursor cursor = database.rawQuery(query, null);
        String[] columnNames = cursor.getColumnNames();

        Row headerRow = daySheet.createRow(0);
        for (int i = 0; i < columnNames.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnNames[i]);
        }

        int rowIndex = 1;

        while (cursor.moveToNext()) {
            Row row = daySheet.createRow(rowIndex);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = row.createCell(i);
//                if (i > 1) {
//                    int cell_value = Integer.parseInt(cursor.getString(i)) * multiplicationConstant;
//                    if (i == 2) {
//                        Log.d("CHECKING", String.valueOf(cell_value));
//                    }
//                    cell.setCellValue(String.valueOf(cell_value));
//                } else {
                cell.setCellValue(cursor.getString(i));
//                }
            }
            rowIndex++;
        }

        Row absentRow = daySheet.createRow(rowIndex);
        Cell nameAbsentcell = absentRow.createCell(0);
        nameAbsentcell.setCellValue("Absentees Count");
        for (int i = 2; i < columnNames.length; i++) {
            Cell cell = absentRow.createCell(i);
            String absentQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE `" + columnNames[i] + "`= 0";
            System.out.println(absentQuery);
            Cursor absentCursor = database.rawQuery(absentQuery, null);
            absentCursor.moveToFirst();
            cell.setCellValue(absentCursor.getInt(0));
            absentCursor.close();
        }

        cursor.close();

//--------------------------------------------------------------------------------------------------

        Sheet monthSheet = workbook.createSheet("MonthWiseDetails");
        Sheet tierOneSheet = workbook.createSheet("Below 75 and Above and Equal 50");
        Sheet tierTwoSheet = workbook.createSheet("Below 50 and Above and Equal 30");
        Sheet tierThreeSheet = workbook.createSheet("Below 30");

        String[] resourceMonths = getResources().getStringArray(R.array.month);
        String[] monthNames = getResources().getStringArray(R.array.monthName);
        StringBuilder monthAttendanceQuery = new StringBuilder("SELECT rollNo, name,");

        List<String> availableMonths = new ArrayList<>();

        for (String month : resourceMonths) {
            Log.d("MONTH CHECK", month);

            String testQuery = "SELECT name FROM pragma_table_info('" + tableName + "') WHERE name LIKE '____" + month + "_%'";
            Cursor columnSelectionQuery = database.rawQuery(testQuery, null);
            if (columnSelectionQuery.moveToFirst()) {
                availableMonths.add(month);
                do {
                    String columnName = columnSelectionQuery.getString(0);
                    monthAttendanceQuery.append("COALESCE(").append(columnName).append(",0) + ");
                } while (columnSelectionQuery.moveToNext());
            }
            columnSelectionQuery.close();
            if (availableMonths.contains(month)) {
                monthAttendanceQuery.setLength(monthAttendanceQuery.length() - 3);
                monthAttendanceQuery.append(" AS ").append(monthNames[Integer.parseInt(month) - 1]).append(", ");
            }
        }
        monthAttendanceQuery.setLength(monthAttendanceQuery.length() - 2);
        monthAttendanceQuery.append(" FROM ").append(tableName).append(" GROUP BY rollNo");

        String monthWiseAttendanceQuery = monthAttendanceQuery.toString();
        Log.d("MONTH QUERY", monthWiseAttendanceQuery);
        Cursor resultCursor = database.rawQuery(monthWiseAttendanceQuery, null);


        Row headerRow1 = monthSheet.createRow(0);
        Row headerRow2 = tierOneSheet.createRow(0);
        Row headerRow3 = tierTwoSheet.createRow(0);
        Row headerRow4 = tierThreeSheet.createRow(0);

        headerRow1.createCell(0).setCellValue("RollNo");
        headerRow1.createCell(1).setCellValue("Name");

        headerRow2.createCell(0).setCellValue("RollNo");
        headerRow2.createCell(1).setCellValue("Name");

        headerRow3.createCell(0).setCellValue("RollNo");
        headerRow3.createCell(1).setCellValue("Name");

        headerRow4.createCell(0).setCellValue("RollNo");
        headerRow4.createCell(1).setCellValue("Name");

        for (int i = 0; i < availableMonths.size(); i++) {
            headerRow1.createCell(2 + i).setCellValue(monthNames[Integer.parseInt(availableMonths.get(i)) - 1]);
            headerRow2.createCell(2 + i).setCellValue(monthNames[Integer.parseInt(availableMonths.get(i)) - 1]);
            headerRow3.createCell(2 + i).setCellValue(monthNames[Integer.parseInt(availableMonths.get(i)) - 1]);
            headerRow4.createCell(2 + i).setCellValue(monthNames[Integer.parseInt(availableMonths.get(i)) - 1]);

        }
        headerRow1.createCell(2 + availableMonths.size()).setCellValue("Cumulative");
        headerRow1.createCell(3 + availableMonths.size()).setCellValue("Percentage");

        headerRow2.createCell(2 + availableMonths.size()).setCellValue("Cumulative");
        headerRow2.createCell(3 + availableMonths.size()).setCellValue("Percentage");

        headerRow3.createCell(2 + availableMonths.size()).setCellValue("Cumulative");
        headerRow3.createCell(3 + availableMonths.size()).setCellValue("Percentage");

        headerRow4.createCell(2 + availableMonths.size()).setCellValue("Cumulative");
        headerRow4.createCell(3 + availableMonths.size()).setCellValue("Percentage");


        Row totalColumnsRow = monthSheet.createRow(1);
        Row totalColumnsRow1 = tierOneSheet.createRow(1);
        Row totalColumnsRow2 = tierTwoSheet.createRow(1);
        Row totalColumnsRow3 = tierThreeSheet.createRow(1);

        totalColumnsRow.createCell(0).setCellValue("");
        totalColumnsRow.createCell(1).setCellValue("Total Columns");

        totalColumnsRow1.createCell(0).setCellValue("");
        totalColumnsRow1.createCell(1).setCellValue("Total Columns");

        totalColumnsRow2.createCell(0).setCellValue("");
        totalColumnsRow2.createCell(1).setCellValue("Total Columns");

        totalColumnsRow3.createCell(0).setCellValue("");
        totalColumnsRow3.createCell(1).setCellValue("Total Columns");

        int cumulativeTotal = 0;

        for (int i = 0; i < availableMonths.size(); i++) {
            String testQuery = "SELECT COUNT(name) FROM pragma_table_info('" + tableName + "') WHERE name LIKE '____" + availableMonths.get(i) + "_%'";
            Cursor countCursor = database.rawQuery(testQuery, null);
            if (countCursor.moveToFirst()) {
                cumulativeTotal += countCursor.getInt(0) * multiplicationConstant;
                totalColumnsRow.createCell(2 + i).setCellValue((countCursor.getInt(0) * multiplicationConstant));
                totalColumnsRow1.createCell(2 + i).setCellValue((countCursor.getInt(0) * multiplicationConstant));
                totalColumnsRow2.createCell(2 + i).setCellValue((countCursor.getInt(0) * multiplicationConstant));
                totalColumnsRow3.createCell(2 + i).setCellValue((countCursor.getInt(0) * multiplicationConstant));

            }
            countCursor.close();
        }
        totalColumnsRow.createCell(2 + availableMonths.size()).setCellValue(cumulativeTotal);
        totalColumnsRow.createCell(3 + availableMonths.size()).setCellValue("100 %");

        totalColumnsRow1.createCell(2 + availableMonths.size()).setCellValue(cumulativeTotal);
        totalColumnsRow1.createCell(3 + availableMonths.size()).setCellValue("100 %");

        totalColumnsRow2.createCell(2 + availableMonths.size()).setCellValue(cumulativeTotal);
        totalColumnsRow2.createCell(3 + availableMonths.size()).setCellValue("100 %");

        totalColumnsRow3.createCell(2 + availableMonths.size()).setCellValue(cumulativeTotal);
        totalColumnsRow3.createCell(3 + availableMonths.size()).setCellValue("100 %");

        int rowIndex1 = 2;
        int tierOneIndex = 2;
        int tierTwoIndex = 2;
        int tierThreeIndex = 2;

        while (resultCursor.moveToNext()) {
            Row row1 = monthSheet.createRow(rowIndex1);
            row1.createCell(0).setCellValue(resultCursor.getString(0));
            row1.createCell(1).setCellValue(resultCursor.getString(1));

            int cumulativeSum = 0;
            for (int i = 0; i < availableMonths.size(); i++) {
                int attendance = resultCursor.getInt(2 + i) * multiplicationConstant;
                row1.createCell(2 + i).setCellValue(attendance);
                cumulativeSum += attendance;
            }

            row1.createCell(2 + availableMonths.size()).setCellValue(cumulativeSum);
            double percentage = (cumulativeTotal > 0) ? (cumulativeSum * 100.0) / cumulativeTotal : 0.0;

            CellStyle cellStyle = workbook.createCellStyle();

            if (percentage < 75.0 && percentage >= 50) {

                cellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                cellStyle.setFillPattern((short) 1);

                Row row2 = tierOneSheet.createRow(tierOneIndex);
                row2.createCell(0).setCellValue(resultCursor.getString(0));
                row2.createCell(1).setCellValue(resultCursor.getString(1));

                int tiersCumulativeSum = 0;
                for (int i = 0; i < availableMonths.size(); i++) {
                    int attendance = resultCursor.getInt(2 + i) * multiplicationConstant;
                    row2.createCell(2 + i).setCellValue(attendance);
                    cumulativeSum += attendance;
                }

                row2.createCell(2 + availableMonths.size()).setCellValue(tiersCumulativeSum);
                String formattedPercentage = String.format(Locale.US, "%.2f", percentage);
                Cell cell1 = row2.createCell(3 + availableMonths.size());
                cell1.setCellValue(formattedPercentage + "%");

                tierOneIndex++;

            } else if (percentage < 50 && percentage >= 30) {

                cellStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
                cellStyle.setFillPattern((short) 1);

                Row row2 = tierTwoSheet.createRow(tierTwoIndex);
                row2.createCell(0).setCellValue(resultCursor.getString(0));
                row2.createCell(1).setCellValue(resultCursor.getString(1));

                int tiersCumulativeSum = 0;
                for (int i = 0; i < availableMonths.size(); i++) {
                    int attendance = resultCursor.getInt(2 + i) * multiplicationConstant;
                    row2.createCell(2 + i).setCellValue(attendance);
                    cumulativeSum += attendance;
                }

                row2.createCell(2 + availableMonths.size()).setCellValue(tiersCumulativeSum);
                String formattedPercentage = String.format(Locale.US, "%.2f", percentage);
                Cell cell1 = row2.createCell(3 + availableMonths.size());
                cell1.setCellValue(formattedPercentage + "%");

                tierTwoIndex++;

            } else if (percentage < 30) {

                cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle.setFillPattern((short) 1);

                Row row2 = tierThreeSheet.createRow(tierThreeIndex);
                row2.createCell(0).setCellValue(resultCursor.getString(0));
                row2.createCell(1).setCellValue(resultCursor.getString(1));

                int tiersCumulativeSum = 0;
                for (int i = 0; i < availableMonths.size(); i++) {
                    int attendance = resultCursor.getInt(2 + i) * multiplicationConstant;
                    row2.createCell(2 + i).setCellValue(attendance);
                    cumulativeSum += attendance;
                }

                row2.createCell(2 + availableMonths.size()).setCellValue(tiersCumulativeSum);
                String formattedPercentage = String.format(Locale.US, "%.2f", percentage);
                Cell cell1 = row2.createCell(3 + availableMonths.size());
                cell1.setCellValue(formattedPercentage + "%");

                tierThreeIndex++;
            }

            String formattedPercentage = String.format(Locale.US, "%.2f", percentage);
            Cell cell1 = row1.createCell(3 + availableMonths.size());
            cell1.setCellValue(formattedPercentage + "%");
            cell1.setCellStyle(cellStyle);

            rowIndex1++;


        }

        resultCursor.close();
        saveFile(workbook);
    }

    private void saveFile(Workbook workbook) {
        String fileName = inpDegreeText + "_" + inpClassText + "_" + inpYearText + "_" + inpClassTypeText + "_" + new Utils().getTIMESTAMP() + ".xlsx";
        File output;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 12,13,14
            String downloadDir = Environment.getExternalStorageDirectory().getPath();
            output = new File(downloadDir + "/Download/AttendEase", fileName);
        } else {
            // 9,10,11
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            output = new File(new File(downloadsDir, "AttendEase"), fileName);
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();
            Toast.makeText(this, "Generated Successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeDir() {

        File folder;
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        folder = new File(downloadsDir, "AttendEase");

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Message", "Failed to create directory");
            }
        } else {
            Log.d("Message", "Exist");
        }
    }

    private boolean validate(String degreeText) {
        if (degreeText == null) {
            inpDegreeName.setError("Select a Degree");
            return false;
        }
        return true;
    }
}