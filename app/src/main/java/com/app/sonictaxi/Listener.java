package com.app.sonictaxi;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Listener {
    private static ValueEventListener locationListener;
    private static DatabaseReference currentRef;
    private static final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface OnDataReceivedListener {
        void onDataReceived(DataSnapshot snapshot);
        void onError(DatabaseError error);
    }

    public static void listenToDriverLocation(String driverId, final OnDataReceivedListener listener) {
        // التأكد من إيقاف أي استماع سابق قبل البدء بمسار جديد
        stopListening();

        currentRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("DriversLocation").child(driverId);

        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onDataReceived(snapshot);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error);
            }
        };

        currentRef.addValueEventListener(locationListener);
    }

    public static void stopListening() {
        if (currentRef != null && locationListener != null) {
            currentRef.removeEventListener(locationListener);
            locationListener = null;
            currentRef = null;
        }
    }
}