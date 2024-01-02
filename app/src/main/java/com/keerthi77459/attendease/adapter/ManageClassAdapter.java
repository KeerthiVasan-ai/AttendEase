package com.keerthi77459.attendease.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.keerthi77459.attendease.ui.StudentDetail;
import com.keerthi77459.attendease.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

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
    ImageButton delete;
    Utils utils = new Utils();
    ArrayList<String> rollNo;

    public ManageClassViewHolder(@NonNull View itemView) {
        super(itemView);

        delete = itemView.findViewById(R.id.deleteClass);
        outDegreeName = itemView.findViewById(R.id.outDegreeName);
        outClassName = itemView.findViewById(R.id.outClassName);
        outYearName = itemView.findViewById(R.id.outYearName);

        rollNo = new ArrayList<String>();

        delete.setVisibility(View.VISIBLE);

        delete.setOnClickListener(view -> {

            String outDegreeText = outDegreeName.getText().toString();
            String outClassText = outClassName.getText().toString();
            String outYearText = outYearName.getText().toString().split(":")[1];

            DbHelper dbHelper = new DbHelper(itemView.getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String condition = "rollNo IN (SELECT rollNo FROM studentDetail WHERE degree = '" + outDegreeText + "' AND class = '" + outClassText + "' AND year = '" + outYearText + "')";
            String query = "SELECT rollNo FROM attendanceDetail WHERE " + condition;
            System.out.println(query);
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()){
                rollNo.add(cursor.getString(0));
            }

            String whereClauses = "degree = ? AND class = ? AND year = ?";
            String[] whereArgs = {outDegreeText,outClassText,outYearText};
            for(String roll : rollNo){
                db.delete(utils.getTABLE_ATTENDANCE_DETAIL(),"rollNo = ?",new String[] {roll});
            }

            db.delete(utils.getTABLE_STUDENT_DETAIL(),whereClauses,whereArgs);
            db.delete(utils.getTABLE_CLASS_DETAIL(),whereClauses,whereArgs);

            db.close();

            Toast.makeText(itemView.getContext(), "Successfully Deleted", Toast.LENGTH_SHORT).show();
            itemView.getContext().startActivity(new Intent(itemView.getContext(), MainActivity.class));

        });
    }
}