package com.paurush.bhojan_saajha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Donate extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final int REQUEST_CODE = 11;
    private SupportMapFragment mapFragment;
    private EditText mFullName, mFoodItem, mDescription, mPhone;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    public static final String TAG = "TAG";

    // 1. Store selected location.
    private LatLng selectedLocation; // Variable to hold the selected location

    // 2. onCreate: Initialize UI, Firebase, and Map.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donate);

        mFullName = findViewById(R.id.donorname);
        mFoodItem = findViewById(R.id.fooditem);
        mPhone = findViewById(R.id.phone);
        mDescription = findViewById(R.id.description);
        Button mSubmitBtn = findViewById(R.id.submit);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        mSubmitBtn.setOnClickListener(v -> validateAndSubmitData());
    }

    // 3. Handle permission result.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapFragment.getMapAsync(this);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 4. Map is ready.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // 5. Set a map click listener to drop a marker at the selected location.
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng; // Store the selected location
            mMap.clear(); // Clear existing markers
            mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location")); // Add marker
        });
    }

    // 6. Build GoogleApiClient and connect.
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // 7. On successful connection, request location updates.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // 8. Location is updated.
    @Override
    public void onLocationChanged(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        Objects.requireNonNull(mMap.addMarker(markerOptions)).showInfoWindow();
    }

    // 9. Handle submit button click.
    private void validateAndSubmitData() {
        String fullname = mFullName.getText().toString().trim();
        String fooditem = mFoodItem.getText().toString().trim();
        String description = mDescription.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();

        if (validateInputs(fullname, fooditem, phone)) {
            userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

            if (selectedLocation != null) { // Use selected location instead of last location
                GeoPoint geoPoint = new GeoPoint(selectedLocation.latitude, selectedLocation.longitude);
                saveDonationData(fullname, fooditem, description, phone, geoPoint);
            } else {
                Toast.makeText(getApplicationContext(), "Please select a location!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 10. Validate inputs.
    private boolean validateInputs(String fullname, String fooditem, String phone) {
        if (TextUtils.isEmpty(fullname)) {
            mFullName.setError("Name is Required.");
            return false;
        }
        if (TextUtils.isEmpty(fooditem)) {
            mFoodItem.setError("Food item is required.");
            return false;
        }
        if (TextUtils.isEmpty(phone) || phone.length() != 10 || !phone.matches("\\d{10}")) {
            mPhone.setError("Phone Number must be exactly 10 digits and contain only numbers.");
            return false;
        }
        return true;
    }

    // 11. Save donation data to Firestore.
    private void saveDonationData(String fullname, String fooditem, String description, String phone, GeoPoint geoPoint) {
        Map<String, Object> user = new HashMap<>();
        user.put("timestamp", FieldValue.serverTimestamp());
        user.put("name", fullname);
        user.put("food item", fooditem);
        user.put("phone", phone);
        user.put("description", description);
        user.put("location", geoPoint);
        user.put("userid", userID);
        user.put("type", "Donor");

        CollectionReference collectionReference = fStore.collection("user data");
        collectionReference.add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Submit Successful!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Donation data saved successfully!");

                    clearFields();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error saving donation data!", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error saving donation data", e);
                });
    }

    // 12. Clear fields after successful submission.
    private void clearFields() {
        mFullName.setText("");
        mFoodItem.setText("");
        mDescription.setText("");
        mPhone.setText("");
    }

    // 13. Navigate to MainActivity.
    private void navigateToMainActivity() {
        Intent intent = new Intent(Donate.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // 14. Handle connection suspended event.
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended, attempting to reconnect...");
        mGoogleApiClient.connect(); // Attempt to reconnect
    }

    // 15. Handle connection failed event.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Connection failed: " + connectionResult.getErrorMessage());
        Toast.makeText(this, "Connection to Google Play services failed.", Toast.LENGTH_SHORT).show();
    }
}
