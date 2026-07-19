package com.app.sonictaxi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RatingActivity extends AppCompatActivity {

    private RatingBar rbPrice, rbSpeed;
    private Button btnFinish;
    private TextView tvDriverName;
    private DatabaseReference driverRef;

    private String driverId;
    private String driverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rating);

        // استقبال البيانات من الرحلة السابقة
        driverId = getIntent().getStringExtra("driver_id");
        driverName = getIntent().getStringExtra("driver_name");

        // إعداد الواجهات
        initViews();

        // عرض اسم السائق (لمسة احترافية)
        tvDriverName.setText("كيف كانت رحلتك مع السائق " + (driverName != null ? driverName : "سائقنا") + "؟");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnFinish.setOnClickListener(v -> submitRatings());
    }

    private void initViews() {
        rbPrice = findViewById(R.id.rb_price);
        rbSpeed = findViewById(R.id.rb_speed);
        btnFinish = findViewById(R.id.btn_finish);
        tvDriverName = findViewById(R.id.tv_title); // قمت باستخدام tv_title لعرض اسم السائق

        if (driverId != null) {
            driverRef = FirebaseDatabase.getInstance()
                    .getReference("Users/Drivers").child(driverId);
        }
    }

    private void submitRatings() {
        float priceRating = rbPrice.getRating();
        float speedRating = rbSpeed.getRating();

        if (priceRating == 0 || speedRating == 0) {
            Toast.makeText(this, "يرجى تقييم السعر والسرعة", Toast.LENGTH_SHORT).show();
            return;
        }

        if (driverRef != null) {

            DatabaseReference ratingRef = driverRef.child("ratings").push();
            ratingRef.child("price_rating").setValue(priceRating);
            ratingRef.child("speed_rating").setValue(speedRating);
            ratingRef.child("timestamp").setValue(System.currentTimeMillis());

            Toast.makeText(this, "شكراً لتقييمك، ساعدتَ في تحسين الخدمة!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}