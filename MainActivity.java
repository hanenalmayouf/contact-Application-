package com.app.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView my_account, view_contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        my_account = findViewById(R.id.my_account);
        view_contacts = findViewById(R.id.view_contacts);

        Bundle bundle = getIntent().getExtras();

        my_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyAccount.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        view_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewContacts.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }
}