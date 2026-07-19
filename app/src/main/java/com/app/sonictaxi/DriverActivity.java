package com.app.sonictaxi;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Locale;

public class DriverActivity extends AppCompatActivity {

    private DatabaseReference database;
    private String currentRideId;
    private MapView mapView;
    private Marker destinationMarker;
    private final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";

    private final Handler locationHandler = new Handler();
    private final int LOCATION_UPDATE_INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_driver);

        mapView = findViewById(R.id.driver_mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        // إعداد المركز والزووم ليكون مطابقاً للباسنجر
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(32.2215, 35.2497));

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

        findViewById(R.id.btn_reject).setOnClickListener(v -> findViewById(R.id.request_card).setVisibility(View.GONE));
        findViewById(R.id.btn_logout).setOnClickListener(v -> logoutDriver());
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

                        // عرض الموقعين مع تجنب الـ Null
                        TextView tvDest = findViewById(R.id.tv_destination_display);
                        String pickup = (r.getPickupLocation() != null) ? r.getPickupLocation() : "غير محدد";
                        String dest = (r.getDestination() != null) ? r.getDestination() : "غير محدد";
                        tvDest.setText("من: " + pickup + "\nإلى: " + dest);

                        findViewById(R.id.request_card).setVisibility(View.VISIBLE);
                        setupDestination(dest);
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

    private void setupDestination(String destinationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(destinationName, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    GeoPoint point = new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    runOnUiThread(() -> {
                        // تحريك الكاميرا مرة واحدة فقط عند ظهور الطلب
                        mapView.getController().animateTo(point);

                        if (destinationMarker != null) mapView.getOverlays().remove(destinationMarker);
                        destinationMarker = new Marker(mapView);
                        destinationMarker.setPosition(point);
                        destinationMarker.setTitle("الوجهة: " + destinationName);
                        mapView.getOverlays().add(destinationMarker);
                        mapView.invalidate();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
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
                    Toast.makeText(DriverActivity.this, "تم إرسال العرض!", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.request_card).setVisibility(View.GONE);
                } else {
                    Toast.makeText(DriverActivity.this, "الطلب لم يعد متاحاً.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateDriverLocation() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String driverUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        double baseLat = 32.2215;
        double baseLng = 35.2497;
        double offset = System.currentTimeMillis() % 10000 / 1000000.0;

        database.child("DriversLocation").child(driverUid).setValue(new LocationData(baseLat + offset, baseLng + offset));
        locationHandler.postDelayed(this::updateDriverLocation, LOCATION_UPDATE_INTERVAL);
    }

    private void logoutDriver() {
        locationHandler.removeCallbacksAndMessages(null);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            database.child("DriversLocation").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        }
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHandler.removeCallbacksAndMessages(null);
    }
}