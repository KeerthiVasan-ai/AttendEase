package com.keerthi77459.attendease.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.db.DbHelper;
import com.keerthi77459.attendease.ui.MainActivity;
import com.keerthi77459.attendease.utils.Utils;

import java.util.ArrayList;

public class ManageClassAdapter extends RecyclerView.Adapter<ManageClassAdapter.ManageClassViewHolder> {

    Context context;
    ArrayList<String> departmentName, allClass, classType, classStrength;

    public ManageClassAdapter(Context context, ArrayList<String> departmentName, ArrayList<String> allClass, ArrayList<String> classType, ArrayList<String> classStrength) {

        this.context = context;
        this.departmentName = departmentName;
        this.allClass = allClass;
        this.classType = classType;
        this.classStrength = classStrength;
    }

    @NonNull
    @Override
    public ManageClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.class_layout, parent, false);
        return new ManageClassViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ManageClassViewHolder holder, int position) {
        holder.outDepartmentName.setText(String.valueOf(departmentName.get(position)));
        holder.outClassName.setText(String.valueOf(allClass.get(position)));
        holder.outClassStrength.setText("Class Strength:" + classStrength.get(position));
    }

    @Override
    public int getItemCount() {
        return allClass.size();
    }

    static class ManageClassViewHolder extends RecyclerView.ViewHolder {

        TextView outDepartmentName, outClassName, outClassStrength;
        Utils utils = new Utils();
        ImageButton delete;


        public ManageClassViewHolder(@NonNull View itemView) {
            super(itemView);

            delete = itemView.findViewById(R.id.deleteClass);
            outDepartmentName = itemView.findViewById(R.id.outDegreeName);
            outClassName = itemView.findViewById(R.id.outClassName);
            outClassStrength = itemView.findViewById(R.id.outYearName);

            delete.setVisibility(View.VISIBLE);

            delete.setOnClickListener(view -> {

                String outDegreeText = outDepartmentName.getText().toString();
                String outClassText = outClassName.getText().toString();

                String degreeText = outClassText.split("-")[0];
                String classText = outClassText.split("-")[1];
                String yearText = outClassText.split("-")[2];
                String classType = outClassText.split("-")[3];

                String tableName = degreeText + "_" + classText + "_" + yearText + "_" + classType;
                System.out.println(tableName);

                DbHelper dbHelper = new DbHelper(itemView.getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String whereClause = "degree = ? AND class = ? AND year = ? AND class_type = ?";
                String[] whereArgs = {outDegreeText, outClassText, yearText, classType};

                String query = "DROP TABLE " + tableName;
                System.out.println(query);
                db.execSQL(query);
                db.delete(utils.getTABLE_CLASS_DETAIL(), whereClause, whereArgs);
                db.close();

                Toast.makeText(itemView.getContext(), "Successfully Deleted", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(itemView.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                itemView.getContext().startActivity(intent);

            });
        }
    }

}

