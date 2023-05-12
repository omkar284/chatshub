package com.example.whatsapp;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity
{
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();


        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(v -> SendUserToLoginActivity());

        CreateAccountButton.setOnClickListener(v -> CreateNewAccount());
    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar = new ProgressDialog(RegisterActivity.this);
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                        {

                            String currentUserId = mAuth.getCurrentUser().getUid();
                            RootRef.child("Users").child(currentUserId).setValue("");

                            SendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }


    private void InitializeFields()
    {
        CreateAccountButton =  findViewById(R.id.register_button);
        UserEmail =  findViewById(R.id.register_email);
        UserPassword =  findViewById(R.id.register_password);
        AlreadyHaveAccountLink =  findViewById(R.id.already_have_account_link);

    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

