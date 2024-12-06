package com.keerthi77459.attendease.adapter;

import static android.view.View.INVISIBLE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.keerthi77459.attendease.R;
import com.keerthi77459.attendease.db.DbHelper;
import com.keerthi77459.attendease.model.AttendanceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceViewHolder> {

    Context context;
    ArrayList<AttendanceData> attendanceData;
    String tableName;
    SharedPreferences sharedPreferences;

    public ViewAttendanceAdapter(Context context, ArrayList<AttendanceData> attendanceData, String tableName) {
        this.context = context;
        this.attendanceData = attendanceData;
        this.tableName = tableName;
    }

    @NonNull
    @Override
    public ViewAttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_attendance_layout, parent, false);
        return new ViewAttendanceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAttendanceViewHolder holder, int position) {
        AttendanceData data = attendanceData.get(position);

        String columnName = formatColumnName(data.getColumnName());
        String attendanceDetails = formatAttendanceStatusData(data.getTrueValues());

        holder.nameTextView.setText(columnName);
        holder.attendanceStatus.setText(attendanceDetails);

        holder.deleteButton.setOnClickListener(view -> {
            sharedPreferences = context.getSharedPreferences("doOnce", Context.MODE_PRIVATE);
            String savedColumnName = sharedPreferences.getString("LatestColumn", null);

            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String query = "ALTER TABLE " + tableName + " DROP COLUMN " + data.getColumnName();

            db.execSQL(query);
            db.close();

            if (Objects.equals(savedColumnName, data.getColumnName())) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LatestColumn", null);
                editor.apply();
            }

            notifyItemChanged(position);
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_LONG).show();
        });


    }

    @Override
    public int getItemCount() {
        return attendanceData.size();
    }

    private String formatColumnName(String columnName) {
        String[] columnNameElement = columnName.split("_");
        return "Date :" + columnNameElement[1] + "-" + columnNameElement[2] + "-" + columnNameElement[3] + "  Time :" + columnNameElement[4] + ":" + columnNameElement[5];
    }

    private String formatAttendanceStatusData(List<String> attendanceData) {
        StringBuilder attendanceStatusData = new StringBuilder();
        if (attendanceData.isEmpty()) {
            return "Nil";
        }
        for (int i = 0; i < attendanceData.size(); i++) {
            String detail = attendanceData.get(i);
            String lastThreeChars = detail.substring(detail.length() - 3).replaceFirst("^0+", "");

            attendanceStatusData.append(lastThreeChars);

            if (i < attendanceData.size() - 1) {
                attendanceStatusData.append(",");
            }
        }
        return attendanceStatusData.toString();
    }
}

class ViewAttendanceViewHolder extends RecyclerView.ViewHolder {

    TextView nameTextView, attendanceStatus;
    ImageButton deleteButton;

    public ViewAttendanceViewHolder(@NonNull View itemView) {
        super(itemView);

        nameTextView = itemView.findViewById(R.id.dateAndTime);
        attendanceStatus = itemView.findViewById(R.id.attendanceData);
        deleteButton = itemView.findViewById(R.id.deleteClass);

        deleteButton.setVisibility(View.INVISIBLE);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String dataToCopy = nameTextView.getText().toString() + "\n" + attendanceStatus.getText().toString();  // Replace itemName with the actual data

                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText("item_name", dataToCopy);

                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }

                Toast.makeText(view.getContext(), "Item copied to clipboard", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


    }
}