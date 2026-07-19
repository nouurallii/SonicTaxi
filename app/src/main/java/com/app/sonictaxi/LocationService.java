package com.app.sonictaxi;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference driversRef;
    private final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";
    private final String CHANNEL_ID = "sonictaxi_loc_channel"; // تم توحيد الـ ID

    @Override
    public void onCreate() {
        super.onCreate();
        // استخدام رابط قاعدة البيانات الموحد
        driversRef = FirebaseDatabase.getInstance(DB_URL).getReference("DriversLocation");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // استخدام UID الديناميكي بدلاً من الـ Hardcoded ID
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    double lat = locationResult.getLastLocation().getLatitude();
                    double lng = locationResult.getLastLocation().getLongitude();

                    Map<String, Object> locationMap = new HashMap<>();
                    locationMap.put("lat", lat);
                    locationMap.put("lng", lng);

                    driversRef.child(userId).updateChildren(locationMap);
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        // استخدام الـ CHANNEL_ID الموحد هنا
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sonic Taxi")
                .setContentText("أنت متصل وتشارك موقعك الحالي..")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();

        startForeground(1, notification);

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "تتبع موقع Sonic Taxi", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}