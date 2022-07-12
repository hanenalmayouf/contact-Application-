package com.app.contacts;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.HashMap;

public class ViewContacts extends AppCompatActivity {
    Bundle bundle = new Bundle();
    String username, name;
    ImageView add_contacts;
    ViewContacts.CustomListAdapter customListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contacts);

        add_contacts = findViewById(R.id.add_contacts);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("All Contacts");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bundle = getIntent().getExtras();
        username = bundle.getString("username");
        name = bundle.getString("name");

        add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), AddContacts.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        ListView listView = findViewById(R.id.list_view);
        ArrayList<ContactsListView> contactsListViews = new ArrayList<ContactsListView>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Contacts").child(username);
        Query query = dbRef.orderByChild("username").equalTo(username);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsListViews.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String username = ds.child("username").getValue().toString();
                    String name = ds.child("name").getValue().toString();
                    String callNumber = ds.child("callNumber").getValue().toString();
                    String image = "" + ds.child("image").getValue();

                    contactsListViews.add(new ContactsListView(username, name, callNumber, image));
                }
                customListAdapter = new ViewContacts.CustomListAdapter(contactsListViews);
                listView.setAdapter(customListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
    class CustomListAdapter extends BaseAdapter {
        private ArrayList<ContactsListView> classOfListItem = new ArrayList<ContactsListView>();

        //public constructor
        CustomListAdapter(ArrayList<ContactsListView> listItems) {
            this.classOfListItem = listItems;
        }

        @Override
        public int getCount() {
            return classOfListItem.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return classOfListItem.get(position).fullName; //returns list item at the specified position
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.list_view_items, null);
            final TextView user_name = view.findViewById(R.id.user_name);
            final TextView call_number = view.findViewById(R.id.call_number);
            final TextView name = view.findViewById(R.id.name);
            final ImageView imagePath = view.findViewById(R.id.image_name);

            call_number.setText(classOfListItem.get(position).callNumber);
            name.setText(classOfListItem.get(position).fullName);
            user_name.setText(classOfListItem.get(position).username);

            if(!classOfListItem.get(position).imagePath.isEmpty()) {
                new viewImage(imagePath).execute(classOfListItem.get(position).imagePath);
            }

            ImageView call = view.findViewById(R.id.call);
            ImageView delete = view.findViewById(R.id.delete);
            ImageView message = view.findViewById(R.id.message);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplication(),drug_code.getText(),Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewContacts.this);
                    builder.setMessage("Delete: " + name.getText() + "?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {



                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contacts").child(username)
                                            .child(call_number.getText().toString());
                                    ref.removeValue();

                                    if(!classOfListItem.get(position).imagePath.isEmpty()) {
                                        StorageReference deleteImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(classOfListItem.get(position).imagePath);
                                        deleteImageRef.delete();
                                        classOfListItem.remove(position);
                                        customListAdapter.notifyDataSetChanged();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            });
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String urlPhoneNum = "tel:" + call_number.getText().toString();
                    Uri uriPhoneNum = Uri.parse(urlPhoneNum);
                    Intent intentPhoneNum = new Intent(Intent.ACTION_DIAL, uriPhoneNum);
                    startActivity(intentPhoneNum);
                }
            });
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "smsto:" + call_number.getText().toString();
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    startActivity(intent);
                }
            });
            return view;
        }
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


}