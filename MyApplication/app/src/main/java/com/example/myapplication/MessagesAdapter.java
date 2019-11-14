package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth myAuth;
    private DatabaseReference userReference;

    public MessagesAdapter (List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{

        public TextView receiverMessengerText, senderMessengerText;
        public CircleImageView receiverProfileImg;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessengerText = (TextView) itemView.findViewById(R.id.receiver_messenger_text);
            senderMessengerText = (TextView) itemView.findViewById(R.id.sender_messenger_text);
            receiverProfileImg = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_messenger_layout, viewGroup, false);

        myAuth = FirebaseAuth.getInstance();

        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder messagesViewHolder, int i) {
        String messageSenderId = myAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("image")){
                    String receiverImg = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImg).placeholder(R.drawable.profile_image).into(messagesViewHolder.receiverProfileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text")){
            messagesViewHolder.receiverMessengerText.setVisibility(View.INVISIBLE);
            messagesViewHolder.receiverProfileImg.setVisibility(View.INVISIBLE);

            if(fromUserId.equals(messageSenderId)){
                messagesViewHolder.senderMessengerText.setBackgroundResource(R.drawable.sender_messenger_layout);
                messagesViewHolder.senderMessengerText.setTextColor(Color.WHITE);
                messagesViewHolder.senderMessengerText.setText(messages.getMessage());
            }

            else{
                messagesViewHolder.senderMessengerText.setVisibility(View.INVISIBLE);

                messagesViewHolder.receiverProfileImg.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessengerText.setVisibility(View.VISIBLE);

                messagesViewHolder.receiverMessengerText.setBackgroundResource(R.drawable.receiver_messenger_layout);
                messagesViewHolder.receiverMessengerText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
