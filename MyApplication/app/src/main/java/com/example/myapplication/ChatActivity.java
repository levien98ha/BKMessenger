package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messagernReceiverId, messagerReceiverName, messagerReceiverImage, messengerSenderId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private ImageButton SendMessengerBtn;
    private EditText InputMessenger;

    private FirebaseAuth myAuth;
    private DatabaseReference RootReference;

    private Toolbar myToolbar;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myAuth = FirebaseAuth.getInstance();
        messengerSenderId = myAuth.getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference();


        messagernReceiverId = getIntent().getExtras().get("user_id_chat").toString();
        messagerReceiverName = getIntent().getExtras().get("user_id_name").toString();
        messagerReceiverImage = getIntent().getExtras().get("user_id_image").toString();

        IntializeControllers();

        userName.setText(messagerReceiverName);
       // userLastSeen.setText(messagernReceiverId);
        Picasso.get().load(messagerReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessengerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessenger();
            }
        });

    }

    private void IntializeControllers() {

        myToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar_layout, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.chat_bar_layout_image);
        userName = (TextView) findViewById(R.id.chat_bar_layout_name);
        userLastSeen = (TextView) findViewById(R.id.chat_bar_layout_last_seen);

        SendMessengerBtn = (ImageButton) findViewById(R.id.send_messenger_btn);
        InputMessenger = (EditText) findViewById(R.id.input_messenger);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messenger_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootReference.child("Messages").child(messengerSenderId).child(messagernReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessenger(){
        String messengerText = InputMessenger.getText().toString();

        if(TextUtils.isEmpty(messengerText)){
            Toast.makeText(this, "Text your message..!", Toast.LENGTH_SHORT).show();
        }
        else {
            String messengerSenderRef = "Messages/" + messengerSenderId +"/" + messagernReceiverId;
            String messengerReceiverRef = "Messages/" + messagernReceiverId +"/" + messengerSenderId;

            DatabaseReference userMessengerKeyRef = RootReference.child("Messages").child(messengerSenderId).child(messagernReceiverId).push();

            String messagePushId = userMessengerKeyRef.getKey();

            Map messengerTextBody = new HashMap();
            messengerTextBody.put("message", messengerText);
            messengerTextBody.put("type", "text");
            messengerTextBody.put("from", messengerSenderId);

            Map messengerBodyDetails = new HashMap();
            messengerBodyDetails.put(messengerSenderRef + "/" + messagePushId, messengerTextBody);
            messengerBodyDetails.put(messengerReceiverRef + "/" + messagePushId, messengerTextBody);

            RootReference.updateChildren(messengerBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Your message has been sent!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    InputMessenger.setText("");
                }
            });

        }
   }

}
