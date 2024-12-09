package com.keerthi77459.attendease.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.db.DbHelper;
import com.keerthi77459.attendease.model.ClassData;
import com.keerthi77459.attendease.utils.Utils;
import com.keerthi77459.attendease.viewmodel.AlertDialogBox;
import com.keerthi77459.attendease.viewmodel.ProcessExcel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddClass extends AppCompatActivity {

    DbHelper dbhelper;
    Button submit;
    LinearLayout excel;
    TextView fileNameText;
    AutoCompleteTextView departmentName, className, classTypeName;
    TextInputEditText classStrength;
    String[] department, classType;
    AlertDialog alert;
    private String departmentText, degreeText, classText, yearText, classTypeText, classStrengthText, extension, institutionId;
    Uri fileName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        Resources resource = getResources();
        db = FirebaseFirestore.getInstance();
        ClassData classData = new ClassData(AddClass.this);
        Utils utils = new Utils();
        AlertDialogBox alertDialogBox = new AlertDialogBox(this);
        SharedPreferences sharedPreferences = getSharedPreferences("OnBoardingActivity", Context.MODE_PRIVATE);

        department = sharedPreferences.getStringSet("departmentsName", new HashSet<>()).toArray(new String[]{});
        classType = resource.getStringArray(R.array.class_type);
        institutionId = sharedPreferences.getString("institutionId", "");

        submit = findViewById(R.id.submit);
        excel = findViewById(R.id.uploadButton);

        className = findViewById(R.id.className);
        classTypeName = findViewById(R.id.classType);
        departmentName = findViewById(R.id.degreeName);
        classStrength = findViewById(R.id.classStrength);
        fileNameText = findViewById(R.id.fileNameText);

        dbhelper = new DbHelper(this);

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, department);
        ArrayAdapter<String> classTypeAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, classType);

        departmentName.setAdapter(departmentAdapter);
        classTypeName.setAdapter(classTypeAdapter);


        departmentName.setOnItemClickListener((adapterView, view, i, l) -> {
            departmentText = departmentName.getText().toString();
            classTypeName.setText(null);
            className.setText(null);
            classStrength.setText(null);
        });

        classTypeName.setOnItemClickListener((adapterView, view, i, L) -> {
            classTypeText = classTypeName.getText().toString();
            fetchClassNames(departmentText, institutionId, classTypeText);
            className.setText(null);
            classStrength.setText(null);
        });

        className.setOnItemClickListener((adapterView, view, i, l) -> {
            fetchClassStrength(institutionId, departmentText, classTypeText, className.getText().toString());
            degreeText = className.getText().toString().split("-")[0];
            classText = className.getText().toString().split("-")[1];
            yearText = className.getText().toString().split("-")[2];
            classStrength.setText(null);
        });

        alert = alertDialogBox.displayDialog(utils.getADD_CLASS_MESSAGE());
        alert.show();

        excel.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                selectFile();
            } else {
                if (ContextCompat.checkSelfPermission(AddClass.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(AddClass.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    ActivityCompat.requestPermissions(AddClass.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }
            }
        });

        submit.setOnClickListener(view -> {

            departmentName.setError(null);
            className.setError(null);
            classTypeName.setError(null);
            classStrength.setError(null);

            classStrengthText = Objects.requireNonNull(classStrength.getText()).toString();

            boolean valid = validate(departmentText, degreeText, classText, yearText, classTypeText, classStrengthText);
            String tableName = degreeText + "_" + classText + "_" + yearText + "_" + classTypeText;

            if (valid) {
                extension = fileNameText.getText().toString().split("\\.")[1];
            } else {
                Toast.makeText(AddClass.this, "Select the Excel File", Toast.LENGTH_SHORT).show();
            }
            createTable(tableName);

            ProcessExcel processExcel = new ProcessExcel(this);
            boolean validateFile = processExcel.validateFile(fileName, tableName, extension, departmentText, classText, yearText);

            if (validateFile) {
                classData.addClass(departmentText, degreeText, classText, yearText, classTypeText, classStrengthText);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Class Created Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Check with File Format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validate(String departmentText, String degreeText, String classText, String yearText, String timeDuration, String classStrengthText) {
        if (departmentText == null) {
            departmentName.setError("Select a Degree");
            return false;
        }

        if (degreeText == null && classText == null && yearText == null) {
            className.setError("Select a Class or Check the Network");
            return false;
        }

        if (timeDuration == null) {
            classTypeName.setError("Enter the Duration in Minutes");
            return false;
        }

        if (classStrengthText.trim().isEmpty()) {
            classStrength.setError("Check Internet Connection");
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
                if (data != null) {
                    fileName = data.getData();
                }
                Cursor fileCursor = getContentResolver().query(fileName, null, null, null, null);
                assert fileCursor != null;
                int nameIndex = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileCursor.moveToFirst();
                fileNameText.setText(fileCursor.getString(nameIndex));
                fileCursor.close();
            }
        }
    }

    private void createTable(String tableName) {
        SQLiteDatabase db = openOrCreateDatabase(new Utils().getDB_NAME(), MODE_PRIVATE, null);
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + "(rollNo TEXT PRIMARY KEY,name TEXT,degree TEXT,class TEXT,year TEXT,mode TEXT)";
        System.out.println(createTable);
        db.execSQL(createTable);
        Log.d("Message", "Done");
        db.close();
    }

    private void fetchClassNames(String departmentName, String institutionId, String classType) {
        Log.d("Class Type", classType);
        List<String> subCollectionIds = new ArrayList<>();
        if (Objects.equals(institutionId, "")) {
            Toast.makeText(this, "Some Problem With your Institution", Toast.LENGTH_LONG).show();
        } else {
            db.collection(institutionId).document(departmentName).collection(classType).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot subCollection : task.getResult()) {
                        subCollectionIds.add(subCollection.getId());
                    }
                    updateClassAdapter(subCollectionIds);
                    Log.d("SubCollections", subCollectionIds.toString());
                } else {
                    Log.e("Firebase", "Failed to fetch subCollections", task.getException());
                }
            }).addOnFailureListener(e -> Log.e("Firebase", "Error fetching subCollections", e));
        }
    }

    private void fetchClassStrength(String institutionId, String departmentName, String classType, String className) {
        db.collection(institutionId).document(departmentName).collection(classType).document(className)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> data = task.getResult().getData();
                        if (data != null) {
                            String strength = (String) data.get("strength");
                            if (strength != null) {
                                classStrength.setText(strength);
                                Log.d("Firestore", "Strength: " + strength);
                            } else {
                                Log.w("Firestore", "Strength field is missing in the document.");
                            }
                        } else {
                            Log.w("Firestore", "No data found in the document.");
                        }
                    } else {
                        Log.e("Firestore", "Error fetching document", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch document", e));
    }


    private void updateClassAdapter(List<String> classNameList) {
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, R.layout.drop_down_text, classNameList);
        className.setAdapter(classAdapter);
    }
}

