package com.keerthi77459.attendease.adapter;

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

public class ManageClassAdapter extends RecyclerView.Adapter<ManageClassViewHolder> {

    Context context;
    ArrayList<String> degreeName,className,yearName;

    public ManageClassAdapter(Context context,ArrayList<String> degreeName,ArrayList<String> className,ArrayList<String> yearName) {

        this.context = context;
        this.degreeName = degreeName;
        this.className = className;
        this.yearName = yearName;
    }

    @NonNull
    @Override
    public ManageClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.class_layout,parent,false);
        return new ManageClassViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageClassViewHolder holder, int position) {
        holder.outDegreeName.setText(String.valueOf(degreeName.get(position)));
        holder.outClassName.setText(String.valueOf(className.get(position)));
        holder.outYearName.setText("Semester:"+String.valueOf(yearName.get(position)));
    }

    @Override
    public int getItemCount() {
        return className.size();
    }
}

class ManageClassViewHolder extends RecyclerView.ViewHolder{

    TextView outDegreeName,outClassName,outYearName;
    Utils utils = new Utils();
    ImageButton delete;

    public ManageClassViewHolder(@NonNull View itemView) {
        super(itemView);

        delete = itemView.findViewById(R.id.deleteClass);
        outDegreeName = itemView.findViewById(R.id.outDegreeName);
        outClassName = itemView.findViewById(R.id.outClassName);
        outYearName = itemView.findViewById(R.id.outYearName);

        delete.setVisibility(View.VISIBLE);

        delete.setOnClickListener(view -> {

            String outDegreeText = outDegreeName.getText().toString();
            String outClassText = outClassName.getText().toString();
            String outYearText = outYearName.getText().toString().split(":")[1];

            String tableName = outDegreeText + "_" + outClassText + "_" + outYearText;
            System.out.println(tableName);

            DbHelper dbHelper = new DbHelper(itemView.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String whereClause = "degree = ? AND class = ? AND year = ?";
            String[] whereArgs = {outDegreeText,outClassText,outYearText};

            String query = "DROP TABLE " + tableName;
            System.out.println(query);
            db.execSQL(query);
            db.delete(utils.getTABLE_CLASS_DETAIL(),whereClause,whereArgs);
            db.close();

            Toast.makeText(itemView.getContext(), "Successfully Deleted", Toast.LENGTH_SHORT).show();
            itemView.getContext().startActivity(new Intent(itemView.getContext(), MainActivity.class));

        });
    }
}