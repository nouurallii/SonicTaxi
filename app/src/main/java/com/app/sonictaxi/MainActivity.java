package com.app.sonictaxi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private final String DB_URL = "https://sonic-taxi-3692e-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // التحقق من حالة المستخدم فوراً
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // توجيه المستخدم لصفحة تسجيل الدخول إذا لم يكن مسجلاً
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // فحص نوع الحساب وتوجيهه للمكان الصحيح
            checkUserRoleAndRedirect(currentUser.getUid());

        }
    }

    private void checkUserRoleAndRedirect(String uid) {
        // نصل للمسار الأساسي للمستخدم في Users
        DatabaseReference userRef = FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // قراءة الدور من الداتابيس
                    String role = snapshot.child("role").getValue(String.class);

                    if (role != null) {
                        if (role.equals("driver")) {
                            startActivity(new Intent(MainActivity.this, DriverActivity.class));
                        } else if (role.equals("manager")) {
                            startActivity(new Intent(MainActivity.this, ManagerActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, PassengerActivity.class));
                        }
                    } else {
                        // إذا كان الحقل فارغاً
                        startActivity(new Intent(MainActivity.this, PassengerActivity.class));
                    }
                } else {
                    // إذا لم يكن المستخدم موجوداً في Users، نرسله للراكب
                    startActivity(new Intent(MainActivity.this, PassengerActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "فشل الاتصال: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
