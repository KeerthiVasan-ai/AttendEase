package com.keerthi77459.attendease.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.ui.StudentDetail;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    Context context;
    ArrayList<String> departmentName, allClass, classType, classStrength;

    public ClassAdapter(Context context, ArrayList<String> departmentName, ArrayList<String> allClass, ArrayList<String> classType, ArrayList<String> classStrength) {

        this.context = context;
        this.departmentName = departmentName;
        this.allClass = allClass;
        this.classType = classType;
        this.classStrength = classStrength;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.class_layout, parent, false);
        return new ClassViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        holder.outDepartmentName.setText(String.valueOf(departmentName.get(position)));
        holder.outClassName.setText(String.valueOf(allClass.get(position)));
        holder.outClassStrength.setText("Class Strength:" + classStrength.get(position));
    }

    @Override
    public int getItemCount() {

        return allClass.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {

        TextView outDepartmentName, outClassName, outClassStrength;
        ImageButton delete;
        SharedPreferences sharedPreferences;


        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);

            delete = itemView.findViewById(R.id.deleteClass);
            outDepartmentName = itemView.findViewById(R.id.outDegreeName);
            outClassName = itemView.findViewById(R.id.outClassName);
            outClassStrength = itemView.findViewById(R.id.outYearName);

            delete.setVisibility(View.INVISIBLE);

            System.out.println(outClassName.getText().toString());

            itemView.setOnClickListener(view -> {
                sharedPreferences = itemView.getContext().getSharedPreferences("dataPassing", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String degree = outClassName.getText().toString().split("-")[0];
                String Class = outClassName.getText().toString().split("-")[1];
                String year = outClassName.getText().toString().split("-")[2];
                String classType = outClassName.getText().toString().split("-")[3];
                String classStrength = outClassStrength.getText().toString().split(":")[1];

                editor.putString("outDegreeName", degree);
                editor.putString("outClassName", Class);
                editor.putString("outYearName", year);
                editor.putString("classType", classType);
                editor.putString("classStrength", classStrength);

                editor.apply();
                Intent intent = new Intent(itemView.getContext(), StudentDetail.class);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
