package com.keerthi77459.attendease.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.db.DbHelper;
import com.keerthi77459.attendease.model.ClassData;
import com.keerthi77459.attendease.model.ProcessExcel;

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
        ClassData classData = new ClassData(AddClass.this);

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

        yearName.setOnItemClickListener((adapterView, view, i, l) -> yearText = yearName.getText().toString());

        degreeName.setOnItemClickListener((adapterView, view, i, l) -> degreeText = degreeName.getText().toString());


        excel.setOnClickListener(v -> {
            System.out.println(yearText);
            System.out.println(degreeText);

            if (ContextCompat.checkSelfPermission(AddClass.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(AddClass.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectFile();
            } else {
                ActivityCompat.requestPermissions(AddClass.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        });

        submit.setOnClickListener(view -> {

            degreeName.setError(null);
            className.setError(null);
            yearName.setError(null);

            String classText = String.valueOf(className.getEditText().getText());
            boolean valid = validate(degreeText, classText, yearText);
            if(valid){
                extension = fileNameText.getText().toString().split("\\.")[1];
            } else{
                Toast.makeText(AddClass.this, "Select the Excel File", Toast.LENGTH_SHORT).show();
            }

            ProcessExcel processExcel = new ProcessExcel(AddClass.this);
            boolean validateFile = processExcel.validateFile(fileName, extension,degreeText,classText,yearText);

            if (validateFile) {
                classData.addClass(degreeText, classText, yearText);
                startActivity(new Intent(AddClass.this, MainActivity.class));
                finish();
                Toast.makeText(AddClass.this, "Class Created Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddClass.this, "Check with File Format", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AddClass.this, "Permission Denied", Toast.LENGTH_LONG).show();
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
}

