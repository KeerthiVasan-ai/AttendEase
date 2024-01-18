package com.keerthi77459.attendease.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.Arrays;

public class GenerateReport extends AppCompatActivity {

    Button downloadReport;
    AutoCompleteTextView inpDegreeName, inpClassName, inpYearName;
    private String inpDegreeText, inpClassText, inpYearText;
    DbHelper dbHelper;
    Utils utils;
    ClassData classData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        dbHelper = new DbHelper(this);
        classData = new ClassData(this);
        utils = new Utils();

        classData.getClass();

        inpDegreeName = findViewById(R.id.inpDegreeName);
        inpClassName = findViewById(R.id.inpClassName);
        inpYearName = findViewById(R.id.inpYearName);
        downloadReport = findViewById(R.id.downloadReport);

        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, classData.degreeName);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, classData.className);
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, classData.yearName);
        inpYearName.setAdapter(semesterAdapter);
        inpClassName.setAdapter(classAdapter);
        inpDegreeName.setAdapter(degreeAdapter);

        inpYearName.setOnItemClickListener((adapterView, view, i, l) -> inpYearText = inpYearName.getText().toString());
        inpClassName.setOnItemClickListener((adapterView, view, i, l) -> inpClassText = inpClassName.getText().toString());
        inpDegreeName.setOnItemClickListener((adapterView, view, i, l) -> inpDegreeText = inpDegreeName.getText().toString());

        downloadReport.setOnClickListener(view -> {
            inpDegreeName.setError(null);
            inpClassName.setError(null);
            inpYearName.setError(null);
            boolean valid = validate(inpDegreeText, inpClassText, inpYearText);
            if (valid) {
                getAllLocalUser(inpDegreeText, inpClassText, inpYearText);
            }
        });
    }

    public void getAllLocalUser(String inpDegreeName, String inpClassName, String inpYearName) {

        makeDir();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Workbook workbook = new HSSFWorkbook();
        String tableName = inpDegreeName + "_" + inpClassName + "_" + inpYearName;


        Sheet daySheet = workbook.createSheet("DayWiseDetails");
        String query = dbHelper.fetchAttendanceDetails(tableName);

        Log.d("DailyWiseQuery",query);
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

//        TODO : CHANGE THE WORK IN ACADEMIC BASIS

        Sheet monthSheet = workbook.createSheet("MonthWiseDetails");

        String[] resourceMonths = getResources().getStringArray(R.array.month);
        String[] months = Arrays.copyOfRange(resourceMonths, 0, utils.getCURRENT_MONTH());
        System.out.println(months.length);
        String[] monthNames = getResources().getStringArray(R.array.monthName);
        StringBuilder queryBuilder = new StringBuilder("SELECT rollNo,");

        for (String month : months) {
            queryBuilder.append("SUM(");
            String testQuery = "SELECT name FROM pragma_table_info('" + tableName + "') WHERE name LIKE '_%_" + month + "_%_%_%'";
            Cursor cursor1 = database.rawQuery(testQuery, null);
            if (cursor1.moveToFirst()) {
                do {
                    String columnName = cursor1.getString(0);
                    System.out.println(columnName);
                    queryBuilder.append("COALESCE(").append(columnName).append(",0) + ");
                } while (cursor1.moveToNext());
            } else {
                queryBuilder.append("0 + ");
            }
            queryBuilder.setLength(queryBuilder.length() - 3);
            queryBuilder.append(") AS ").append(monthNames[Integer.parseInt(month) - 1]).append(", ");
            cursor1.close();
        }
        queryBuilder.setLength(queryBuilder.length() - 2);
        queryBuilder.append(" FROM ").append(tableName).append(" GROUP BY rollNo");

        String query1 = queryBuilder.toString();
        Log.d("MonthWiseQuery",query1);

        Cursor resultCursor = database.rawQuery(query1, null);

        String[] columnNames1 = resultCursor.getColumnNames();

        Row headerRow1 = monthSheet.createRow(0);
        for (int i = 0; i < columnNames1.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(columnNames1[i]);
        }

        int rowIndex1 = 1;
        while (resultCursor.moveToNext()) {
            Row row1 = monthSheet.createRow(rowIndex1);
            for (int i = 0; i < columnNames1.length; i++) {
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
            // 12,13
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

    private boolean validate(String degreeText, String classText, String yearText) {
        if (degreeText == null) {
            inpDegreeName.setError("Select a Degree");
            return false;
        }
        if (classText.trim().isEmpty()) {
            inpClassName.setError("Enter the Class Name");
            return false;
        }

        if (yearText == null) {
            inpYearName.setError("Select a Semester");
            return false;
        }
        return true;
    }
}