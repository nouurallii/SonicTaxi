package com.app.sonictaxi;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btn_driver_entry).setOnClickListener(v ->
                navigateToLogin("Driver"));

        findViewById(R.id.btn_manager_entry).setOnClickListener(v ->
                navigateToLogin("Manager"));

        findViewById(R.id.btn_customer_entry).setOnClickListener(v ->
                navigateToLogin("Passenger"));
    }

    private void navigateToLogin(String role) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }
}