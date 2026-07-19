package com.app.sonictaxi;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, etOfficeName, etOfficeCode;
    private RadioGroup rgRole;
    private LinearLayout llOfficeDetails, llDriverDetails;
    private Button btnSignUp;
    private TextView tvLoginRedirect;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etOfficeName = findViewById(R.id.etOfficeName);
        etOfficeCode = findViewById(R.id.etOfficeCode);
        rgRole = findViewById(R.id.rgRole);
        llOfficeDetails = findViewById(R.id.llOfficeDetails);
        llDriverDetails = findViewById(R.id.llDriverDetails);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLoginRedirect = findViewById(R.id.tv_login_redirect);

        // إظهار وإخفاء الحقول بناءً على الاختيار
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOffice) {
                llOfficeDetails.setVisibility(View.VISIBLE);
                llDriverDetails.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbDriver) {
                llDriverDetails.setVisibility(View.VISIBLE);
                llOfficeDetails.setVisibility(View.GONE);
            } else {
                llOfficeDetails.setVisibility(View.GONE);
                llDriverDetails.setVisibility(View.GONE);
            }
        });

        tvLoginRedirect.setPaintFlags(tvLoginRedirect.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يرجى تعبئة الحقول الأساسية", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("كلمة المرور يجب أن تكون 6 خانات على الأقل");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("كلمات المرور غير متطابقة");
            return;
        }

        int selectedId = rgRole.getCheckedRadioButtonId();
        String role;

        if (selectedId == R.id.rbDriver) {
            role = "driver";
        } else if (selectedId == R.id.rbOffice) {
            role = "manager";
        } else if (selectedId == R.id.rbCustomer) {
            role = "customer";
        } else {
            Toast.makeText(this, "يرجى اختيار نوع الحساب", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("role", role);

                        if (role.equals("manager")) {
                            userMap.put("officeName", etOfficeName.getText().toString().trim());
                        } else if (role.equals("driver")) {
                            userMap.put("officeCode", etOfficeCode.getText().toString().trim());
                        }

                        FirebaseDatabase.getInstance("https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference("Users").child(userId).setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "تم التسجيل بنجاح!", Toast.LENGTH_SHORT).show();

                                        Intent intent;
                                        if (role.equals("driver")) {
                                            intent = new Intent(this, DriverActivity.class);
                                        } else if (role.equals("manager")) {
                                            intent = new Intent(this, ManagerActivity.class);
                                        } else {
                                            intent = new Intent(this, PassengerActivity.class);
                                        }
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "فشل حفظ البيانات: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "خطأ: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}