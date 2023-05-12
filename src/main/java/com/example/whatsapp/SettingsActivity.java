package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick = 1;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private ProgressDialog loadingBar;
    private androidx.appcompat.widget.Toolbar SettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();


        InitializeFields();


        userName.setVisibility(View.INVISIBLE);
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });



        RetrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick );
            }
        });
    }




  private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
         SettingsToolBar= (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getData() !=null)
        {
            loadingBar.setTitle("Set Profile Image");
            loadingBar.setMessage("Please wait, your profile image is updated...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Uri sFile =data.getData();
            userProfileImage.setImageURI(sFile);

            final StorageReference StorageRef = storage.getReference().child("profile_pictures")
                    .child(FirebaseAuth.getInstance().getUid());

            StorageRef.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                    .child("profilepic").setValue(uri.toString());
                            Toast.makeText(SettingsActivity.this, "Image Save successfully", Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();


                        }
                    });
                }
            });
        }
    }






    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();



        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write your name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String,String> profileMap = new HashMap<>();
                profileMap.put("uid",currentUserID);
                profileMap.put("name",setUserName);
                profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully....", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: " +message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

    }


    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot datasnapshot) {

                        if ((datasnapshot.exists()) && (datasnapshot.hasChild("name") && (datasnapshot.hasChild("profilepic"))))
                        {
                            String retrieveUserName = datasnapshot.child("name").getValue().toString();
                            String retrieveStatus = datasnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = datasnapshot.child("profilepic").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        }
                        else if ((datasnapshot.exists()) && (datasnapshot.hasChild("name")))
                        {
                            String retrieveUserName = datasnapshot.child("name").getValue().toString();
                            String retrieveStatus = datasnapshot.child("status").getValue().toString();


                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}