package com.app.sonictaxi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.*;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class RideFragment extends Fragment {

    private MapView mapView;
    private Marker carMarker;
    private DatabaseReference driverRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride, container, false);

        // ربط الـ MapView من ملف الـ XML
        mapView = view.findViewById(R.id.ride_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // المسار الصحيح بناءً على هيكلية قاعدة بياناتك: Drivers -> status
        driverRef = FirebaseDatabase.getInstance().getReference("Drivers").child("status");

        listenToDriverLocation();

        return view;
    }

    private void listenToDriverLocation() {
        driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // قراءة القيم من الأعمدة الموجودة في داتابيسك: currentLat و currentLng
                Double lat = snapshot.child("currentLat").getValue(Double.class);
                Double lng = snapshot.child("currentLng").getValue(Double.class);

                if (lat != null && lng != null) {
                    GeoPoint carPos = new GeoPoint(lat, lng);

                    if (carMarker == null) {
                        carMarker = new Marker(mapView);
                        carMarker.setTitle("سونيك تاكسي");
                        mapView.getOverlays().add(carMarker);
                    }

                    carMarker.setPosition(carPos);
                    mapView.getController().animateTo(carPos);
                    mapView.invalidate(); // تحديث الخريطة
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}