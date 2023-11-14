package com.keerthi77459.attendease;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GenerateReport extends AppCompatActivity {

    Button downloadReport;
    TextInputLayout inpClassName;
    AutoCompleteTextView inpDegreeName, inpYearName;
    String[] inpSemester, inpDegree;
    private String inpDegreeText, inpYearText;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        dbHelper = new DbHelper(this);

        Resources resource = getResources();
        inpSemester = resource.getStringArray(R.array.semester);
        inpDegree = resource.getStringArray(R.array.degree);

        inpDegreeName = findViewById(R.id.inpDegreeName);
        inpClassName = findViewById(R.id.inpClassNameField);
        inpYearName = findViewById(R.id.inpYearName);
        downloadReport = findViewById(R.id.downloadReport);

        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, inpDegree);
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, inpSemester);
        inpYearName.setAdapter(semesterAdapter);
        inpDegreeName.setAdapter(degreeAdapter);

        inpYearName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                inpYearText = inpYearName.getText().toString();
            }
        });

        inpDegreeName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                inpDegreeText = inpDegreeName.getText().toString();
            }
        });

        downloadReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inpDegreeName.setError(null);
                inpClassName.setError(null);
                inpYearName.setError(null);
                boolean valid = validate(inpDegreeText,
                        String.valueOf(inpClassName.getEditText().getText()),
                        inpYearText);
                if (valid) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        getAllLocalUser(inpDegreeText,
                                String.valueOf(inpClassName.getEditText().getText()),
                                inpYearText);
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void getAllLocalUser(String inpDegreeName, String inpClassName, String inpYearName) {

        makeDir();
        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("DayWiseDetails");
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String condition = "rollNo IN (SELECT rollNo FROM studentDetail WHERE degree = '" + inpDegreeName + "' AND class = '" + inpClassName + "' AND year = '" + inpYearName + "')";
        String query = "SELECT * FROM attendanceDetail WHERE " + condition;
        System.out.println(query);
        Cursor cursor = database.rawQuery(query, null);
        String[] columnNames = cursor.getColumnNames();

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnNames.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnNames[i]);
        }

        int rowIndex = 1;
        while (cursor.moveToNext()) {
            Row row = sheet.createRow(rowIndex);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(cursor.getString(i));
            }
            rowIndex++;
        }

        Sheet sheet1 = workbook.createSheet("MonthWiseDetails");

        String[] months = getResources().getStringArray(R.array.month);
        String[] monthNames = getResources().getStringArray(R.array.monthName);
        StringBuilder queryBuilder = new StringBuilder("SELECT rollNo,");

        for(String month : months) {
            queryBuilder.append("SUM(");
            String testQuery = "SELECT name FROM pragma_table_info('attendanceDetail') WHERE name LIKE '_%_"+month+"'";
            Cursor cursor1 = database.rawQuery(testQuery, null);
            if(cursor1.moveToFirst()) {
                 do {
                    String columnName = cursor1.getString(0);
                    System.out.println(columnName);
                    queryBuilder.append("COALESCE("+columnName + ",0) + ");
                } while (cursor1.moveToNext());
            } else {
                queryBuilder.append("0 + ");
            }
            queryBuilder.setLength(queryBuilder.length() - 3);
            queryBuilder.append(") AS "+monthNames[Integer.parseInt(month)-1]+", ");
            cursor1.close();
        }
        queryBuilder.setLength(queryBuilder.length() - 2);
        queryBuilder.append(" FROM attendanceDetail GROUP BY rollNo HAVING "+condition);

        String query1 = queryBuilder.toString();
        System.out.println(query1);

        Cursor resultCursor = database.rawQuery(query1,null);

        //TODO : ADD THIS IN NEW SHEET

        String[] columnNames1 = resultCursor.getColumnNames();

        Row headerRow1 = sheet1.createRow(0);
        for (int i = 0; i < columnNames1.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(columnNames1[i]);
        }

        int rowIndex1 = 1;
        while (resultCursor.moveToNext()) {
            Row row1 = sheet1.createRow(rowIndex1);
            for (int i = 0; i < columnNames1.length; i++) {
                Cell cell = row1.createCell(i);
                cell.setCellValue(resultCursor.getString(i));
            }
            rowIndex1++;
        }

        resultCursor.close();

        saveFile(workbook);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void saveFile(Workbook workbook) {
        StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        StorageVolume storageVolume = storageManager.getStorageVolumes().get(0);

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        System.out.println(timeStamp);

        String inpClassText = String.valueOf(inpClassName.getEditText().getText());
        String fileName = inpDegreeText + "_" + inpClassText + "_" + inpYearText + ".xls";
        File output = new File(storageVolume.getDirectory().getPath() + "/Download/AttendEase/" + fileName);
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void makeDir() {
        StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        StorageVolume storageVolume = storageManager.getStorageVolumes().get(0);
        File folder = new File(storageVolume.getDirectory().getPath() + "/Download/", "AttendEase");
        if (!folder.exists()) {
            folder.mkdir();
            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
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