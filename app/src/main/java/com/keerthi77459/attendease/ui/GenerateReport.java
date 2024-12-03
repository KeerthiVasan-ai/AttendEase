package com.keerthi77459.attendease.ui;

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GenerateReport extends AppCompatActivity {

    Button downloadReport;
    AutoCompleteTextView inpDegreeName;
    private String inpDegreeText, inpClassText, inpYearText;
    DbHelper dbHelper;
    Utils utils;
    ArrayList<String> overallClassDetails;
    ClassData classData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

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
            inpYearText = inpDegreeName.getText().toString().split("-")[2];
            inpClassText = inpDegreeName.getText().toString().split("-")[1];
        });

        downloadReport.setOnClickListener(view -> {
            inpDegreeName.setError(null);

            boolean valid = validate(inpDegreeText);
            if (valid) {
                getAttendanceFromDB(inpDegreeText, inpClassText, inpYearText);
            }
        });
    }

    public void getAttendanceFromDB(String inpDegreeName, String inpClassName, String inpYearName) {

        makeDir();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Workbook workbook = new HSSFWorkbook();

// -------------------------------------------------------------------------------------------------

        String tableName = inpDegreeName + "_" + inpClassName + "_" + inpYearName;

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
                cell.setCellValue(cursor.getString(i));
            }
            rowIndex++;
        }

        cursor.close();

//--------------------------------------------------------------------------------------------------

//        TODO : CHANGE THE WORK IN ACADEMIC BASIS

        Sheet monthSheet = workbook.createSheet("MonthWiseDetails");

        String[] resourceMonths = getResources().getStringArray(R.array.month);
        String[] months = Arrays.copyOfRange(resourceMonths, 0, utils.getCURRENT_MONTH());
        String[] monthNames = getResources().getStringArray(R.array.monthName);
        StringBuilder monthAttendanceQuery = new StringBuilder("SELECT rollNo,");

        for (String month : months) {
            monthAttendanceQuery.append("SUM(");
            String testQuery = "SELECT name FROM pragma_table_info('" + tableName + "') WHERE name LIKE '____" + month + "_%'";

            System.out.println(testQuery);

            Cursor columnSelectionQuery = database.rawQuery(testQuery, null);
            if (columnSelectionQuery.moveToFirst()) {
                do {
                    String columnName = columnSelectionQuery.getString(0);

                    System.out.println(columnName);

                    monthAttendanceQuery.append("COALESCE(").append(columnName).append(",0) + ");
                } while (columnSelectionQuery.moveToNext());
            } else {
                monthAttendanceQuery.append("0 + ");
            }
            monthAttendanceQuery.setLength(monthAttendanceQuery.length() - 3);
            monthAttendanceQuery.append(") AS ").append(monthNames[Integer.parseInt(month) - 1]).append(", ");
            columnSelectionQuery.close();
        }
        monthAttendanceQuery.setLength(monthAttendanceQuery.length() - 2);
        monthAttendanceQuery.append(" FROM ").append(tableName).append(" GROUP BY rollNo");

        String monthWiseAttendanceQuery = monthAttendanceQuery.toString();

        Log.d("Month Wise Query", monthWiseAttendanceQuery);

        Cursor resultCursor = database.rawQuery(monthWiseAttendanceQuery, null);
        String[] monthColumnNames = resultCursor.getColumnNames();

        Row headerRow1 = monthSheet.createRow(0);
        for (int i = 0; i < monthColumnNames.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(monthColumnNames[i]);
        }

        int rowIndex1 = 1;
        while (resultCursor.moveToNext()) {
            Row row1 = monthSheet.createRow(rowIndex1);
            for (int i = 0; i < monthColumnNames.length; i++) {
                Cell cell = row1.createCell(i);
                cell.setCellValue(resultCursor.getString(i));
            }
            rowIndex1++;
        }

        resultCursor.close();

        saveFile(workbook);
    }

    private void saveFile(Workbook workbook) {
        String fileName = inpDegreeText + "_" + inpClassText + "_" + inpYearText + "_" + utils.getTIMESTAMP() + ".xls";
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