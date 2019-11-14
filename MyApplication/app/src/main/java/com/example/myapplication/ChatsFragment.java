package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatView;
    private RecyclerView chatsList;

    private DatabaseReference ChatListRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth myAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        myAuth = FirebaseAuth.getInstance();
        currentUserID = myAuth.getCurrentUser().getUid();
        ChatListRequestsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) PrivateChatView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatListRequestsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts contacts) {
                        final String list_user_id_chats = getRef(position).getKey();
                        final String[] imageUserChat = {"defauly"};

                        UsersRef.child(list_user_id_chats).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("image")) {
                                        imageUserChat[0] = dataSnapshot.child("image").getValue().toString();

                                        Picasso.get().load(imageUserChat[0]).into(holder.profileImage);
                                    }
                                    final String userNameChat = dataSnapshot.child("name").getValue().toString();
                                    final String userStatusChat = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(userNameChat);
                                    holder.userStatus.setText("");

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id_chat", list_user_id_chats);
                                            chatIntent.putExtra("user_id_name", userNameChat);
                                            chatIntent.putExtra("user_id_image", imageUserChat[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.users_profile_name);
            userStatus = itemView.findViewById(R.id.users_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
