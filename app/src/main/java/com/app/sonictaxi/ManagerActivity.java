package com.app.sonictaxi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ManagerActivity extends AppCompatActivity {

    private TextView tvOfficeName;
    private EditText etDriverName, etDriverPhone, etDriverCar;
    private Button btnAddDriver;
    private RecyclerView rvDrivers;
    private DriverAdapter driverAdapter;

    private DatabaseReference mDatabase;
    private String currentManagerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentManagerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        tvOfficeName = findViewById(R.id.tv_office_name);
        etDriverName = findViewById(R.id.et_driver_name);
        etDriverPhone = findViewById(R.id.et_driver_phone);
        etDriverCar = findViewById(R.id.et_driver_car);
        btnAddDriver = findViewById(R.id.btn_add_driver);
        rvDrivers = findViewById(R.id.drivers_recycler);

        rvDrivers.setLayoutManager(new LinearLayoutManager(this));

        // جلب السائقين التابعين للمدير الحالي فقط باستخدام managerId
        FirebaseRecyclerOptions<Driver> options = new FirebaseRecyclerOptions.Builder<Driver>()
                .setQuery(mDatabase.child("Users/Drivers").orderByChild("managerId").equalTo(currentManagerId), Driver.class)
                .build();

        driverAdapter = new DriverAdapter(options) {
            @Override
            protected void onBindViewHolder(@NonNull DriverViewHolder holder, int position, @NonNull Driver model) {
                super.onBindViewHolder(holder, position, model);

                // إمكانية حذف أو إلغاء السائق من المكتب بالضغط المطول على العنصر
                holder.itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(ManagerActivity.this)
                            .setTitle("إلغاء السائق")
                            .setMessage("هل أنت متأكد من حذف هذا السائق من مكتبك؟")
                            .setPositiveButton("نعم", (dialog, which) -> {
                                getRef(position).removeValue().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ManagerActivity.this, "تم إلغاء السائق بنجاح", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(ManagerActivity.this, "فشل الحذف", Toast.LENGTH_SHORT).show();
                                });
                            })
                            .setNegativeButton("إلغاء", null)
                            .show();
                    return true;
                });
            }
        };

        rvDrivers.setAdapter(driverAdapter);
        btnAddDriver.setOnClickListener(v -> addDriverToSystem());
    }

    private void addDriverToSystem() {
        String name = etDriverName.getText().toString().trim();
        String phone = etDriverPhone.getText().toString().trim();
        String car = etDriverCar.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || car.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع بيانات السائق", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference newDriverRef = mDatabase.child("Users/Drivers").push();

        Map<String, Object> driverMap = new HashMap<>();
        driverMap.put("name", name);
        driverMap.put("phone", phone);
        driverMap.put("carModel", car);
        driverMap.put("status", "Pending");
        driverMap.put("managerId", currentManagerId);

        newDriverRef.setValue(driverMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "تم إضافة السائق بنجاح", Toast.LENGTH_SHORT).show();
                        etDriverName.setText("");
                        etDriverPhone.setText("");
                        etDriverCar.setText("");
                    } else {
                        Toast.makeText(this, "فشل الإضافة", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (driverAdapter != null) driverAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (driverAdapter != null) driverAdapter.stopListening();
    }
}