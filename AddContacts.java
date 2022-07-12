package com.app.contacts;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddContacts extends AppCompatActivity {
    private String strUsername, strName ;
    private EditText call_number, name;
    private Button add_contacts;
    private ImageView image;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        call_number = findViewById(R.id.call_number);
        name = findViewById(R.id.name);
        add_contacts = findViewById(R.id.add_contacts);
        image = findViewById(R.id.image);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Contacts");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        strUsername = bundle.getString("username");
        strName = bundle.getString("name");

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                activityResultLauncher.launch(galleryIntent);
            }
        });

        activityResultLauncher= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode()  == RESULT_OK && result.getData() != null){
                            uri = result.getData().getData();
                            image.setImageURI(uri);

                        }
                    }
                });
        add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEmpty()) {
                    saveData();
                    Toast.makeText(getApplication(), "User Added", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplication(), "Complete Data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveData() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("username", strUsername);
        hashMap.put("name", name.getText().toString());
        hashMap.put("callNumber", call_number.getText().toString());
        hashMap.put("image", "");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts");
        reference.child(strUsername).child(call_number.getText().toString()).setValue(hashMap);
        if (uri != null){
            storeImage(uri);
        }
    }

    private void storeImage(Uri uri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference ImageReference = storageReference.child("Contacts").child(strUsername)
                .child(call_number.getText().toString());
        ImageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while ((!uriTask.isSuccessful())) ;
                Uri downloadUri = uriTask.getResult();
                if (uriTask.isSuccessful()) {
                    HashMap<String, Object> results = new HashMap<>();
                    results.put("image", downloadUri.toString());
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Contacts").child(strUsername);
                    reference.child(call_number.getText().toString()).updateChildren(results);
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isEmpty() {
        if (call_number.getText().toString().length() == 0) return true;
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}