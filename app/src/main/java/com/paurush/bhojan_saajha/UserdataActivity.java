package com.paurush.bhojan_saajha;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserdataActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<model> datalist;
    FirebaseFirestore db;
    MyAdapter adapter;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    public String userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userdata);

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        datalist = new ArrayList<>();
        adapter = new MyAdapter(datalist);
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch user data from Firestore
        db.collection("user data").orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    int initialSize = datalist.size(); // Store initial size of the datalist

                    for (DocumentSnapshot d : list) {
                        model obj = d.toObject(model.class);
                        String Userid = (String) d.get("userid");
                        assert Userid != null;
                        if (Userid.equals(userID)) {
                            datalist.add(obj); // Add object to datalist
                        }
                    }

                    int newSize = datalist.size(); // Get new size after additions
                    // Notify adapter of the new items added
                    adapter.notifyItemRangeInserted(initialSize, newSize - initialSize);
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }
}
