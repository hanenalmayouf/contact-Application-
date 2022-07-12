package com.app.contacts;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.HashMap;

public class MyAccount extends AppCompatActivity {
    private String strUsername, strPassword ;
    private EditText userName, password, name, address;
    private Button update_user;
    private TextView login;
    private ImageView userImage;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        password = findViewById(R.id.password);
        userName = findViewById(R.id.userName);
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        login = findViewById(R.id.login);
        update_user = findViewById(R.id.update_user);
        userImage = findViewById(R.id.userImage);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Edit user");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        Bundle bundle = getIntent().getExtras();
        strUsername = bundle.getString("username");


        DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference("User");
        Query query = usersDbRef.orderByChild("userName").equalTo(strUsername);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String quserName = "" + ds.child("userName").getValue();
                    String qpassword = "" + ds.child("password").getValue();
                    String qname = "" + ds.child("name").getValue();
                    String qaddress = "" + ds.child("address").getValue();
                    String quserImage = "" + ds.child("userImage").getValue();

                    if (quserName.equals(strUsername)) {
                        userName.setText(quserName);
                        password.setText(qpassword);
                        name.setText(qname);
                        address.setText(qaddress);
                        if(!quserImage.isEmpty()) {
                            new MyAccount.viewImage(userImage).execute(quserImage);
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        userImage.setOnClickListener(new View.OnClickListener() {
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
                            userImage.setImageURI(uri);

                        }
                    }
                });

        update_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEmpty()) {
                    submitForm();
                    Toast.makeText(getApplication(), "User Added", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplication(), "Complete Data", Toast.LENGTH_LONG).show();
            }
        });


    }

    private class viewImage extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;
        public viewImage(ImageView imageView) {
            this.imageView=imageView;
            //Toast.makeText(UpdateProducts.this, "Please wait, it may take a few minute...",Toast.LENGTH_SHORT).show();
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    private void submitForm() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userName", userName.getText().toString());
        hashMap.put("password", password.getText().toString());
        hashMap.put("name", name.getText().toString());
        hashMap.put("address", address.getText().toString());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(userName.getText().toString()).updateChildren(hashMap);
        if (uri != null){
            storeImage(uri);
        }
    }

    private void storeImage(Uri uri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference ImageReference = storageReference.child("User").child(userName.getText().toString())
                .child(userName.getText().toString());
        ImageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while ((!uriTask.isSuccessful())) ;
                Uri downloadUri = uriTask.getResult();
                if (uriTask.isSuccessful()) {
                    HashMap<String, Object> results = new HashMap<>();
                    results.put("userImage", downloadUri.toString());
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User").child(userName.getText().toString());
                    reference.updateChildren(results);
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
        if (userName.getText().toString().length() == 0) return true;
        if (password.getText().toString().length() == 0) return true;
        if (name.getText().toString().length() == 0) return true;
        if (address.getText().toString().length() == 0) return true;
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