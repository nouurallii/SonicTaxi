package com.app.sonictaxi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class DriverActivity extends AppCompatActivity {

    private DatabaseReference database;
    private String currentRideId;
    private MapView mapView;
    private final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";

    private final Handler locationHandler = new Handler();
    private final int LOCATION_UPDATE_INTERVAL = 5000; // 5 ثوانٍ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_driver);

        mapView = findViewById(R.id.driver_mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        database = FirebaseDatabase.getInstance(DB_URL).getReference();

        updateDriverLocation();
        listenForRideRequests();

        findViewById(R.id.btn_send_offer).setOnClickListener(v -> {
            EditText etPrice = findViewById(R.id.et_price_offer);
            String priceInput = etPrice.getText().toString();

            if (!priceInput.isEmpty()) {
                sendOffer(Double.parseDouble(priceInput));
            } else {
                etPrice.setError("يرجى إدخال سعر!");
            }
        });

        findViewById(R.id.btn_reject).setOnClickListener(v ->
                findViewById(R.id.request_card).setVisibility(View.GONE)
        );

        // ربط زر تسجيل الخروج
        findViewById(R.id.btn_logout).setOnClickListener(v -> logoutDriver());
    }

    private void logoutDriver() {
        // 1. إيقاف تتبع الموقع فوراً
        locationHandler.removeCallbacksAndMessages(null);

        // 2. مسح موقع السائق من قاعدة البيانات
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String driverUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            database.child("DriversLocation").child(driverUid).removeValue();
        }

        // 3. تسجيل الخروج من Firebase والعودة لشاشة الدخول
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void listenForRideRequests() {
        database.child("Rides").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RideRequest r = ds.getValue(RideRequest.class);
                    if (r != null && "pending".equals(r.getStatus())) {
                        currentRideId = ds.getKey();
                        findViewById(R.id.request_card).setVisibility(View.VISIBLE);
                        found = true;
                        break;
                    }
                }
                if (!found) findViewById(R.id.request_card).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        });
    }


    private void sendOffer(double offer) {
        if (currentRideId == null || FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String driverUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rideRef = database.child("Rides").child(currentRideId);

        rideRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                String status = currentData.child("status").getValue(String.class);

                if ("pending".equals(status)) {
                    currentData.child("offerPrice").setValue(offer);
                    currentData.child("status").setValue("offered");
                    currentData.child("driverId").setValue(driverUid);
                    return Transaction.success(currentData);
                }
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    Toast.makeText(DriverActivity.this, "تم إرسال العرض بنجاح!", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.request_card).setVisibility(View.GONE);
                } else {
                    Toast.makeText(DriverActivity.this, "عذراً، الطلب لم يعد متاحاً.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateDriverLocation() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String driverUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // نستخدم إحداثيات افتراضية تتحرك قليلاً كل 5 ثوانٍ
        double baseLat = 32.2215; // إحداثيات مركز نابلس
        double baseLng = 35.2497;

        // إضافة قيمة بسيطة متغيرة لمحاكاة الحركة
        double offset = System.currentTimeMillis() % 10000 / 1000000.0;

        database.child("DriversLocation").child(driverUid)
                .setValue(new LocationData(baseLat + offset, baseLng + offset));

        locationHandler.postDelayed(this::updateDriverLocation, LOCATION_UPDATE_INTERVAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHandler.removeCallbacksAndMessages(null);
    }
}