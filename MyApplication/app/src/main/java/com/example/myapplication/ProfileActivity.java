package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    private CircleImageView findFriendsProfileImage;
    private TextView findFriendsProfileName, findFriendsProfileStatus;
    private Button findFriendsRequestBtn, findFriendsDenidedbtn;

    private DatabaseReference userReference, addFriendsRequest, contactFriends;
    private FirebaseAuth myAuth;

    private String receiverUserId, currentUserId, currentUserState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        myAuth = FirebaseAuth.getInstance();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        addFriendsRequest = FirebaseDatabase.getInstance().getReference().child("Requests");
        contactFriends = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId = getIntent().getExtras().get("idUserContact").toString();
        currentUserId = myAuth.getCurrentUser().getUid();
        currentUserState = "new";

        findFriendsProfileImage =  (CircleImageView) findViewById(R.id.find_friends_profile_image);
        findFriendsProfileName = (TextView) findViewById(R.id.find_friends_profile_name);
        findFriendsProfileStatus = (TextView) findViewById(R.id.find_friends_profile_status);
        findFriendsRequestBtn = (Button) findViewById(R.id.find_friends_profile_request_btn);
        findFriendsDenidedbtn = (Button) findViewById(R.id.find_friends_profile_denided_btn);
        
        RetrieveUserInfor();
    }


    private void RetrieveUserInfor() {
        userReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    findFriendsProfileName.setText(userName);
                    findFriendsProfileStatus.setText(userStatus);

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(findFriendsProfileImage);

                    ManagermentRequest();
                }
                else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    findFriendsProfileName.setText(userName);
                    findFriendsProfileStatus.setText(userStatus);

                    ManagermentRequest();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(ProfileActivity.this, FindFriendsActivity.class);
        findFriendsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFriendsIntent);
        finish();
    }

    private void ManagermentRequest(){
        addFriendsRequest.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)){
                    String requestType = dataSnapshot.child(receiverUserId).child("requestType").getValue().toString();

                    if(requestType.equals("addSend")){
                        currentUserState = "add_friends_request";
                        findFriendsRequestBtn.setText("Cancel Add Friends");
                    }
                    else if(requestType.equals("addReceiver")){
                        currentUserState = "add_friends_receiver";
                        findFriendsRequestBtn.setText("Acccept");

                        findFriendsDenidedbtn.setVisibility(View.VISIBLE);
                        findFriendsDenidedbtn.setEnabled(true);

                        findFriendsDenidedbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelAddFriendsRequest();
                            }
                        });
                    }
                }
                else{
                    contactFriends.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId)){
                                currentUserState = "friends";
                                findFriendsRequestBtn.setText("Unfriends");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if(!currentUserId.equals(receiverUserId)){
            findFriendsRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findFriendsRequestBtn.setEnabled(false);
                    if(currentUserState.equals("new")){
                        AddFriendsRequest();
                    }
                    if(currentUserState.equals("add_friends_request")){
                        CancelAddFriendsRequest();
                    }
                    if(currentUserState.equals("add_friends_receiver")){
                        AcceptAddFriendsRequest();
                    }
                    if(currentUserState.equals("friends")){
                        Unfriends();
                    }
                }
            });
        }
        else{
            findFriendsRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void AddFriendsRequest(){
        addFriendsRequest.child(currentUserId).child(receiverUserId).child("requestType").setValue("addSend").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    addFriendsRequest.child(receiverUserId).child(currentUserId)
                            .child("requestType").setValue("addReceiver")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    findFriendsRequestBtn.setEnabled(true);
                                    currentUserState = "add_friends_request";
                                    findFriendsRequestBtn.setText("Cancel Add Friends");
                                }
                            });
                }
            }
        });

    }

    private void CancelAddFriendsRequest(){
        addFriendsRequest.child(currentUserId).child(receiverUserId).child("requestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    addFriendsRequest.child(receiverUserId).child(currentUserId)
                            .child("requestType").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        findFriendsRequestBtn.setEnabled(true);
                                        currentUserState = "new";
                                        findFriendsRequestBtn.setText("Add Friends");

                                        findFriendsDenidedbtn.setVisibility(View.INVISIBLE);
                                        findFriendsDenidedbtn.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void AcceptAddFriendsRequest(){
       contactFriends.child(currentUserId).child(receiverUserId).child("Contacts").setValue("Be Friends").addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   contactFriends.child(receiverUserId).child(currentUserId).child("Contacts").setValue("Be Friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               addFriendsRequest.child(currentUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           addFriendsRequest.child(receiverUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   findFriendsRequestBtn.setEnabled(true);
                                                   currentUserState = "friends";
                                                   findFriendsRequestBtn.setText("Unfriends");

                                                   findFriendsDenidedbtn.setVisibility(View.INVISIBLE);
                                                   findFriendsDenidedbtn.setEnabled(false);
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

    private void Unfriends(){
        contactFriends.child(currentUserId).child(receiverUserId).child("requestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactFriends.child(receiverUserId).child(currentUserId)
                            .child("requestType").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        findFriendsRequestBtn.setEnabled(true);
                                        currentUserState = "new";
                                        findFriendsRequestBtn.setText("Add Friends");

                                        findFriendsDenidedbtn.setVisibility(View.INVISIBLE);
                                        findFriendsDenidedbtn.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

}
