package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth myAuth;
    private DatabaseReference RootReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser();
        RootReference = FirebaseDatabase.getInstance().getReference();

//        toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("BKMessenger");

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    protected void onStart(){
        super.onStart();
        if(currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            VerifyUserExistance();
        }
    }

    private void VerifyUserExistance() {
        String UserCurrentId = myAuth.getCurrentUser().getUid();

        RootReference.child("Users").child(UserCurrentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome !", Toast.LENGTH_SHORT).show();
                }
                else {
                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    //select setting
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_logout){
             myAuth.signOut();
             SendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_friend){
            SendUserToFindFriendsActivity();
        }
        if(item.getItemId() == R.id.main_settings){
            SendUserToSettingActivity();
        }
        return true;
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
       // finish();
    }

    private void SendUserToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
       // settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
       // finish();;
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
       // findFriendsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFriendsIntent);
       // finish();;
    }
}
