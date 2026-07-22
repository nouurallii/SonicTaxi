package com.app.sonictaxi;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Locale;

public class PassengerActivity extends AppCompatActivity {

    private MapView mapView;
    private Marker driverMarker;
    private TextView tvPriceDisplay, tvStatus;
    private EditText etDestination;
    private Button btnAcceptPrice, btnRequestTaxi;
    private String currentRideId;

    // عناصر القائمة الجانبية والشريط السفلي
    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private NavigationView navView;
    private BottomNavigationView bottomNavigation;

    private DatabaseReference ridesRef;
    private DatabaseReference currentRideRef;

    private final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_passenger);

        initViews();

        ridesRef = FirebaseDatabase.getInstance(DB_URL).getReference("Rides");

        btnRequestTaxi.setOnClickListener(v -> {
            String destination = etDestination.getText().toString().trim();
            if (destination.isEmpty()) {
                etDestination.setError("يرجى إدخال الوجهة!");
                return;
            }
            searchAndGo(destination);
        });
    }

    private void initViews() {
        // ربط عناصر الخريطة والطلب
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(32.2215, 35.2497)); // إحداثيات نابلس الافتراضية

        tvPriceDisplay = findViewById(R.id.tv_price_display);
        tvStatus = findViewById(R.id.status_text);
        etDestination = findViewById(R.id.destination_search);
        btnAcceptPrice = findViewById(R.id.btn_accept_price);
        btnRequestTaxi = findViewById(R.id.btn_request_taxi);

        // ربط عناصر واجهة الـ DrawerLayout والقوائم
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btnMenu);
        navView = findViewById(R.id.nav_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // تفعيل زر القائمة الجانبية لفتحها وإغلاقها
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // الاستماع لاختيارات القائمة الجانبية
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }

        // الاستماع لاختيارات الشريط السفلي
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                return true;
            });
        }
    }

    private void searchAndGo(final String locationName) {
        tvStatus.setText("جاري تحديد موقع: " + locationName);
        View statusCard = findViewById(R.id.status_card);
        if (statusCard != null) {
            statusCard.setVisibility(View.VISIBLE);
        }
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    final Address address = addresses.get(0);
                    final GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                    runOnUiThread(() -> {
                        mapView.getController().animateTo(point);
                        Marker destMarker = new Marker(mapView);
                        destMarker.setPosition(point);
                        mapView.getOverlays().add(destMarker);
                        mapView.invalidate();
                        proceedWithRideRequest(locationName);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void proceedWithRideRequest(String destination) {
        currentRideId = ridesRef.push().getKey();
        currentRideRef = ridesRef.child(currentRideId);
        currentRideRef.child("status").setValue("pending");
        currentRideRef.child("destination").setValue(destination);
        btnRequestTaxi.setEnabled(false);
        listenForDriverOffer();
    }

    private void listenForDriverOffer() {
        currentRideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String status = snapshot.child("status").getValue(String.class);

                // 1. حالة العرض
                if ("offered".equals(status)) {
                    Double price = snapshot.child("offerPrice").getValue(Double.class);
                    tvPriceDisplay.setText("السعر: " + (price != null ? price : 0) + " شيكل");
                    btnAcceptPrice.setVisibility(View.VISIBLE);
                }
                // 2. حالة انتهاء الرحلة -> الانتقال للتقييم
                else if ("finished".equals(status)) {
                    String driverId = snapshot.child("driverId").getValue(String.class);
                    Intent intent = new Intent(PassengerActivity.this, RatingActivity.class);
                    intent.putExtra("driver_id", driverId);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnAcceptPrice.setOnClickListener(v -> {
            currentRideRef.child("status").setValue("accepted");
            currentRideRef.child("driverId").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String driverId = snapshot.getValue(String.class);
                    if (driverId != null) startTrackingAcceptedDriver(driverId);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
            btnAcceptPrice.setVisibility(View.GONE);
        });
    }

    private void startTrackingAcceptedDriver(String driverId) {
        Listener.listenToDriverLocation(driverId, new Listener.OnDataReceivedListener() {
            @Override
            public void onDataReceived(DataSnapshot snapshot) {
                if (snapshot.hasChild("lat") && snapshot.hasChild("lng")) {
                    double lat = snapshot.child("lat").getValue(Double.class);
                    double lng = snapshot.child("lng").getValue(Double.class);
                    if (driverMarker == null) {
                        driverMarker = new Marker(mapView);
                        mapView.getOverlays().add(driverMarker);
                    }
                    driverMarker.setPosition(new GeoPoint(lat, lng));
                    mapView.invalidate();
                }
            }
            @Override
            public void onError(DatabaseError e) {}
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Listener.stopListening();
    }
}