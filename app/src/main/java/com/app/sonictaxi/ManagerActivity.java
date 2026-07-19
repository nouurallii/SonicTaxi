package com.app.sonictaxi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        // جلب الـ UID الخاص بالمدير الذي سجل دخوله حالياً
        currentManagerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. ربط العناصر
        tvOfficeName = findViewById(R.id.tv_office_name);
        etDriverName = findViewById(R.id.et_driver_name);
        etDriverPhone = findViewById(R.id.et_driver_phone);
        etDriverCar = findViewById(R.id.et_driver_car);
        btnAddDriver = findViewById(R.id.btn_add_driver);
        rvDrivers = findViewById(R.id.drivers_recycler);

        // 2. إعداد قائمة السائقين التابعين لهذا المدير فقط
        rvDrivers.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Driver> options = new FirebaseRecyclerOptions.Builder<Driver>()
                .setQuery(mDatabase.child("Users/Drivers").orderByChild("managerId").equalTo(currentManagerId), Driver.class)
                .build();

        driverAdapter = new DriverAdapter(options);
        rvDrivers.setAdapter(driverAdapter);

        // 3. زر الإضافة
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

        // إنشاء ID فريد للسائق باستخدام push()
        DatabaseReference newDriverRef = mDatabase.child("Users/Drivers").push();

        Map<String, Object> driverMap = new HashMap<>();
        driverMap.put("name", name);
        driverMap.put("phone", phone);
        driverMap.put("carModel", car);
        driverMap.put("status", "Pending");
        driverMap.put("managerId", currentManagerId); // الربط الجوهري

        newDriverRef.setValue(driverMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "تم إضافة السائق بنجاح", Toast.LENGTH_SHORT).show();
                        etDriverName.setText("");
                        etDriverPhone.setText("");
                        etDriverCar.setText("");
                    } else {
                        Toast.makeText(this, "فشل الإضافة: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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