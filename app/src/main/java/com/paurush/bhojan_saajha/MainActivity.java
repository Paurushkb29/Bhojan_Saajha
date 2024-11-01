package com.paurush.bhojan_saajha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import android.util.Log;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Check and request notification permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
        }

        // Initialize CardViews
        CardView donate = findViewById(R.id.cardDonate);
        CardView receive = findViewById(R.id.cardReceive);
        CardView logout = findViewById(R.id.cardLogout);
        CardView foodmap = findViewById(R.id.cardFoodmap);
        CardView mypin = findViewById(R.id.cardMyPin);
        CardView history = findViewById(R.id.cardHistory);
        CardView about = findViewById(R.id.cardAboutus);
        CardView contact = findViewById(R.id.cardContact);

        // Firebase authentication check
        if (fAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LandingPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            // Subscribe to notifications
            subscribeToNotifications();
        }

        // Set click listeners for each CardView
        donate.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Donate.class)));
        receive.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Receive.class)));
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LandingPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        foodmap.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FoodMap.class)));
        mypin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyPin.class)));
        history.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, History.class)));
        about.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, About.class)));
        contact.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Contact.class)));

        // Retrieve FCM Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM Token", "Token: " + token);
                        saveFcmToken(token);
                    } else {
                        Log.e("FCM Token", "Fetching FCM token failed", task.getException());
                    }
                });
    }

    private void subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("donations")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to donation notifications";
                    if (!task.isSuccessful()) {
                        msg = "Subscription failed";
                    }
                    Log.d("FCM", msg);
                });
    }

    private void saveFcmToken(String token) {
        String userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        fStore.collection("users").document(userID)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d("FCM Token", "Token saved successfully."))
                .addOnFailureListener(e -> Log.e("FCM Token", "Error saving token", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. You will not receive notifications.", Toast.LENGTH_LONG).show();
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("To receive real-time notifications, please enable notification permissions in the app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
