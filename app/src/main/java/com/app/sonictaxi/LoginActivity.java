package com.app.sonictaxi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // ربط العناصر
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGuestLogin = findViewById(R.id.btnGuestLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 1. تسجيل الدخول
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "يرجى تعبئة الحقول", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                checkUserRoleAndRedirect(mAuth.getCurrentUser().getUid());
                            } else {
                                Toast.makeText(this, "خطأ: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // 2. لتنقل لصفحة التسجيل
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        // 3. تنقل لصفحة استعادة كلمة المرور
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));

        // 4. دخول كضيف
        btnGuestLogin.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, PassengerActivity.class));
            finish();
        });
    }

    // دالة المحدثة والموحدة للتحقق من الدور
    private void checkUserRoleAndRedirect(String uid) {

        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            String role = snapshot.child("role").getValue(String.class);

                            if ("driver".equals(role)) {
                                startActivity(new Intent(LoginActivity.this, DriverActivity.class));
                            } else if ("manager".equals(role)) {
                                startActivity(new Intent(LoginActivity.this, ManagerActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, PassengerActivity.class));
                            }
                        } else {
                            // في حال عدم وجود بيانات المستخدم، التوجيه الافتراضي للراكب
                            startActivity(new Intent(LoginActivity.this, PassengerActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "فشل الاتصال: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}