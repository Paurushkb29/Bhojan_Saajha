package com.paurush.bhojan_saajha;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LogUp extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logup);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        TextView mRegisterBtn = findViewById(R.id.register);
        Button mLoginBtn = findViewById(R.id.login);

        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() !=null){
            Intent intent = new Intent(LogUp.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        mLoginBtn.setOnClickListener(v -> {

            String email = mEmail.getText().toString().trim();
            String password= mPassword.getText().toString().trim();

            if(TextUtils.isEmpty(email))
            {
                mEmail.setError("Email is Required.");
                return;
            }

            if(TextUtils.isEmpty(password))
            {
                mPassword.setError("Password is Required.");
                return;
            }

            if(password.length() < 6)
            {
                mPassword.setError("Password Must be >=6 Characters");
                return;
            }

            //authenticate the user
            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(LogUp.this, "Logged in Successfully.", Toast.LENGTH_SHORT) .show();
                    Intent intent = new Intent(LogUp.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(LogUp.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
            //startActivity(new Intent(LogUp.this, MainActivity.class));
        });

        mRegisterBtn.setOnClickListener(v -> {
            // redirect to RegisterActivity
            startActivity(new Intent(LogUp.this, MainActivity.class));
        });

    }
}