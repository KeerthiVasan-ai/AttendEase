package com.keerthi77459.attendease.adapter;

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

public class ClassAdapter extends RecyclerView.Adapter<ClassViewHolder> {

    Context context;
    ArrayList<String> degreeName,className,yearName;

    public ClassAdapter(Context context,ArrayList<String> degreeName,ArrayList<String> className,ArrayList<String> yearName) {

        this.context = context;
        this.degreeName = degreeName;
        this.className = className;
        this.yearName = yearName;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.class_layout,parent,false);
        return new ClassViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        holder.outDegreeName.setText(String.valueOf(degreeName.get(position)));
        holder.outClassName.setText(String.valueOf(className.get(position)));
        holder.outYearName.setText("Semester:"+String.valueOf(yearName.get(position)));
    }

    @Override
    public int getItemCount() {

        return className.size();
    }
}

class ClassViewHolder extends RecyclerView.ViewHolder{

    TextView outDegreeName,outClassName,outYearName;
    ImageButton delete;
    SharedPreferences sharedPreferences;


    public ClassViewHolder(@NonNull View itemView) {
        super(itemView);

        delete = itemView.findViewById(R.id.deleteClass);
        outDegreeName = itemView.findViewById(R.id.outDegreeName);
        outClassName = itemView.findViewById(R.id.outClassName);
        outYearName = itemView.findViewById(R.id.outYearName);

        delete.setVisibility(View.INVISIBLE);

        itemView.setOnClickListener(view -> {
            sharedPreferences = itemView.getContext().getSharedPreferences("dataPassing",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String year = outYearName.getText().toString().split(":")[1];
            editor.putString("outDegreeName",outDegreeName.getText().toString());
            editor.putString("outClassName",outClassName.getText().toString());
            editor.putString("outYearName",year);

            editor.commit();
            Intent intent = new Intent(itemView.getContext(), StudentDetail.class);
            itemView.getContext().startActivity(intent);
        });



    }
}
