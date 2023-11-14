package com.keerthi77459.attendease;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.math.BigDecimal;

public class AddClass extends AppCompatActivity {

    DbHelper dbhelper;
    Button submit;
    LinearLayout excel;
    TextView fileNameText;
    AutoCompleteTextView degreeName, yearName;
    TextInputLayout className;
    String[] semester, degree;
    private String degreeText, yearText,extension;
    Uri fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        Resources resource = getResources();
        semester = resource.getStringArray(R.array.semester);
        degree = resource.getStringArray(R.array.degree);

        submit = findViewById(R.id.submit);
        excel = findViewById(R.id.uploadButton);
        className = findViewById(R.id.classNameField);
        degreeName = findViewById(R.id.degreeName);
        yearName = findViewById(R.id.yearName);
        fileNameText = findViewById(R.id.fileNameText);

        dbhelper = new DbHelper(this);

        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, degree);
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, semester);
        yearName.setAdapter(semesterAdapter);
        degreeName.setAdapter(degreeAdapter);

        yearName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                yearText = yearName.getText().toString();
            }
        });

        degreeName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                degreeText = degreeName.getText().toString();
            }
        });


        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(yearText);
                System.out.println(degreeText);

                if (ContextCompat.checkSelfPermission(AddClass.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(AddClass.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    ActivityCompat.requestPermissions(AddClass.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                degreeName.setError(null);
                className.setError(null);
                yearName.setError(null);
                String classText = String.valueOf(className.getEditText().getText());
                boolean valid = validate(degreeText, classText, yearText);
                if (valid) {
                    if (fileName != null) {
                        extension = fileNameText.getText().toString().split("\\.")[1];
                        if (extension.equals("xlsx") || extension.equals("xls")) {
                            long done = readFile(fileName);
                            System.out.println(done);
                            long done1 = addClass(classText);
                            System.out.println(done1);
                            startActivity(new Intent(AddClass.this, MainActivity.class));
                            Toast.makeText(AddClass.this, "Class Created Successfully", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(AddClass.this, "Select the Excel File Only", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddClass.this, "Select the File", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean validate(String degreeText, String classText, String yearText) {
        if (degreeText == null) {
            degreeName.setError("Select a Degree");
            return false;
        }
        if (classText.trim().isEmpty()) {
            className.setError("Enter the Class Name");
            return false;
        }

        if (yearText == null) {
            yearName.setError("Select a Semester");
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    Toast.makeText(AddClass.this,"Permission Denied",Toast.LENGTH_LONG).show();
                }
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 102);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                String filepath = data.getData().getPath();
                fileName = data.getData();
                Cursor fileCursor = getContentResolver().query(fileName, null, null, null, null);
                int nameIndex = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileCursor.moveToFirst();
                fileNameText.setText(fileCursor.getString(nameIndex));
            }
        }
    }

    private long readFile(final Uri file) {
        final long[] result = {0};
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    XSSFWorkbook workbook;
                    try (InputStream inputStream = getContentResolver().openInputStream(file)) {
                        workbook = new XSSFWorkbook(inputStream);
                    }
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    int rowscount = sheet.getPhysicalNumberOfRows();
                    if (rowscount > 0) {
                        for (int r = 1; r < rowscount; r++) {
                            Row row = sheet.getRow(r);
                            if (row.getPhysicalNumberOfCells() == 3) {
                                String A = getCellData(row, 0, formulaEvaluator);
                                String B = getCellData(row, 1, formulaEvaluator);
                                String C = getCellData(row, 2, formulaEvaluator);

                                System.out.println(A);
                                System.out.println(B);
                                System.out.println(C);

//                              TODO: SQLite Insertion
                                try {
                                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                                    ContentValues contentValue = new ContentValues();
                                    contentValue.put("rollNo", A);
                                    contentValue.put("name", B);
                                    contentValue.put("degree", degreeText);
                                    contentValue.put("class", String.valueOf(className.getEditText().getText()));
                                    contentValue.put("year", yearText);
                                    contentValue.put("phoneNumber", C);
                                    db.insert("studentDetail", null, contentValue);
                                    ContentValues contentValue2 = new ContentValues();
                                    contentValue2.put("rollNo", A);
                                    result[0] = db.insert("attendanceDetail", null, contentValue2);
                                } catch (SQLiteException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return result[0];
    }

    private String getCellData(Row row, int cellposition, FormulaEvaluator formulaEvaluator) {
        String value = "";
        Cell cell = row.getCell(cellposition);
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

    private long addClass(String ClassText) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        String query = "SELECT * FROM studentDetail WHERE degree = '"+degreeText+"'"+" AND class = '"+ClassText+"'"+" AND year = '"+degreeText+"'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            Toast.makeText(this, "Class Already Exists", Toast.LENGTH_SHORT).show();
            return 0;
        } else {
            long result = 0;
            ContentValues contentValue1 = new ContentValues();
            contentValue1.put("degree", degreeText);
            contentValue1.put("class", ClassText);
            contentValue1.put("year", yearText);
            result = db.insert("classDetail", null, contentValue1);
            return result;
        }
    }
}
