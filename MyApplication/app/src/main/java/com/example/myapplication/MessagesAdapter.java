package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        public ImageView receiverMessengerImage, senderMessengerImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessengerText = (TextView) itemView.findViewById(R.id.receiver_messenger_text);
            senderMessengerText = (TextView) itemView.findViewById(R.id.sender_messenger_text);
            receiverProfileImg = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            receiverMessengerImage = (ImageView) itemView.findViewById(R.id.receiver_messenger_image);
            senderMessengerImage =  (ImageView) itemView.findViewById(R.id.sender_messenger_image);
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
    public void onBindViewHolder(@NonNull final MessagesViewHolder messagesViewHolder, final int position) {
        String messageSenderId = myAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

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

        messagesViewHolder.receiverMessengerText.setVisibility(View.GONE);
        messagesViewHolder.receiverProfileImg.setVisibility(View.GONE);
        messagesViewHolder.senderMessengerText.setVisibility(View.GONE);
        messagesViewHolder.senderMessengerImage.setVisibility(View.GONE);
        messagesViewHolder.receiverMessengerImage.setVisibility(View.GONE);

        if(fromMessageType.equals("text")){

            if(fromUserId.equals(messageSenderId)){
                messagesViewHolder.senderMessengerText.setVisibility(View.VISIBLE);

                messagesViewHolder.senderMessengerText.setBackgroundResource(R.drawable.sender_messenger_layout);
                messagesViewHolder.senderMessengerText.setTextColor(Color.WHITE);
                messagesViewHolder.senderMessengerText.setText(messages.getMessage());
            }

            else{
 //               messagesViewHolder.senderMessengerText.setVisibility(View.INVISIBLE);

                messagesViewHolder.receiverProfileImg.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessengerText.setVisibility(View.VISIBLE);

                messagesViewHolder.receiverMessengerText.setBackgroundResource(R.drawable.receiver_messenger_layout);
                messagesViewHolder.receiverMessengerText.setText(messages.getMessage());
            }
        }
        else  if(fromMessageType.equals("image")){
            if(fromUserId.equals(messageSenderId)){
                messagesViewHolder.senderMessengerImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messagesViewHolder.senderMessengerImage);
            }
            else {
                messagesViewHolder.receiverProfileImg.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessengerImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messagesViewHolder.receiverMessengerImage);
            }
        }
        //nếu là file pdf, docx
        else{
            if(fromUserId.equals(messageSenderId)){
                messagesViewHolder.senderMessengerImage.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessengerImage.setBackgroundResource(R.drawable.file_image);

                messagesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messagesViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
            else{
                messagesViewHolder.receiverProfileImg.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessengerImage.setVisibility(View.VISIBLE);

                messagesViewHolder.receiverMessengerImage.setBackgroundResource(R.drawable.file_image);

                messagesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messagesViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
