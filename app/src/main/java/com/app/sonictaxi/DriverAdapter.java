package com.app.sonictaxi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class DriverAdapter extends FirebaseRecyclerAdapter<Driver, DriverAdapter.DriverViewHolder> {

    public DriverAdapter(@NonNull FirebaseRecyclerOptions<Driver> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull DriverViewHolder holder, int position, @NonNull Driver model) {
        // نستخدم Getters من كلاس Driver
        holder.txtName.setText(model.getName() != null ? model.getName() : "غير معروف");
        holder.txtCar.setText(model.getCarModel() != null ? model.getCarModel() : "غير محدد");
        holder.txtStatus.setText(model.getStatus() != null ? model.getStatus() : "Pending");
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // نتأكد من استخدام ملف التصميم الخاص بك item_driver
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtCar, txtStatus;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            // هذه الـ IDs يجب أن تكون موجودة في ملف item_driver.xml
            txtName = itemView.findViewById(R.id.txtDriverName);
            txtCar = itemView.findViewById(R.id.txtCarModel);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}