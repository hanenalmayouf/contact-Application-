package com.app.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private String strUsername, strPassword ;
    private EditText userName, password;
    private Button login;
    private TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.password);
        userName = findViewById(R.id.userName);
        register = findViewById(R.id.register);
        login = findViewById(R.id.btn_signin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strUsername = userName.getText().toString();
                strPassword = password.getText().toString();
                if (strUsername.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), "Complete Data", Toast.LENGTH_LONG).show();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
                    reference.child(strUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String storedPassword = snapshot.child("password").getValue(String.class);
                                String name = snapshot.child("name").getValue(String.class);
                                if(storedPassword.equals(strPassword)){
                                    Bundle bundle = new Bundle();
                                    bundle.putString("username", strUsername);
                                    bundle.putString("name", name);
                                    Intent intent = new Intent(getApplication(), MainActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();

                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Password error", Toast.LENGTH_LONG).show();
                                }
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });
    }
}
