package com.paurush.bhojan_saajha;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class History extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference notebookRef = db.collection("user data");
    public static final String TAG = "TAG";
    private LinearLayout showDataLayout;
    private FirebaseAuth fAuth;
    private final List<String> selectedIds = new ArrayList<>(); // To store IDs of selected items
    private final Map<String, CheckBox> checkBoxMap = new HashMap<>(); // To store checkboxes for items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        fAuth = FirebaseAuth.getInstance();
        showDataLayout = findViewById(R.id.showdata);

        findViewById(R.id.delete).setOnClickListener(v -> deleteSelectedNotes());

        // Set up the "Select All" checkbox
        CheckBox selectAllCheckBox = findViewById(R.id.select_all);
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox checkBox : checkBoxMap.values()) {
                checkBox.setChecked(isChecked);
                if (isChecked) {
                    selectedIds.add(checkBox.getText().toString()); // Update selection
                } else {
                    selectedIds.clear(); // Clear selection if unchecked
                }
            }
        });

        loadNotes();
    }

    public void loadNotes() {
        notebookRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userID = fAuth.getCurrentUser() != null ? fAuth.getCurrentUser().getUid() : null;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            if (document.contains("name") && document.contains("description")
                                    && document.contains("userid") && document.contains("timestamp")) {

                                String name = document.getString("name");
                                String description = document.getString("description");
                                String userIdFromDoc = document.getString("userid");
                                Timestamp timestamp = document.getTimestamp("timestamp");

                                // Check if the current user ID matches
                                if (userIdFromDoc != null && userIdFromDoc.equals(userID)) {
                                    addCheckBoxToLayout(document.getId(), name, description, timestamp);
                                }
                            }
                        }

                        // If no data is found, display the no data message properly centered
                        if (showDataLayout.getChildCount() == 0) {
                            TextView noDataText = new TextView(this);
                            noDataText.setText(R.string.no_donation_records);
                            noDataText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                            noDataText.setTextSize(18); // Adjust text size as needed
                            noDataText.setGravity(android.view.Gravity.CENTER); // Center the text horizontally

                            // Set layout parameters to ensure proper margins and width
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(20, 20, 20, 20); // Add some margin around the text
                            noDataText.setLayoutParams(params);

                            showDataLayout.addView(noDataText);
                        }
                    } else {
                        Log.d(TAG, "Error fetching data: ", task.getException());
                    }
                });
    }


    private void addCheckBoxToLayout(String documentId, String name, String description, Timestamp timestamp) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Format the timestamp if needed
        String dateAndTime = formatTimestamp(timestamp);

        // Create a new checkbox with white text to be visible on the black background
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(getString(R.string.checkbox_text, name, description, dateAndTime));
        checkBox.setTextColor(ContextCompat.getColor(this, android.R.color.white)); // Set text color for visibility

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(documentId);
            } else {
                selectedIds.remove(documentId);
            }
        });

        checkBoxMap.put(documentId, checkBox); // Store checkbox reference
        layout.addView(checkBox);

        showDataLayout.addView(layout);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(timestamp.toDate());
        }
        return "No Date"; // Fallback if timestamp is null
    }

    private void deleteSelectedNotes() {
        for (String id : selectedIds) {
            notebookRef.document(id).delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
        }

        // Clear selections
        selectedIds.clear();
        showDataLayout.removeAllViews(); // Clear the layout and reload notes
        loadNotes();
    }
}
