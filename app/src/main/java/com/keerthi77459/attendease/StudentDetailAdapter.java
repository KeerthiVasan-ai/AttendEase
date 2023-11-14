package com.keerthi77459.attendease;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class StudentDetailAdapter extends RecyclerView.Adapter<StudentDetailViewHolder> {

    Context context;
    ArrayList<String> rollNo;
    ArrayList<String> name;
    ArrayList<String> phoneNumber;
    ArrayList<String> attendedRoll;

    public StudentDetailAdapter(Context context, ArrayList<String> rollNo, ArrayList<String> name, ArrayList<String> phoneNumber) {
        this.context = context;
        this.rollNo = rollNo;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    @Override
    public StudentDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.student_layout,parent,false);
        return new StudentDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentDetailViewHolder holder, int position) {
        attendedRoll = new ArrayList<>();
        holder.outStudentName.setText(String.valueOf(name.get(position)));
        holder.outRollName.setText(String.valueOf(rollNo.get(position)));
        String value = rollNo.get(position);
        holder.isAttended.setText(value);

        holder.isAttended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.isAttended.isChecked()){
                    attendedRoll.add(value);
                } else{
                    attendedRoll.remove(value);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return rollNo.size();
    }

    public ArrayList<String> getAttendedRoll() {
        return attendedRoll;
    }
}

class StudentDetailViewHolder extends RecyclerView.ViewHolder {

    TextView outStudentName,outRollName;
    CheckBox isAttended;

    public StudentDetailViewHolder(@NonNull View itemView) {

        super(itemView);

        outStudentName = itemView.findViewById(R.id.outStudentName);
        outRollName = itemView.findViewById(R.id.outRollName);
        isAttended = itemView.findViewById(R.id.isAttended);
    }
}
