package com.paurush.bhojan_saajha;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {

    // Constant tag for logging
    public static final String TAG = "TAG";

    // Declare UI components for user input fields
    private EditText mFullName, mEmail, mPassword, mPhone;

    // Declare Firebase authentication and Firestore database instances
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    // Variable to store the user ID
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge mode for the activity layout
        EdgeToEdge.enable(this);

        // Set the layout for the activity
        setContentView(R.layout.activity_signup);

        // Initialize input fields and buttons by mapping to their XML IDs
        mFullName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        Button mRegisterBtn = findViewById(R.id.register);
        TextView mLoginBtn = findViewById(R.id.login);

        // Initialize FirebaseAuth and FirebaseFirestore instances
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Check if a user is already logged in
        if (fAuth.getCurrentUser() != null) {
            // Redirect to MainActivity if user is already logged in
            Intent intent = new Intent(SignUp.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        // Set click listener for the Register button
        mRegisterBtn.setOnClickListener(v -> {
            // Get user input from the fields
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String name = mFullName.getText().toString().trim();
            String phone = mPhone.getText().toString().trim();

            // Validate that email is not empty
            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required.");
                return;
            }

            // Validate that password is not empty
            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required.");
                return;
            }

            // Check if password length is at least 6 characters
            if (password.length() < 6) {
                mPassword.setError("Password Must be >=6 Characters");
                return;
            }

            // Create a new user with email and password in Firebase Authentication
            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Show a success message on user creation
                    Toast.makeText(SignUp.this, "User Created.", Toast.LENGTH_SHORT).show();

                    // Retrieve the current user's unique ID
                    userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

                    // Create a document reference in Firestore for the new user
                    DocumentReference documentReference = fStore.collection("users").document(userID);

                    // Prepare user data to be saved in Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("email", email);
                    user.put("phone", phone);

                    // Save user data in Firestore
                    documentReference.set(user).addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "onSuccess: User profile is created for " + userID);
                        Toast.makeText(SignUp.this, "Registered Successfully.", Toast.LENGTH_SHORT).show();
                    });

                    // Redirect to MainActivity after successful registration
                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Display error message if registration fails
                    Toast.makeText(SignUp.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set click listener for the Login button
        mLoginBtn.setOnClickListener(v ->
                // Redirect to LogUp activity when login text is clicked
                startActivity(new Intent(getApplicationContext(), LogUp.class))
        );
    }
}
