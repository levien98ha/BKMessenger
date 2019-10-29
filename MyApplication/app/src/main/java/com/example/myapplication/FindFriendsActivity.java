package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private RecyclerView myRecyclerList;
    private DatabaseReference userReference, root;
    private FirebaseUser currentUser;
    private FirebaseAuth myAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        root = FirebaseDatabase.getInstance().getReference();

        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser();


        myRecyclerList =  (RecyclerView) findViewById(R.id.find_friends_recycler);
        myRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        myToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMainActivity();
            }
        });
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

   // public  String idCurrentUser = myAuth.getCurrentUser().getDisplayName();



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userReference, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {
               // if(!idCurrentUser.equals(model.getName())){
                    holder.userName.setText(model.getName());
                    holder.userStatus.setText(model.getStatus());
                    Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
               // }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String idUserContact = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("idUserContact",idUserContact);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        myRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.users_profile_name);
            userStatus = itemView.findViewById(R.id.users_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SendUserToMainActivity();
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(FindFriendsActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
