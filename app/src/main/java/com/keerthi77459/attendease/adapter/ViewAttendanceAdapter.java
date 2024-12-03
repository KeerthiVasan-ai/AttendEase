package com.keerthi77459.attendease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.model.AttendanceData;

import java.util.ArrayList;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceViewHolder> {

    Context context;
    ArrayList<AttendanceData> attendanceData;

    public ViewAttendanceAdapter(Context context,ArrayList<AttendanceData> attendanceData){
        this.context = context;
        this.attendanceData = attendanceData;
    }

    @NonNull
    @Override
    public ViewAttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_attendance_layout,parent,false);
        return new ViewAttendanceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAttendanceViewHolder holder, int position) {
        AttendanceData data = attendanceData.get(position);
        holder.nameTextView.setText(data.getColumnName());
        holder.attendanceStatus.setText(String.join(", ", data.getTrueValues()));
    }

    @Override
    public int getItemCount() {
        return attendanceData.size();
    }
}

class ViewAttendanceViewHolder extends RecyclerView.ViewHolder {

    TextView nameTextView, attendanceStatus;

    public ViewAttendanceViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.columnNameTextView);
        attendanceStatus = itemView.findViewById(R.id.trueValuesTextView);

//        TODO: IMPLEMENT A DELETE BUTTON, CHECK WITH SHAREDPREFERNCE WITH THE COLUMN NAME IF IT IS
//        TODO: MATCHES ALTER TABLE AND CHANGE THE SP
//        TODO: ELSE, DELETE THE COLUMN
    }
}