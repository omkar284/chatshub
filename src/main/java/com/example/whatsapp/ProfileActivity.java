package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID,Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, DeclineMessageRequestButton;
    private DatabaseReference UserRef, chatRequestRef, contactRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton =  (Button) findViewById(R.id.decline_message_request_button);
        Current_State = "new";


        RetrieveUserInfo();



    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("profilepic")))
                {
                    String userImage = dataSnapshot.child("profilepic").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    ManageChatRequest();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                Current_State = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                Current_State = "request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                Current_State = "friends";
                                                sendMessageRequestButton.setText("Remove this Contacts");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(receiverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }


    private void RemoveSpecificContact() {
        contactRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void AcceptChatRequest() {
        contactRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    sendMessageRequestButton.setText("Remove this Contact");
                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void CancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void sendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "request_sent";
                                                sendMessageRequestButton.setText("Cancel Chat Request");
                                            }

                                        }
                                    });
                        }
                    }
                });
    }
}